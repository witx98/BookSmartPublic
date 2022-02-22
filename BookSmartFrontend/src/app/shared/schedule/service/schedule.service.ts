import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Schedule} from "../interface/Schedule";

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {

  constructor(private http: HttpClient) {
  }

  public getSchedulesByWorkerId(workerId: number): Observable<Schedule[]> {
    return this.http.get<Schedule[]>(`/BookSmart/schedules/worker/${workerId}`);
  }
}
