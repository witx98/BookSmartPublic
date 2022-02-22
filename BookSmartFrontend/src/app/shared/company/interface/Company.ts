import {Worker} from "../../worker/interface/Worker";
import {Service} from "../../service/interface/Service";

export interface Company {
  id: number;
  companyName: string;
  phone: string;
  email: string;
  street: string;
  city: string;
  zipCode: string;
  pictureUrl: string;
  description: string;
  workers: Worker[];
  services: Service[];
}
