package pl.mwitkowski.booksmart.reservation.application;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.time.Duration;


@Value
@ConstructorBinding
@ConfigurationProperties("app.reservations")
public class ReservationProperties {
    Duration confirmationPeriod;
    String abandonCron;
}