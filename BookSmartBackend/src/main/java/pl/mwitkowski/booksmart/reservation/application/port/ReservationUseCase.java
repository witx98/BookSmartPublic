package pl.mwitkowski.booksmart.reservation.application.port;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.springframework.http.HttpStatus;
import pl.mwitkowski.booksmart.client.application.port.ClientUseCase.CreateClientCommand;
import pl.mwitkowski.booksmart.commons.Either;
import pl.mwitkowski.booksmart.reservation.domain.ReservationEntity;
import pl.mwitkowski.booksmart.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationUseCase {

    CreateReservationResponse createReservation(CreateReservationCommand command);

    UpdateReservationStatusResponse updateReservationStatus(UpdateReservationStatusCommand command);

    List<ReservationEntity> findAll();

    List<ReservationEntity> findByEmail(String email);

    Optional<ReservationEntity> findById(Long id);

    List<ReservationEntity> findByWorkerId(Long workerId);

    List<ReservationEntity> findByClientId(Long clientId);

    List<AvailableDate> getAvailableReservationDates(Long serviceId, Long workerId, LocalDate selectedDate);

    String confirmReservation(String token);

    List<ReservationEntity> findCurrentWorkerReservations(Long workerId);

    List<ReservationEntity> findPastWorkerReservations(Long workerId);

    @AllArgsConstructor
    @Getter
    enum Error {
        NOT_FOUND(HttpStatus.NOT_FOUND),
        FORBIDDEN(HttpStatus.FORBIDDEN);

        private final HttpStatus status;
    }

    @Value
    @Builder
    @AllArgsConstructor
    class CreateReservationCommand {
        LocalDateTime startTime;
        Long workerId;
        CreateClientCommand client;
        Long serviceId;

        ReservationStatus status = ReservationStatus.NEW;
    }


    @Value
    @AllArgsConstructor
    class AvailableDate {
        LocalDateTime startTime;
        LocalDateTime endTime;
    }


    class CreateReservationResponse extends Either<String, ReservationEntity> {
        CreateReservationResponse(boolean success, String left, ReservationEntity right) {
            super(success, left, right);
        }

        public static CreateReservationResponse success(ReservationEntity reservation) {
            return new CreateReservationResponse(true, null, reservation);
        }

        public static CreateReservationResponse failure(String error) {
            return new CreateReservationResponse(false, error, null);
        }
    }

    @Value
    class UpdateReservationStatusCommand {
        Long reservationId;
        ReservationStatus status;
    }

    class UpdateReservationStatusResponse extends Either<Error, ReservationStatus> {
        UpdateReservationStatusResponse(boolean success, Error left, ReservationStatus right) {
            super(success, left, right);
        }

        public static UpdateReservationStatusResponse success(ReservationStatus reservationStatus) {
            return new UpdateReservationStatusResponse(true, null, reservationStatus);
        }

        public static UpdateReservationStatusResponse failure(Error error) {
            return new UpdateReservationStatusResponse(false, error, null);
        }
    }
}
