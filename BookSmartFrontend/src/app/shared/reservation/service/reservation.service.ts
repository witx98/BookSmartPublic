import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {Observable, Subject} from "rxjs";
import {AvailableDate} from "../interface/AvailableDate";
import {switchMap} from "rxjs/operators";
import {AvailableDatesCommand} from "../interface/available-dates-command";
import {CreateReservationCommand} from "../interface/create-reservation-command";
import {Reservation} from "../interface/Reservation";

@Injectable({
  providedIn: 'root'
})
export class ReservationService {

  constructor(private http: HttpClient) {
  }

  search(serviceId: number, workerId: number, selectedDate$: Subject<Date>): Observable<AvailableDate[]> {
    return selectedDate$.pipe(
      switchMap(date => {
        let availableDatesCommand = {
          'workerId': workerId,
          'serviceId': serviceId,
          'selectedDate': date
        } as AvailableDatesCommand;
        return this.getAvailableDates(availableDatesCommand)
      })
    )
  }

  public getAvailableDates(availableDatesCommand: AvailableDatesCommand): Observable<AvailableDate[]> {
    availableDatesCommand.selectedDate.setHours(12);
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    };

    return this.http.post<AvailableDate[]>(`/BookSmart/reservations/availableDates`, availableDatesCommand, httpOptions);
  }

  public makeReservation(createReservationCommand: CreateReservationCommand): Observable<any> {
    return this.http.post<any>(`/BookSmart/reservations`, createReservationCommand);
  }

  public getClientReservations(clientId: number): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`/BookSmart/reservations/client/${clientId}`);
  }

  public getWorkerCurrentReservations(workerId: number): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`/BookSmart/reservations/worker/current/${workerId}`);
  }

  public getWorkerPastReservations(workerId: number): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`/BookSmart/reservations/worker/past/${workerId}`);
  }
}
