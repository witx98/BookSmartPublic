import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {Company} from "../interface/Company";

@Injectable({
  providedIn: 'root'
})
export class CompanyService {

  constructor(private http: HttpClient) {
  }

  public getCompanies(serviceName: string, city: string): Observable<Company[]> {
    let params = new HttpParams({fromObject: {serviceName, city}});
    return this.http.get<Company[]>(`/BookSmart/companies`,{params});
  }
  public getCompanyById(companyId: number): Observable<Company> {
    return this.http.get<Company>(`/BookSmart/companies/${companyId}`);
  }
}
