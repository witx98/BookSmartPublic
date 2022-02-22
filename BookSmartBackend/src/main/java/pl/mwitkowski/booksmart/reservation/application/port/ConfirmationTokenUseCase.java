package pl.mwitkowski.booksmart.reservation.application.port;

import pl.mwitkowski.booksmart.reservation.domain.ConfirmationToken;
import pl.mwitkowski.booksmart.reservation.domain.ReservationEntity;

public interface ConfirmationTokenUseCase {

    ConfirmationToken findByToken(String token);

    void createConfirmationToken(ReservationEntity reservation, String token);

    void setConfirmedAt(String token);

}
