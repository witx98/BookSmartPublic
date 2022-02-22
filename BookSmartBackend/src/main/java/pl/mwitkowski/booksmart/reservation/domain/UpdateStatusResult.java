package pl.mwitkowski.booksmart.reservation.domain;

import lombok.Value;

@Value
public class UpdateStatusResult {
    ReservationStatus newStatus;
    boolean revoked;

    static UpdateStatusResult ok(ReservationStatus newStatus) {
        return new UpdateStatusResult(newStatus, false);
    }

    static UpdateStatusResult revoked(ReservationStatus newStatus) {
        return new UpdateStatusResult(newStatus, true);
    }
}