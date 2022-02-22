import {Injectable} from '@angular/core';
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {BehaviorSubject, Observable, throwError} from 'rxjs';
import {AuthService} from "../../../shared/auth/service/auth.service";
import {catchError, filter, switchMap, take} from "rxjs/operators";
import {Tokens} from "../../../shared/auth/interface/Tokens";
import {Router} from "@angular/router";

@Injectable()
export class TokenInterceptor implements HttpInterceptor {

  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

  constructor(public authService: AuthService,
              private router: Router) {
  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (this.authService.getAccessToken()) {
      request = this.addAuthorizationHeader(request, this.authService.getAccessToken());
    }
    request = this.addXMLHttPRequestHeader(request);
    return next.handle(request).pipe(

      catchError(error => {
        // if(this.authService.isRefreshTokenExpired()) {
        //   console.log("isRefreshTokenExpired")
        //   this.authService.logout();
        //   this.router.navigate(['../login']);
        //   return throwError(error);
        // }
        if (error instanceof HttpErrorResponse && error.status === 401 && !request.url.includes('BookSmart/login') && !request.url.includes('BookSmart/users/register')) {
          return this.handle401Error(request, next)
        } else {
          this.authService.logout().subscribe();
          return throwError(error);
        }
      })
    );
  }

  private addAuthorizationHeader(request: HttpRequest<any>, accessToken: string | null): HttpRequest<any> {
    return request.clone({
      setHeaders: {
        'Authorization': `Bearer ${accessToken}`
      }
    });
  }

  private addXMLHttPRequestHeader(request: HttpRequest<any>): HttpRequest<any> {
    return request.clone({
      setHeaders: {
        'X-Requested-With': `XMLHttpRequest`
      }
    });
  }

  private handle401Error(request: HttpRequest<any>, next: HttpHandler) {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      return this.authService.refreshToken().pipe(
        switchMap((token: Tokens) => {
          this.isRefreshing = false;
          this.refreshTokenSubject.next(token.access_token);
          return next.handle(this.addAuthorizationHeader(request, token.access_token));
        })
      );
    } else {
      return this.refreshTokenSubject.pipe(
        filter(token => token != null),
        take(1),
        switchMap(accessToken => {
          return next.handle(this.addAuthorizationHeader(request, accessToken));
        })
      );
    }
  }
}
