import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {LoginCommand} from "../../user/interface/LoginCommand";
import {catchError, map, mapTo, tap} from "rxjs/operators";
import {Observable, of} from "rxjs";
import {Tokens} from "../interface/Tokens";
import {User} from "../../user/interface/User";
import {RegisterCommand} from "../../user/interface/register-command";
import {UserRoles} from "../../user/user-roles";
import * as moment from "moment";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly ACCESS_TOKEN = 'access_token';
  private readonly REFRESH_TOKEN = 'refresh_token';
  private loggedUser!: string | null;

  constructor(private http: HttpClient) {
  }

  login(user: LoginCommand)  {
    return this.http.post<any>(`/BookSmart/login`, user)
      .pipe(
        map(res => {
          this.doLoginUser(user.username, res)
          return res;
        })
        // ,
        // // mapTo(true)
        // catchError(error => {
        //   console.log("Login error")
        //   alert(error.error);
        //   return error;
        // })
      );
  }

  logout(): Observable<boolean> {
    return this.http.post<any>(`/BookSmart/logout`, {
      'refresh_token': this.getRefreshToken()
    }).pipe(
      tap(() => {
        console.log("logout");
        this.doLogoutUser();
      })
      // ,
      // mapTo(true),
      // catchError(error => {
      //   alert(error.error);
      //   return of(false);
      // })
    )
  }

  public get isLoggedIn(): boolean {
    return this.getAccessToken() != '';
  }

  private doLoginUser(username: string, tokens: Tokens): void {
    this.loggedUser = username;
    this.storeTokens(tokens);
  }

  private storeTokens(tokens: Tokens) {
    localStorage.setItem(this.ACCESS_TOKEN, tokens.access_token);
    localStorage.setItem(this.REFRESH_TOKEN, tokens.refresh_token);
  }

  private getRefreshToken(): string {
    let item = localStorage.getItem(this.REFRESH_TOKEN);
    return item != null ? item : '';
  }

   getAccessToken(): string {
     let item = localStorage.getItem(this.ACCESS_TOKEN);
    return item != null ? item : '';
  }

  private doLogoutUser() {
    console.log("doLogoutUser")
    this.loggedUser = null;
    this.removeTokens();
  }

  private removeTokens() {
    localStorage.removeItem(this.ACCESS_TOKEN);
    localStorage.removeItem(this.REFRESH_TOKEN);
  }

  refreshToken() {
    return this.http.post<any>(`BookSmart/users/token/refresh`, {
      'refresh_token': this.getRefreshToken()
    }).pipe(
      tap((tokens: Tokens) => {
        this.storeAccessToken(tokens.access_token);
      })
    );
  }

  private storeAccessToken(access_token: string) {
    localStorage.setItem(this.ACCESS_TOKEN, access_token);
  }

  isRefreshTokenExpired() {
    let date = new Date(JSON.parse(atob(this.getRefreshToken()?.split('.')[1])).exp);
    console.log("DATE " + date);
    return moment().isAfter(date);
  }

  getUser(): User {
    return JSON.parse(atob(this.getAccessToken()?.split('.')[1])) as User;
  }

  register(registerCommand: RegisterCommand) {
    console.log("registerCommand:");
    console.log(registerCommand);
    return this.http.post<any>(`/BookSmart/users/register`, registerCommand);
  }

  get isClientLoggedIn(): boolean {
    return this.isLoggedIn && this.getUser().roles.includes(UserRoles.CLIENT);
  }

  get isWorkerLoggedIn(): boolean {
    return this.isLoggedIn && this.getUser().roles.includes(UserRoles.WORKER);
  }
}
