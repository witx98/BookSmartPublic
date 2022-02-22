package pl.mwitkowski.booksmart.reservation.web;

import lombok.Value;
import pl.mwitkowski.booksmart.client.web.RestClient;
import pl.mwitkowski.booksmart.reservation.domain.ReservationStatus;
import pl.mwitkowski.booksmart.service.web.RestService;
import pl.mwitkowski.booksmart.worker.web.RestWorker;

import java.time.LocalDateTime;

@Value
public class RestReservation {
    Long id;
    RestWorker worker;
    RestClient client;
    RestService service;
    ReservationStatus status;
    LocalDateTime startTime;
    LocalDateTime endTime;

}
