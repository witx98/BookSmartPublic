package pl.mwitkowski.booksmart.schedule.web;

import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
public class RestSchedule {
    Long id;
    LocalDate date;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Long workerId;
}
