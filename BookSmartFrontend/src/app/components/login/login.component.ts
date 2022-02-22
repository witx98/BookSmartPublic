import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../../shared/auth/service/auth.service";
import {AlertService} from "../../shared/alert/service/alert.service";
import {first} from "rxjs/operators";
import {LoginCommand} from "../../shared/user/interface/LoginCommand";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  form!: FormGroup;
  loading = false;
  submitted = false;

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private alertService: AlertService
  ) {
  }

  ngOnInit() {
    this.form = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  get f() {
    return this.form.controls;
  }

  onSubmit() {
    this.submitted = true;

    // reset alerts on submit
    this.alertService.clear();

    // stop here if form is invalid
    if (this.form.invalid) {
      return;
    }

    this.loading = true;

    let command =
      {
        username: this.f.username.value,
        password: this.f.password.value
      } as LoginCommand
    this.authService.login(command)
      .pipe(first())
      .subscribe({
        next: () => {
          // get return url from query parameters or default to home page
          this.alertService.success('Login successful', {keepAfterRouteChange: true});
          const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
          this.router.navigateByUrl(returnUrl);
        },
        error: error => {
          console.log(error)
          this.alertService.error(error);
          this.loading = false;
        }
      });
  }

}
