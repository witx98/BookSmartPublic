import {Component} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FormControl, FormGroup} from "@angular/forms";
import {AuthService} from "./shared/auth/service/auth.service";
import {first} from "rxjs/operators";
import {AlertService} from "./shared/alert/service/alert.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'BookSmartFrontend';
  companyListForm = new FormGroup({
    serviceName: new FormControl(),
    city: new FormControl()
  })

  constructor(private router: Router,
              private route: ActivatedRoute,
              private authService: AuthService,
              private alertService: AlertService) {

  }

  searchCompanyList(serviceName: string, city: string) {
    this.router.navigate(['/'], {skipLocationChange: true}).then(() => {
      this.router.navigate(['/company-list', serviceName, city]);
    });
  }

  isUserLoggedIn(): boolean {
    return this.authService.isLoggedIn;
  }

  isClientLoggedIn(): boolean {
    return this.authService.isClientLoggedIn;
  }

  singIn(): void {
    this.router.navigate(['/login']);
  }

  signUp(): void {
    this.router.navigate(['/register']);
  }

  clientReservations(): void {
    console.log(this.authService.getUser().sub)
    this.router.navigate(['/client', this.authService.getUser().sub]);
  }

  isWorkerLoggedIn(): boolean {
    return this.authService.isWorkerLoggedIn;
  }

  logout(): void {
    this.authService.logout()
      .pipe(first())
      .subscribe({
        next: () => {
          // get return url from query parameters or default to home page
          this.alertService.success('Logout successful', { keepAfterRouteChange: true });
          const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
          this.router.navigateByUrl(returnUrl);
        },
        error: error => {
          this.alertService.error(error);
        }
      });
  }

  workerSchedule(): void {
    console.log(this.authService.getUser().sub)
    this.router.navigate(['/worker', this.authService.getUser().sub]);

  }
}
