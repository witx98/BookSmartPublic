package pl.mwitkowski.booksmart.schedule.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import pl.mwitkowski.booksmart.commons.CreatedURI;
import pl.mwitkowski.booksmart.schedule.application.port.ScheduleUseCase;
import pl.mwitkowski.booksmart.schedule.application.port.ScheduleUseCase.CreateScheduleCommand;
import pl.mwitkowski.booksmart.schedule.application.port.ScheduleUseCase.UpdateScheduleCommand;
import pl.mwitkowski.booksmart.schedule.domain.ScheduleEntity;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/schedules")
public class ScheduleController {

    private final ScheduleUseCase scheduleService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RestSchedule> getById(@PathVariable Long id) {
        return scheduleService
                .findById(id)
                .map(this::toRestSchedule)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/worker/{workerId}")
    @ResponseStatus(HttpStatus.OK)
    public List<RestSchedule> getCurrentWorkerSchedules(@PathVariable Long workerId) {
        return scheduleService.findAllCurrentWorkerSchedules(workerId)
                .stream().map(this::toRestSchedule)
                .collect(Collectors.toList());
    }

    private RestSchedule toRestSchedule(ScheduleEntity schedule) {
        return new RestSchedule(
                schedule.getId(),
                schedule.getDate(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getWorker().getId()
        );
    }

    @Secured({"ROLE_ADMIN", "ROLE_WORKER"})
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createSchedule(@Valid @RequestBody RestScheduleCommand command) {
        return scheduleService
                .createSchedule(command.toCreateCommand())
                .handle(
                        scheduleId -> ResponseEntity.created(createURI(scheduleId)).build(),
                        error -> ResponseEntity.badRequest().body(error)
                );
    }

    URI createURI(Long id) {
        return new CreatedURI("/" + id).uri();
    }

    @Secured({"ROLE_ADMIN", "ROLE_WORKER"})
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateSchedule(@PathVariable Long id, @RequestBody RestScheduleCommand command) {
        scheduleService.updateSchedule(command.toUpdateCommand(id))
                .handle(
                        scheduleId -> ResponseEntity.accepted().build(),
                        error -> ResponseEntity.badRequest().body(error)
                );
    }

    @GetMapping("/company/{companyId}")
    @ResponseStatus(HttpStatus.OK)
    public List<List<ScheduleEntity>> getCurrentCompanySchedules(@PathVariable Long companyId) {
        return scheduleService.findAllCurrentCompanySchedules(companyId);
    }

    @Secured({"ROLE_ADMIN", "ROLE_WORKER"})
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSchedule(@PathVariable Long id) {
        scheduleService.deleteById(id);
    }

    @Data
    private static class RestScheduleCommand {

        @NotNull
        @NotBlank
        private Long workerId;

        @NotNull
        @NotBlank
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate date;

        @NotNull
        @NotBlank
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime startTime;

        @NotNull
        @NotBlank
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime endTime;

        public CreateScheduleCommand toCreateCommand() {
            return new CreateScheduleCommand(date, startTime, endTime, workerId);
        }

        public UpdateScheduleCommand toUpdateCommand(Long id) {
            return new UpdateScheduleCommand(id, date, startTime, endTime, workerId);
        }
    }

}
