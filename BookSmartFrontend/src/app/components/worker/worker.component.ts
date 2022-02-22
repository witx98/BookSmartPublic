import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {WorkerService} from "../../shared/worker/service/worker.service";
import {ScheduleService} from "../../shared/schedule/service/schedule.service";
import {Schedule} from "../../shared/schedule/interface/Schedule";
import {CalendarOptions} from '@fullcalendar/angular';
import {Worker} from "../../shared/worker/interface/Worker";
import {ReservationService} from "../../shared/reservation/service/reservation.service";
import {Reservation} from "../../shared/reservation/interface/Reservation";
import {Observable} from "rxjs"; // useful for typechecking


export interface Event {
  title: string;
  start: Date;
  end: Date;
}


@Component({
  selector: 'app-worker',
  templateUrl: './worker.component.html',
  styleUrls: ['./worker.component.css']
})
export class WorkerComponent implements OnInit {

  events: Event[] = [];
  currentSchedule: Schedule[] = [];
  loggedWorker!: Worker;
  workerCurrentReservations$!: Observable<Reservation[]>;
  workerPastReservations$!: Observable<Reservation[]>;
  workerSchedules!: Schedule[];

  calendarOptions!: CalendarOptions;

  constructor(private route: ActivatedRoute,
              private workerService: WorkerService,
              private scheduleService: ScheduleService,
              private reservationService: ReservationService) {


  }

  onDateClick() {
    console.log()
    alert('Clicked on date : ')
  }

  ngOnInit(): void {
    let email = String(<string>this.route.snapshot.paramMap.get('email'));
    this.getWorkerByEmail(email);


  }

  // public getWorker(id: number): void {
  //   this.workerService.getWorkerById(id).subscribe(
  //     (response: Worker) => {
  //       console.log("Got worker:");
  //       console.log(response);
  //     }
  //   )
  // }

  public getWorkerByEmail(email: string): void {
    this.workerService.getWorkerByEmail(email).subscribe(
      (response: Worker) => {
        console.log("Got worker:");
        console.log(response);
        this.loggedWorker = response;
        this.getSchedule(this.loggedWorker.id);
        this.workerCurrentReservations$ = this.reservationService.getWorkerCurrentReservations(this.loggedWorker.id);
        this.workerPastReservations$ = this.reservationService.getWorkerPastReservations(this.loggedWorker.id);
        console.log("END")
      }
    )
  }

  public getSchedule(id: number): void {
    this.scheduleService.getSchedulesByWorkerId(id).subscribe(
      (response: Schedule[]) => {
        console.log("Got schedules:");
        console.log(response);
        this.workerSchedules = response;
        this.events = this.schedulesToEvents(this.workerSchedules);
        this.workerCurrentReservations$
          .subscribe(
            (res: Reservation[]) => {
              this.events = this.events.concat(this.reservationsToEvents(res));
              this.prepareCalendar(this.events);
            }
          )
      }
    )
  }

  private schedulesToEvents(schedules: Schedule[]): Event[] {
    return schedules.map(schedule => {
        return {
          title: "Schedule",
          start: schedule.startTime,
          end: schedule.endTime,
        } as Event;
      }
    )
  }

  private reservationsToEvents(reservations: Reservation[]): Event[] {
    return reservations.map(reservation => {
        return {
          title: reservation.service.serviceName,
          start: reservation.startTime,
          end: reservation.endTime,
        } as Event;
      }
    )
  }

  public prepareCalendar(events: Event[]) {
    this.calendarOptions = {
      initialView: 'dayGridMonth',
      headerToolbar: {
        center: 'dayGridMonth,timeGridWeek,timeGridDay'
      },
      eventClick: function (calEvent) {
        alert('Event: ');
        console.log(calEvent.event._def.title)
      },
      events: events,
      displayEventTime: false
    };
  }
}
