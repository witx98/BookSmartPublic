import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable, Subject} from "rxjs";
import {Service} from "../interface/Service";
import {debounceTime, distinctUntilChanged, switchMap} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class ServiceService {

  constructor(private http: HttpClient) {
  }

  search(companyId: number, serviceName$: Subject<string>): Observable<Service[]> {
    return serviceName$
      .pipe(
        debounceTime(400),
        distinctUntilChanged(),
        switchMap( name => this.searchServices(companyId, name))
      )
  }

  public searchServices(companyId: number, serviceName: string): Observable<Service[]> {
    let params = new HttpParams({fromObject: {companyId, serviceName}});
    return this.http.get<Service[]>(`/BookSmart/services`, {params});
  }

  public getById(serviceId: number): Observable<Service> {
    return this.http.get<Service>(`/BookSmart/services/${serviceId}`);
  }

}
