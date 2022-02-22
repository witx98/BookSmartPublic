import {NgModule} from '@angular/core'
import {RouterModule, Routes} from "@angular/router";
import {CompanyListComponent} from "./components/company-list/company-list.component";
import {CompanyDetailsComponent} from "./components/company-details/company-details.component";
import {PageNotFoundComponent} from "./components/page-not-found/page-not-found.component";
import {WorkerComponent} from "./components/worker/worker.component";
import {HomePageComponent} from "./components/home-page/home-page.component";
import {LoginComponent} from "./components/login/login.component";
import {RegisterComponent} from "./components/register/register.component";
import {ClientComponent} from "./components/client/client/client.component";


const routes: Routes = [
  {path: '', redirectTo: '/home-page', pathMatch: 'full'},
  {path: 'home-page', component: HomePageComponent},
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {path: 'company-list/:serviceName/:city', component: CompanyListComponent},
  {path: 'company-details/:id', component: CompanyDetailsComponent},
  {path: 'worker/:email', component: WorkerComponent},
  {path: 'client/:email', component: ClientComponent},
  {path: '**', component: PageNotFoundComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}

export const routingComponents = [CompanyListComponent, CompanyDetailsComponent, PageNotFoundComponent, WorkerComponent, HomePageComponent, LoginComponent, RegisterComponent, ClientComponent]
