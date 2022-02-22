package pl.mwitkowski.booksmart.commons.listeners;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import pl.mwitkowski.booksmart.commons.events.OnReservationCompleteEvent;
import pl.mwitkowski.booksmart.email.application.port.EmailSenderUseCase;
import pl.mwitkowski.booksmart.email.domain.EmailBuilder;
import pl.mwitkowski.booksmart.reservation.application.port.ConfirmationTokenUseCase;
import pl.mwitkowski.booksmart.reservation.domain.ReservationEntity;

import java.util.UUID;

@Component
@AllArgsConstructor
public class ReservationCompleteListener implements ApplicationListener<OnReservationCompleteEvent> {

    private final EmailSenderUseCase emailSender;
    private final ConfirmationTokenUseCase confirmationTokenService;

    @Override
    public void onApplicationEvent(OnReservationCompleteEvent event) {
        this.confirmReservation(event);
    }

    private void confirmReservation(OnReservationCompleteEvent event) {
        ReservationEntity reservation = event.getReservation();
        String token = UUID.randomUUID().toString();
        confirmationTokenService.createConfirmationToken(reservation, token);

        String recipientAddress = reservation.getClient().getEmail();
        String subject = "Reservation confirmation";
        String confirmationUrl = "http://localhost:8080" + event.getAppUrl() + "/reservations/confirmReservation?token=" + token;
        String name = reservation.getClient().getFirstname() + " " + reservation.getClient().getLastname();

        emailSender.send(recipientAddress, subject, EmailBuilder.buildReservationConfirmation(name, confirmationUrl, reservation));
    }


}
