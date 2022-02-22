package pl.mwitkowski.booksmart.reservation.application;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.mwitkowski.booksmart.reservation.application.port.ReservationUseCase;
import pl.mwitkowski.booksmart.reservation.application.port.ReservationUseCase.UpdateReservationStatusCommand;
import pl.mwitkowski.booksmart.reservation.db.ReservationRepository;
import pl.mwitkowski.booksmart.reservation.domain.ReservationEntity;
import pl.mwitkowski.booksmart.reservation.domain.ReservationStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Component
@AllArgsConstructor
public class AbandonedReservationsJob {
    private final ReservationRepository reservationRepository;
    private final ReservationUseCase reservationService;
    private final ReservationProperties properties;
    private final UserDetails systemUser;

    @Transactional
    @Scheduled(cron = "${app.reservations.abandon-cron}")
    public void run() {
        prepareAuthentication();
        searchAbandonedReservations();
        searchDoneReservations();
    }

    private void searchAbandonedReservations() {
        Duration confirmationPeriod = properties.getConfirmationPeriod();
        LocalDateTime olderThan = LocalDateTime.now().minus(confirmationPeriod);
        List<ReservationEntity> abandonedReservations = reservationRepository.findByStatusAndCreatedAtLessThanEqual(ReservationStatus.NEW, olderThan);
        log.info("Found reservations to be abandoned: " + abandonedReservations.size());
        abandonedReservations.forEach(reservation -> {
            UpdateReservationStatusCommand command = new UpdateReservationStatusCommand(reservation.getId(), ReservationStatus.ABANDONED);
            reservationService.updateReservationStatus(command);
        });
    }

    private void searchDoneReservations() {
        LocalDateTime olderThan = LocalDateTime.now();
        List<ReservationEntity> doneReservations = reservationRepository.findByStatusAndEndTimeLessThanEqual(ReservationStatus.CONFIRMED, olderThan);
        log.info("Found reservations to be done: " + doneReservations.size());
        doneReservations.forEach(reservation -> {
            UpdateReservationStatusCommand command = new UpdateReservationStatusCommand(reservation.getId(), ReservationStatus.DONE);
            reservationService.updateReservationStatus(command);
        });
    }

    private void prepareAuthentication() {
        Authentication auth = new UsernamePasswordAuthenticationToken(systemUser.getUsername(), systemUser.getPassword(), systemUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
