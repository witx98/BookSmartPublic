import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {Company} from "../../shared/company/interface/Company";
import {CompanyService} from "../../shared/company/service/company.service";
import {Service} from "../../shared/service/interface/Service";
import {ServiceService} from "../../shared/service/service/service.service";
import {BehaviorSubject, Observable} from "rxjs";
import {take} from "rxjs/operators";

@Component({
  selector: 'app-company-details',
  templateUrl: './company-details.component.html',
  styleUrls: ['./company-details.component.scss']
})
export class CompanyDetailsComponent implements OnInit {

  selectedCompany!: Company;
  selectedCompanyId!: number;
  selectedCompanyServices$!: Observable<Service[]>;
  serviceName$ = new BehaviorSubject<string>('');

  constructor(private route: ActivatedRoute,
              private companyService: CompanyService,
              private serviceService: ServiceService) {

    this.route.paramMap
      .pipe(take(1))
      .subscribe(params => {
        this.selectedCompanyId = Number(params.get('id'));
        this.selectedCompanyServices$ = this.serviceService.search(this.selectedCompanyId, this.serviceName$);
      });
  }

  ngOnInit(): void {
    this.getCompany(this.selectedCompanyId);
  }

  public getCompany(id: number): void {
    this.companyService.getCompanyById(id).subscribe(
      (response: Company) => {
        console.log("Got company:");
        console.log(response);
        this.selectedCompany = response;
        console.log(this.selectedCompany.pictureUrl);
      }
    )
  }

  identifyService(index: number, service: Service): number {
    return service.id;
  }

}
