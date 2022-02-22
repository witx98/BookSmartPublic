import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {take} from "rxjs/operators";
import {ClientService} from "../../../shared/client/service/client.service";
import {ReservationService} from "../../../shared/reservation/service/reservation.service";
import {Client} from "../../../shared/client/interface/client";
import {Observable} from "rxjs";
import {Reservation} from "../../../shared/reservation/interface/Reservation";
import {ReservationsStatus} from "../../../shared/reservation/interface/reservations-status";

@Component({
  selector: 'app-client',
  templateUrl: './client.component.html',
  styleUrls: ['./client.component.css']
})
export class ClientComponent implements OnInit {
  loggedClientEmail!: string;
  loggedClient!: Client;
  clientReservations$!: Observable<Reservation[]>

  constructor(private route: ActivatedRoute,
              private clientService: ClientService,
              private reservationService: ReservationService) {
    this.route.paramMap
      .pipe(take(1))
      .subscribe(params => {
        this.loggedClientEmail = String(params.get('email'));
      });
  }

  ngOnInit(): void {
    this.clientService.getClient(this.loggedClientEmail).subscribe(
      (response: Client) => {
        console.log("Got client:");
        console.log(response);
        this.loggedClient = response;
        this.clientReservations$ = this.reservationService.getClientReservations(this.loggedClient.id);
      }
    )

  }

  canNotBeConfirmed(reservation: Reservation) {
    return reservation.status !== ReservationsStatus.NEW;
  }

  canBeCancelled(reservation: Reservation) {
    return !(reservation.status === ReservationsStatus.NEW || reservation.status === ReservationsStatus.CONFIRMED);
  }
}
