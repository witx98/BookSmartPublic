import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA} from "@angular/material/dialog";
import {ScheduleService} from "../../shared/schedule/service/schedule.service";
import {ServiceService} from "../../shared/service/service/service.service";
import {CompanyService} from "../../shared/company/service/company.service";
import {Company} from "../../shared/company/interface/Company";
import {Worker} from "../../shared/worker/interface/Worker";
import {Service} from "../../shared/service/interface/Service";
import {Schedule} from "../../shared/schedule/interface/Schedule";
import {ReservationService} from "../../shared/reservation/service/reservation.service";
import {AvailableDate} from "../../shared/reservation/interface/AvailableDate";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {CreateClientCommand} from "../../shared/client/interface/create-client-command";
import {AvailableDatesCommand} from "../../shared/reservation/interface/available-dates-command";
import {CreateReservationCommand} from "../../shared/reservation/interface/create-reservation-command";
import {first, take} from "rxjs/operators";
import {AlertService} from "../../shared/alert/service/alert.service";
import {AuthService} from "../../shared/auth/service/auth.service";
import {User} from "../../shared/user/interface/User";
import {Client} from "../../shared/client/interface/client";
import {ClientService} from "../../shared/client/service/client.service";

@Component({
  selector: 'app-make-reservation-dialog',
  templateUrl: './make-reservation-dialog.component.html',
  styleUrls: ['./make-reservation-dialog.component.css']
})
export class MakeReservationDialogComponent implements OnInit {

  //pre-dialog
  selectedCompany!: Company;
  selectedService!: Service;
  loggedClient!: Client;

  //part 1
  selectedWorker!: Worker;
  selectedWorkerSchedules: Schedule[] = [];

  //part 2
  selectedDate: Date;
  minDate: Date;
  maxDate: Date;
  availableDates: AvailableDate[] = [];
  selectedAvailableDate!: AvailableDate;

  //part 3
  form!: FormGroup;
  filled = false;
  createClientCommand!: CreateClientCommand;

  //part 4
  reservationCompleted: boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: any,
              private companyService: CompanyService,
              private scheduleService: ScheduleService,
              private serviceService: ServiceService,
              private reservationService: ReservationService,
              private formBuilder: FormBuilder,
              private alertService: AlertService,
              private authService: AuthService,
              private clientService: ClientService) {

    if(this.authService.isClientLoggedIn) {
      this.clientService.getClient(this.authService.getUser().sub)
        .pipe(take(1))
        .subscribe(
        (response: Client) => {
          console.log("Got client: ");
          console.log(response);
          this.loggedClient = response;
        }
      );
    }

    const currentYear = new Date().getFullYear();
    this.minDate = new Date(Date.now());
    this.selectedDate = this.minDate;
    this.maxDate = new Date(currentYear + 1, 11, 31);


    this.companyService.getCompanyById(this.data.companyId).subscribe(
      (response: Company) => {
        console.log("Got company:");
        console.log(response);
        this.selectedCompany = response;
      });

    this.serviceService.getById(this.data.serviceId).subscribe(
      (response: Service) => {
        console.log("Got service:");
        console.log(response);
        this.selectedService = response;
      }
    )

  }

  ngOnInit(): void {
    this.form = this.formBuilder.group({
      firstname: ['', Validators.required],
      lastname: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern("^((\\+48-?)|0)?[0-9]{9}$")]]
    });

  }

  getAvailableDates(serviceId: number, workerId: number, selectedDate: Date) {
    console.log("getAvailableDates " + selectedDate)
    let availableDatesCommand = {
      'workerId': workerId,
      'serviceId': serviceId,
      'selectedDate': selectedDate
    } as AvailableDatesCommand;
    this.reservationService.getAvailableDates(availableDatesCommand).subscribe(
      (response: AvailableDate[]) => {
        console.log("Got available dates:");
        console.log(response);
        this.availableDates = response;
      }
    )
  }

  setSelectedWorker(worker: Worker) {
    console.log(worker);
    this.selectedWorker = worker;
    this.setDate(this.minDate);
  }

  setDate($event: Date) {
    this.selectedDate = $event;
    console.log("Selected date: " + this.selectedDate)
    this.getAvailableDates(this.selectedService.id, this.selectedWorker.id, this.selectedDate);
    console.log("Date: " + $event.getDate());
  }


  // convenience getter for easy access to form fields
  get f() {
    return this.form.controls;
  }

  get isFormInvalid(): boolean {
    return this.form.invalid;
  }

  get isFormSubmitted(): boolean {
    return this.filled;
  }

  get isClientLoggedIn() {
    return this.authService.isClientLoggedIn;
  }

  onContactDataFormSubmit() {
    if (this.form.invalid) {
      return;
    }
    this.createClientCommand = this.form.value;
    this.filled = true;
  }

  setSelectedAvailableDate(availableDate: AvailableDate) {
    this.selectedAvailableDate = availableDate;
    if(this.authService.isClientLoggedIn) {
      console.log(this.loggedClient)
      this.form.setValue({
        firstname: this.loggedClient.firstname,
        lastname: this.loggedClient.lastname,
        email: this.loggedClient.email,
        phone: this.loggedClient.phone
      });
    }
  }

  makeReservation() {
    let reservationCommand = {
      'startTime': this.selectedAvailableDate.startTime,
      'workerId': this.selectedWorker.id,
      'createClientCommand': this.createClientCommand,
      'serviceId': this.selectedService.id
    } as CreateReservationCommand;

    this.reservationService.makeReservation(reservationCommand)
      .pipe(first())
          .subscribe({
            next: () => {
              this.alertService.success('Successfully made reservation', { keepAfterRouteChange: true });
              this.reservationCompleted = true
            },
            error: (error: string) => {
              this.alertService.error(error);
            }
          });
  }
}
