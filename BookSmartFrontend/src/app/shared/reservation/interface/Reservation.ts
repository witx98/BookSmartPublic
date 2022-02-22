import {Worker} from "../../worker/interface/Worker";
import {Service} from "../../service/interface/Service";
import {Client} from "../../client/interface/client";
import {ReservationsStatus} from "./reservations-status";

export interface Reservation {
  id: number;
  worker: Worker;
  service: Service;
  client: Client;
  status: ReservationsStatus;
  startTime: Date;
  endTime: Date;
}
