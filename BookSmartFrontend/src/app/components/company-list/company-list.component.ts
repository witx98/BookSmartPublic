import {Component, OnInit} from '@angular/core';
import {CompanyService} from "../../shared/company/service/company.service";
import {Company} from "../../shared/company/interface/Company";
import {ActivatedRoute, Router} from "@angular/router";
import {MatDialog} from "@angular/material/dialog";
import {MakeReservationDialogComponent} from "../make-reservation-dialog/make-reservation-dialog.component";

@Component({
  selector: 'app-company-list',
  templateUrl: './company-list.component.html',
  styleUrls: ['./company-list.component.scss']
})
export class CompanyListComponent implements OnInit {

  public companies: Company[] = [];
  searchedServiceName: string = '';
  searchedCity: string = '';

  constructor(private companyService: CompanyService,
              private router: Router,
              private route: ActivatedRoute,
              private dialog: MatDialog) {

    this.route.paramMap
      .subscribe(params => {
        this.searchedServiceName = String(params.get('serviceName'));
        this.searchedCity = String(params.get('city'));
      });
  }

  ngOnInit(): void {
    console.log("LOG")
    this.getCompanies(this.searchedServiceName, this.searchedCity);
  }

  public getCompanies(serviceName: string, city: string): void {
    this.companyService.getCompanies(serviceName, city).subscribe(
      (response: Company[]) => {
        console.log("Got companies list:");
        console.log(response);
        this.companies = response;
      }
    )
  }

  onSelect(company: Company) {
    this.router.navigate(['/company-details', company.id]);
  }

  openReservationDialog(companyId: number, serviceId: number) {
    this.dialog.open(MakeReservationDialogComponent, {data: {companyId: companyId, serviceId: serviceId}});
  }
}
