package pl.mwitkowski.booksmart.reservation.application;

import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.mwitkowski.booksmart.client.application.port.ClientUseCase;
import pl.mwitkowski.booksmart.client.domain.ClientEntity;
import pl.mwitkowski.booksmart.reservation.application.port.ConfirmationTokenUseCase;
import pl.mwitkowski.booksmart.reservation.application.port.ReservationUseCase;
import pl.mwitkowski.booksmart.reservation.db.ReservationRepository;
import pl.mwitkowski.booksmart.reservation.domain.ConfirmationToken;
import pl.mwitkowski.booksmart.reservation.domain.ReservationEntity;
import pl.mwitkowski.booksmart.reservation.domain.ReservationStatus;
import pl.mwitkowski.booksmart.reservation.domain.UpdateStatusResult;
import pl.mwitkowski.booksmart.schedule.application.port.ScheduleUseCase;
import pl.mwitkowski.booksmart.schedule.domain.ScheduleEntity;
import pl.mwitkowski.booksmart.security.UserSecurity;
import pl.mwitkowski.booksmart.security.userDetails.application.port.AuthenticationProviderUseCase;
import pl.mwitkowski.booksmart.service.application.port.ServiceEntityUseCase;
import pl.mwitkowski.booksmart.service.domain.ServiceEntity;
import pl.mwitkowski.booksmart.worker.application.port.WorkerUseCase;
import pl.mwitkowski.booksmart.worker.domain.WorkerEntity;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
class ReservationService implements ReservationUseCase {

    private final ScheduleUseCase scheduleService;
    private final WorkerUseCase workerService;
    private final ClientUseCase clientService;
    private final ServiceEntityUseCase service;
    private final ReservationRepository reservationRepository;
    private final UserSecurity userSecurity;
    private final AuthenticationProviderUseCase authenticationProvider;
    private final UserDetails systemUser;
    private final ConfirmationTokenUseCase confirmationTokenService;

    @Override
    @Transactional
    public CreateReservationResponse createReservation(CreateReservationCommand command) {

        Optional<WorkerEntity> worker = workerService.findById(command.getWorkerId());

        if (worker.isEmpty()) {
            return CreateReservationResponse.failure("The worker with id: " + command.getWorkerId() + " not found.");
        }

        Optional<ScheduleEntity> workerScheduleForDate = scheduleService
                .findWorkerScheduleForDate(command.getWorkerId(), command.getStartTime().toLocalDate());

        if (workerScheduleForDate.isEmpty()) {
            return CreateReservationResponse.failure("The worker with id: " + command.getWorkerId()
                    + " has no schedule for that date: " + command.getStartTime().toLocalDate());
        }

        Optional<ServiceEntity> serviceEntity = this.service.findById(command.getServiceId());

        if (serviceEntity.isEmpty()) {
            return CreateReservationResponse.failure("The service with id: " + command.getServiceId() + " not found.");
        }


        LocalDateTime endTime = command.getStartTime().plus(serviceEntity.get().getDuration());
        Optional<ReservationEntity> overlappingReservation = reservationRepository
                .findOverlappingReservation(command.getWorkerId(), command.getStartTime(), endTime, ReservationStatus.CANCELLED, ReservationStatus.ABANDONED);

        if (overlappingReservation.isPresent()) {
            return CreateReservationResponse.failure("The worker with id: " + command.getWorkerId()
                    + " has overlapping reservation in that time: " + command.getStartTime() + " - " + endTime);
        }

        List<ReservationEntity> workerOtherReservations = reservationRepository
                .findReservationsInBetweenDates(command.getWorkerId(), command.getStartTime(), endTime, ReservationStatus.CANCELLED, ReservationStatus.ABANDONED);

        if (workerOtherReservations.size() > 0) {
            return CreateReservationResponse.failure("The worker with id: " + command.getWorkerId()
                    + " has other reservations in that time: " + command.getStartTime() + " - " + endTime);
        }

        ClientUseCase.CreateClientResponse clientResponse = clientService.getOrCreateClient(command.getClient());

        if (!clientResponse.isSuccess()) {
            return CreateReservationResponse.failure(clientResponse.getLeft());
        }
        prepareAuthentication();
        ClientEntity client = clientService.findById(clientResponse.getRight()).orElseThrow(() -> new IllegalStateException("Client with id: " + clientResponse.getRight() + " not found"));

        ReservationEntity reservation = ReservationEntity.builder()
                .startTime(command.getStartTime())
                .client(client)
                .service(serviceEntity.get())
                .worker(worker.get())
                .endTime(endTime)
                .build();

        return CreateReservationResponse.success(reservationRepository.save(reservation));
    }

    @Override
    public List<AvailableDate> getAvailableReservationDates(Long serviceId, Long workerId, LocalDate selectedDate) {
        ServiceEntity serviceEntity = service.findById(serviceId).orElseThrow(() -> new IllegalArgumentException("Service with id:" + serviceId + " not found."));
        Optional<ScheduleEntity> schedule = scheduleService.findWorkerScheduleForDate(workerId, selectedDate);

        if (schedule.isEmpty()) {
            return Collections.emptyList();
        }

        List<ReservationEntity> reservations = reservationRepository.findReservationsInBetweenDates(workerId, schedule.get().getStartTime(), schedule.get().getEndTime(), ReservationStatus.CANCELLED, ReservationStatus.ABANDONED);

        //TODO: schedule - startTime (start of hour) - endTime(end of hour)
        long timeSlotsAmount = Duration.between(LocalTime.MIN, LocalTime.MAX).dividedBy(Duration.ofMinutes(15));
        long serviceDurationInMinutes = serviceEntity.getDuration().toMinutes();

        LocalDateTime startTime = LocalDateTime.of(schedule.get().getDate(), LocalTime.MIN);
        LocalDateTime endTime = startTime.plusMinutes(serviceDurationInMinutes);

        List<AvailableDate> availableDates = new ArrayList<>();
        for (int i = 0; i < timeSlotsAmount; i++) {
            if ((startTime.isAfter(schedule.get().getStartTime()) || startTime.isEqual(schedule.get().getStartTime()))
                    && (endTime.isBefore(schedule.get().getEndTime()) || endTime.isEqual(schedule.get().getEndTime()))) {
                if (reservations.isEmpty()) {
                    availableDates.add(new AvailableDate(startTime, endTime));
                } else {
                    boolean isAnyReservationInterfering = false;
                    for (ReservationEntity reservation : reservations) {
                        if (isReservationInterfering(startTime, endTime, reservation)) {
                            isAnyReservationInterfering = true;
                            break;
                        }
                    }
                    if (!isAnyReservationInterfering) {
                        availableDates.add(new AvailableDate(startTime, endTime));
                    }
                }
            }
            startTime = startTime.plusMinutes(15);
            endTime = startTime.plusMinutes(serviceDurationInMinutes);
        }

        return availableDates;
    }

    @Override
    @Transactional
    public String confirmReservation(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.findByToken(token);
        if (confirmationToken == null) {
            return "Invalid token";
        }
        if (confirmationToken.getConfirmedAt() != null) {
            return "Reservation already confirmed";
        }
        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return "Token expired at " + confirmationToken.getExpiresAt();
        }
        ReservationEntity reservation = confirmationToken.getReservation();
        reservation.updateStatus(ReservationStatus.CONFIRMED);
        confirmationTokenService.setConfirmedAt(token);
        reservationRepository.save(reservation);
        return "Reservation confirmed";
    }

    @Override
    public List<ReservationEntity> findCurrentWorkerReservations(Long workerId) {
        return workerService.findById(workerId).map(worker -> {
            if (userSecurity.isWorkerOrAdmin(worker.getEmail(), authenticationProvider.getAuthentication())) {
                return reservationRepository.findCurrentWorkerReservations(workerId);
            }
            throw new AccessDeniedException("Access denied to reservation list of worker " + workerId);
        }).orElseThrow(() -> new IllegalArgumentException("Worker with id " + workerId + " not found."));
    }

    @Override
    public List<ReservationEntity> findPastWorkerReservations(Long workerId) {
        return workerService.findById(workerId).map(worker -> {
            if (userSecurity.isWorkerOrAdmin(worker.getEmail(), authenticationProvider.getAuthentication())) {
                return reservationRepository.findPastWorkerReservations(workerId);
            }
            throw new AccessDeniedException("Access denied to reservation list of worker " + workerId);
        }).orElseThrow(() -> new IllegalArgumentException("Worker with id " + workerId + " not found."));
    }

    private boolean isReservationInterfering(LocalDateTime startTime, LocalDateTime endTime, ReservationEntity reservation) {
        return isReservationStartInBetween(startTime, endTime, reservation)
                || isReservationEndInBetween(startTime, endTime, reservation)
                || isReservationOverlapping(startTime, endTime, reservation);
    }

    private boolean isReservationStartInBetween(LocalDateTime startTime, LocalDateTime endTime, ReservationEntity reservation) {
        return (reservation.getStartTime().isAfter(startTime) || reservation.getStartTime().isEqual(startTime)) && reservation.getStartTime().isBefore(endTime);
    }

    private boolean isReservationEndInBetween(LocalDateTime startTime, LocalDateTime endTime, ReservationEntity reservation) {
        return reservation.getEndTime().isAfter(startTime) && (reservation.getEndTime().isBefore(endTime) || reservation.getEndTime().isEqual(endTime));
    }

    private boolean isReservationOverlapping(LocalDateTime startTime, LocalDateTime endTime, ReservationEntity reservation) {
        return (reservation.getStartTime().isEqual(startTime) || reservation.getStartTime().isBefore(startTime))
                && (reservation.getEndTime().isEqual(endTime) || reservation.getEndTime().isAfter(endTime));
    }

    @Override
    @Transactional
    public UpdateReservationStatusResponse updateReservationStatus(UpdateReservationStatusCommand command) {
        return reservationRepository.findById(command.getReservationId())
                .map(reservation -> {
                    if (hasPermission(command.getStatus(), authenticationProvider.getAuthentication(), reservation.getWorker().getEmail(), reservation.getClient().getEmail())) {
                        UpdateStatusResult result = reservation.updateStatus(command.getStatus());
                        reservationRepository.save(reservation);
                        return UpdateReservationStatusResponse.success(reservation.getStatus());
                    }
                    return UpdateReservationStatusResponse.failure(Error.FORBIDDEN);
                })
                .orElse(UpdateReservationStatusResponse.failure(Error.NOT_FOUND));
    }

    private boolean hasPermission(ReservationStatus status, Authentication user, String relatedWorker, String objectOwner) {
        if (userSecurity.isAdmin(user)) {
            return true;
        } else if (userSecurity.isRelatedWorker(relatedWorker, user) && status.equals(ReservationStatus.CANCELLED)) {
            return true;
        } else return userSecurity.isOwner(objectOwner, user)
                && (status.equals(ReservationStatus.CANCELLED) || status.equals(ReservationStatus.CONFIRMED));
    }

    public List<ReservationEntity> findAll() {
        return reservationRepository.findAll();
    }

    @Override
    public List<ReservationEntity> findByEmail(String email) {
        return reservationRepository.findByClientEmail(email);
    }

    @Override
    public Optional<ReservationEntity> findById(Long id) {
        return reservationRepository.findById(id)
                .map(reservation -> {
                    if (userSecurity.isOwnerWorkerOrAdmin(reservation.getClient().getEmail(), reservation.getWorker().getEmail(), authenticationProvider.getAuthentication())) {
                        return reservation;
                    }
                    throw new AccessDeniedException("Access denied to reservation: " + id);
                });
    }

    @Override
    public List<ReservationEntity> findByWorkerId(Long workerId) {
        return workerService.findById(workerId).map(worker -> {
            if (userSecurity.isWorkerOrAdmin(worker.getEmail(), authenticationProvider.getAuthentication())) {
                return reservationRepository.findByWorker_Id(workerId);
            }
            throw new AccessDeniedException("Access denied to reservation list of worker " + workerId);
        }).orElseThrow(() -> new IllegalArgumentException("Worker with id " + workerId + " not found."));
    }

    @Override
    public List<ReservationEntity> findByClientId(Long clientId) {
        return clientService.findById(clientId).map(client -> {
            if (userSecurity.isOwnerOrAdmin(client.getEmail(), authenticationProvider.getAuthentication())) {
                return reservationRepository.findByClient_Id(clientId);
            }
            throw new AccessDeniedException("Access denied to reservation list of client " + clientId);
        }).orElseThrow(() -> new IllegalArgumentException("Client with id " + clientId + " not found."));
    }

    private void prepareAuthentication() {
        Authentication auth = new UsernamePasswordAuthenticationToken(systemUser.getUsername(), systemUser.getPassword(), systemUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
