package pl.mwitkowski.booksmart.reservation.web;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.mwitkowski.booksmart.client.application.port.ClientUseCase.CreateClientCommand;
import pl.mwitkowski.booksmart.client.domain.ClientEntity;
import pl.mwitkowski.booksmart.client.web.RestClient;
import pl.mwitkowski.booksmart.commons.CreatedURI;
import pl.mwitkowski.booksmart.commons.events.OnReservationCompleteEvent;
import pl.mwitkowski.booksmart.reservation.application.port.ReservationUseCase;
import pl.mwitkowski.booksmart.reservation.application.port.ReservationUseCase.AvailableDate;
import pl.mwitkowski.booksmart.reservation.application.port.ReservationUseCase.CreateReservationCommand;
import pl.mwitkowski.booksmart.reservation.application.port.ReservationUseCase.UpdateReservationStatusCommand;
import pl.mwitkowski.booksmart.reservation.domain.ReservationEntity;
import pl.mwitkowski.booksmart.reservation.domain.ReservationStatus;
import pl.mwitkowski.booksmart.service.domain.ServiceEntity;
import pl.mwitkowski.booksmart.service.web.RestService;
import pl.mwitkowski.booksmart.worker.domain.WorkerEntity;
import pl.mwitkowski.booksmart.worker.web.RestWorker;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/reservations")
public class ReservationController {


    private final ReservationUseCase reservationService;
    private final ApplicationEventPublisher eventPublisher;

    @Secured({"ROLE_ADMIN"})
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RestReservation> getAll(@RequestParam Optional<String> email, HttpServletRequest request) {
        List<ReservationEntity> reservations;
        if (email.isPresent()) {
            reservations = reservationService.findByEmail(email.get());
        } else {
            reservations = reservationService.findAll();
        }
        return reservations.stream()
                .map(reservation -> toRestReservation(reservation, request))
                .collect(Collectors.toList());
    }

    private RestReservation toRestReservation(ReservationEntity reservation, HttpServletRequest request) {
        return new RestReservation(
                reservation.getId(),
                toRestWorker(reservation.getWorker(), request),
                toRestClient(reservation.getClient()),
                toRestService(reservation.getService()),
                reservation.getStatus(),
                reservation.getStartTime(),
                reservation.getEndTime()
        );
    }

    private RestService toRestService(ServiceEntity service) {
        return new RestService(
                service.getId(),
                service.getServiceName(),
                service.getDuration().toMinutes(),
                service.getPrice(),
                service.getKeywords()

        );
    }

    private RestClient toRestClient(ClientEntity client) {
        return new RestClient(
                client.getId(),
                client.getFirstname(),
                client.getLastname(),
                client.getEmail(),
                client.getPhone()
        );
    }

    private RestWorker toRestWorker(WorkerEntity worker, HttpServletRequest request) {
        String pictureUrl = Optional.ofNullable(worker.getPictureId())
                .map(pictureId -> ServletUriComponentsBuilder
                        .fromContextPath(request)
                        .path("/uploads/{id}/file")
                        .build(pictureId)
                        .toASCIIString())
                .orElse(null);

        return new RestWorker(
                worker.getId(),
                worker.getFirstname(),
                worker.getLastname(),
                worker.getPhone(),
                worker.getEmail(),
                pictureUrl
        );
    }

    @Secured({"ROLE_ADMIN", "ROLE_WORKER", "ROLE_CLIENT"})
    @GetMapping("/{id}")
    public ResponseEntity<RestReservation> getById(@PathVariable Long id, HttpServletRequest request) {
        return reservationService
                .findById(id)
                .map(reservation -> toRestReservation(reservation, request))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Secured({"ROLE_ADMIN", "ROLE_CLIENT"})
    @GetMapping("/client/{clientId}")
    public List<RestReservation> getAllClientReservations(@PathVariable Long clientId, HttpServletRequest request) {
        return reservationService.findByClientId(clientId)
                .stream()
                .map(reservation -> toRestReservation(reservation, request))
                .collect(Collectors.toList());
    }


    @Secured({"ROLE_ADMIN", "ROLE_WORKER"})
    @GetMapping("/worker/{workerId}")
    public List<RestReservation> getAllWorkerReservations(@PathVariable Long workerId, HttpServletRequest request) {
        return reservationService.findByWorkerId(workerId)
                .stream()
                .map(reservation -> toRestReservation(reservation, request))
                .collect(Collectors.toList());
    }

    @Secured({"ROLE_ADMIN", "ROLE_WORKER"})
    @GetMapping("/worker/current/{workerId}")
    public List<RestReservation> getCurrentWorkerReservations(@PathVariable Long workerId, HttpServletRequest request) {
        return reservationService.findCurrentWorkerReservations(workerId)
                .stream()
                .map(reservation -> toRestReservation(reservation, request))
                .collect(Collectors.toList());
    }

    @Secured({"ROLE_ADMIN", "ROLE_WORKER"})
    @GetMapping("/worker/past/{workerId}")
    public List<RestReservation> getPastWorkerReservations(@PathVariable Long workerId, HttpServletRequest request) {
        return reservationService.findPastWorkerReservations(workerId)
                .stream()
                .map(reservation -> toRestReservation(reservation, request))
                .collect(Collectors.toList());
    }

    @PostMapping("/availableDates")
    public List<AvailableDate> getAvailableDates(@Valid @RequestBody RestAvailableDatesCommand command) {
        return reservationService.getAvailableReservationDates(
                command.getServiceId(),
                command.getWorkerId(),
                command.getSelectedDate()
        );
    }

    @PostMapping
    @ResponseStatus
    public ResponseEntity<Object> createReservation(HttpServletRequest request, @Valid @RequestBody RestReservationCommand command) {
        return reservationService
                .createReservation(command.toCreateCommand())
                .handle(
                        reservation -> {
                            eventPublisher.publishEvent(new OnReservationCompleteEvent(reservation, request.getContextPath()));
                            return ResponseEntity.created(createURI(reservation.getId())).build();
                        },
                        error -> ResponseEntity.badRequest().body(error)
                );
    }

    @GetMapping("/confirmReservation")
    public String confirmReservation(@RequestParam("token") String token) {
        return reservationService.confirmReservation(token);
    }

    URI createURI(Long id) {
        return new CreatedURI("/" + id).uri();
    }

    @Secured({"ROLE_ADMIN", "ROLE_WORKER", "ROLE_CLIENT"})
    @PatchMapping("/{id}/status")
    public ResponseEntity<Object> updateReservationStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        ReservationStatus reservationStatus = ReservationStatus
                .parseString(status)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown status: " + status));

        UpdateReservationStatusCommand command = new UpdateReservationStatusCommand(id, reservationStatus);
        return reservationService.updateReservationStatus(command).handle(
                newStatus -> ResponseEntity.accepted().build(),
                error -> ResponseEntity.status(error.getStatus()).build()
        );
    }

    @Data
    private static class RestReservationCommand {
        @NotNull
        @Future
        LocalDateTime startTime;

        @NotNull
        @Min(0)
        Long workerId;

        @NotNull
        CreateClientCommand createClientCommand;

        @NotNull
        @Min(0)
        Long serviceId;

        public CreateReservationCommand toCreateCommand() {
            return new CreateReservationCommand(startTime, workerId, createClientCommand, serviceId);
        }
    }

    @Data
    private static class RestAvailableDatesCommand {

        @NotNull
        @Min(0)
        Long workerId;

        @NotNull
        @Min(0)
        Long serviceId;

        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate selectedDate;

    }
}
