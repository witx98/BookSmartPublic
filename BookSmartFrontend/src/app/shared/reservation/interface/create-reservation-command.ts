import {CreateClientCommand} from "../../client/interface/create-client-command";

export interface CreateReservationCommand {
  startTime: Date;
  workerId: number;
  createClientCommand: CreateClientCommand;
  serviceId: number;
}
