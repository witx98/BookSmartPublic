import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Worker} from "../interface/Worker";

@Injectable({
  providedIn: 'root'
})
export class WorkerService {

  constructor(private http: HttpClient) {
  }

  public getWorkerById(workerId: number): Observable<Worker> {
    return this.http.get<Worker>(`/BookSmart/workers/${workerId}`);
  }

  public getWorkerByEmail(workerEmail: string): Observable<Worker> {
    return this.http.get<Worker>(`/BookSmart/workers/byEmail/${workerEmail}`);
  }
}
