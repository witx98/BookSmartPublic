package pl.mwitkowski.booksmart.reservation.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

public enum ReservationStatus {
    NEW {
        @Override
        public UpdateStatusResult updateStatus(ReservationStatus status) {
            return switch (status) {
                case CONFIRMED -> UpdateStatusResult.ok(CONFIRMED);
                case CANCELLED -> UpdateStatusResult.revoked(CANCELLED);
                case ABANDONED -> UpdateStatusResult.revoked(ABANDONED);
                default -> super.updateStatus(status);
            };
        }
    },
    CONFIRMED {
        @Override
        public UpdateStatusResult updateStatus(ReservationStatus status) {
            if (status == DONE) {
                return UpdateStatusResult.ok(DONE);
            }
            return super.updateStatus(status);
        }
    },
    CANCELLED,
    ABANDONED,
    DONE;

    public static Optional<ReservationStatus> parseString(String value) {
        return Arrays.stream(values())
                .filter(it -> StringUtils.equalsIgnoreCase(it.name(), value))
                .findFirst();
    }

    public UpdateStatusResult updateStatus(ReservationStatus status) {
        throw new IllegalArgumentException("Unable to mark " + this.name() + " order as " + status.name());
    }
}
