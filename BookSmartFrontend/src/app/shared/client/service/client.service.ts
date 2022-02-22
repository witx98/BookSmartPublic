import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {Client} from "../interface/client";

@Injectable({
  providedIn: 'root'
})
export class ClientService {

  constructor(private http: HttpClient) {

  }

  public getClient(email: string): Observable<Client> {
    return this.http.get<Client>(`/BookSmart/clients/byEmail/${email}`);
  }
}
