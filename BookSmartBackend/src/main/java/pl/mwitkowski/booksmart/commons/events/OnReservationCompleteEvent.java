package pl.mwitkowski.booksmart.commons.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pl.mwitkowski.booksmart.reservation.domain.ReservationEntity;

@Getter
public class OnReservationCompleteEvent extends ApplicationEvent {

    private final String appUrl;
    private final ReservationEntity reservation;


    public OnReservationCompleteEvent(ReservationEntity reservation, String appUrl) {
        super(reservation);
        this.reservation = reservation;
        this.appUrl = appUrl;
    }
}
