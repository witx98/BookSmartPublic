import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppComponent} from './app.component';
import {AppRoutingModule, routingComponents} from "./app-routing.module";
import {CompanyService} from "./shared/company/service/company.service";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {MatIconModule} from '@angular/material/icon';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {IconsModule, MDBBootstrapModule} from "angular-bootstrap-md";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import { FullCalendarModule } from '@fullcalendar/angular'; // must go before plugins
import dayGridPlugin from '@fullcalendar/daygrid'; // a plugin!
import interactionPlugin from '@fullcalendar/interaction'; // a plugin!
import timeGrid from '@fullcalendar/timegrid';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import { MakeReservationDialogComponent } from './components/make-reservation-dialog/make-reservation-dialog.component';
import {MatDialogModule} from "@angular/material/dialog";
import {MatListModule} from "@angular/material/list";
import {MatStepperModule} from "@angular/material/stepper";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MatNativeDateModule} from "@angular/material/core";
import { AlertComponent } from './components/alert/alert.component';
import {ErrorInterceptor} from "./core/error/interceptor/error.interceptor";
import {TokenInterceptor} from "./core/auth/interceptor/token.interceptor";
import {ScrollingModule} from "@angular/cdk/scrolling";
// import {MAT_MOMENT_DATE_FORMATS, MomentDateAdapter} from '@angular/material-moment-adapter';


FullCalendarModule.registerPlugins([
  dayGridPlugin,
  interactionPlugin,
  timeGrid
])

@NgModule({
  declarations: [
    AppComponent,
    routingComponents,
    MakeReservationDialogComponent,
    AlertComponent
  ],
    imports: [
        BrowserModule,
        HttpClientModule,
        AppRoutingModule,
        MatIconModule,
        BrowserAnimationsModule,
        IconsModule,
        MDBBootstrapModule,
        MatFormFieldModule,
        MatInputModule,
        FullCalendarModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatCardModule,
        MatDialogModule,
        MatListModule,
        MatStepperModule,
        FormsModule,
        MatDatepickerModule,
        MatNativeDateModule,
        ScrollingModule
    ],
  providers: [CompanyService,
    {provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true},
    {provide: MAT_DATE_LOCALE, useValue: 'pl-PL'}
    //,
    // {provide: DateAdapter, useClass: MomentDateAdapter, deps:[MAT_DATE_LOCALE]},
    // {provide: MAT_DATE_FORMATS, useValue: MAT_MOMENT_DATE_FORMATS}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
