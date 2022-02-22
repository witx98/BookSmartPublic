package pl.mwitkowski.booksmart.schedule.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.mwitkowski.booksmart.schedule.application.port.ScheduleUseCase;
import pl.mwitkowski.booksmart.schedule.db.ScheduleRepository;
import pl.mwitkowski.booksmart.schedule.domain.ScheduleEntity;
import pl.mwitkowski.booksmart.security.UserSecurity;
import pl.mwitkowski.booksmart.security.userDetails.application.port.AuthenticationProviderUseCase;
import pl.mwitkowski.booksmart.worker.application.port.WorkerUseCase;
import pl.mwitkowski.booksmart.worker.domain.WorkerEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
class ScheduleService implements ScheduleUseCase {

    private final ScheduleRepository scheduleRepository;
    private final WorkerUseCase workerService;
    private final UserSecurity userSecurity;
    private final AuthenticationProviderUseCase authenticationProvider;

    @Override
    @Transactional
    public CreateScheduleResponse createSchedule(CreateScheduleCommand command) {
        Optional<WorkerEntity> worker = workerService.findById(command.getWorkerId());

        if (worker.isEmpty()) {
            return CreateScheduleResponse.failure("Worker with id: " + command.getWorkerId() + " doesn't exists.");
        }

        if (isAnyFieldNull(command.getDate(), command.getStartTime(), command.getEndTime(), command.getWorkerId())) {
            return CreateScheduleResponse.failure("Fields cannot be null");
        }

        if (!userSecurity.isWorkerOrAdmin(worker.get().getEmail(), authenticationProvider.getAuthentication())) {
            return CreateScheduleResponse.failure("Forbidden action");
        }

        if (command.getStartTime().isAfter(command.getEndTime())) {
            return CreateScheduleResponse.failure("Start time cannot be after end time.");
        }
        if (!command.getStartTime().toLocalDate().equals(command.getEndTime().toLocalDate())
                || !command.getStartTime().toLocalDate().equals(command.getDate())) {
            return CreateScheduleResponse.failure("Start time and end time have to be on same day.");
        }
        if (command.getStartTime().isBefore(LocalDateTime.now())) {
            return CreateScheduleResponse.failure("Cannot create schedule for past date.");
        }
        if (scheduleRepository.findByDateAndWorker_Id(command.getDate(), command.getWorkerId()).isPresent()) {
            return CreateScheduleResponse.failure("Worker with id: " + command.getWorkerId()
                    + " already has schedule for this date: " + command.getDate());
        }

        ScheduleEntity schedule = ScheduleEntity.builder()
                .date(command.getDate())
                .startTime(command.getStartTime())
                .endTime(command.getEndTime())
                .worker(worker.get())
                .build();

        return CreateScheduleResponse.success(scheduleRepository.save(schedule).getId());
    }

    @Override
    @Transactional
    public UpdateScheduleResponse updateSchedule(UpdateScheduleCommand command) {
        if (command.getId() == null || isAnyFieldNull(command.getDate(), command.getStartTime(), command.getEndTime(), command.getWorkerId())) {
            return UpdateScheduleResponse.failure("Fields cannot be null");
        }

        Optional<WorkerEntity> worker = workerService.findById(command.getWorkerId());

        if (worker.isEmpty()) {
            return UpdateScheduleResponse.failure("Worker with id: " + command.getWorkerId() + " doesn't exists.");
        }

        if (!userSecurity.isWorkerOrAdmin(worker.get().getEmail(), authenticationProvider.getAuthentication())) {
            return UpdateScheduleResponse.failure("Forbidden action");
        }

        if (command.getStartTime().isAfter(command.getEndTime())) {
            return UpdateScheduleResponse.failure("Start time cannot be after end time.");
        }
        if (command.getStartTime().isBefore(LocalDateTime.now())) {
            return UpdateScheduleResponse.failure("Cannot create schedule for past date.");
        }
        if (command.getDate().isBefore(LocalDate.now())) {
            return UpdateScheduleResponse.failure("New date cannot be in the past: " + command.getDate());
        }

        if (!command.getStartTime().toLocalDate().equals(command.getEndTime().toLocalDate())
                || !command.getStartTime().toLocalDate().equals(command.getDate())) {
            return UpdateScheduleResponse.failure("Start time and end time have to be on same day.");
        }

        Optional<ScheduleEntity> schedule = scheduleRepository.findById(command.getId());

        if (schedule.isEmpty()) {
            return UpdateScheduleResponse.failure("Schedule with id: " + command.getId() + "doesn't exist.");
        }
        if (schedule.get().getDate().isBefore(LocalDate.now())) {
            return UpdateScheduleResponse.failure("Modified schedule cannot be in the past");
        }

        return UpdateScheduleResponse.success(updateFields(command, schedule.get()).getId());
    }

    @Override
    public void deleteById(Long id) {
        ScheduleEntity schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule with id: " + id + "not found"));

        if (userSecurity.isWorkerOrAdmin(schedule.getWorker().getEmail(), authenticationProvider.getAuthentication())) {
            scheduleRepository.deleteById(id);
        }
        throw new IllegalStateException("Forbidden action - unauthorized deletion attempt");
    }

    @Override
    public List<ScheduleEntity> findAllCurrentWorkerSchedules(Long workerId) {
        return scheduleRepository.findCurrentWorkerSchedules(workerId);
    }

    @Override
    public List<List<ScheduleEntity>> findAllCurrentCompanySchedules(Long companyId) {
        List<WorkerEntity> companyWorkers = workerService.findByCompanyId(companyId);

        return companyWorkers.stream()
                .map(worker -> findAllCurrentWorkerSchedules(worker.getId()))
                .collect(Collectors.toList());

    }

    @Override
    public Optional<ScheduleEntity> findWorkerScheduleForDate(Long workerId, LocalDate date) {
        return scheduleRepository.findByWorker_IdAndDate(workerId, date);
    }

    @Override
    public Optional<ScheduleEntity> findById(Long id) {
        return scheduleRepository.findById(id);
    }

    private ScheduleEntity updateFields(UpdateScheduleCommand command, ScheduleEntity schedule) {
        schedule.setDate(command.getDate());
        schedule.setStartTime(command.getStartTime());
        schedule.setEndTime(command.getEndTime());
        return schedule;
    }

    private boolean isAnyFieldNull(LocalDate date, LocalDateTime startTime, LocalDateTime endTime, Long workerId) {
        return date == null || startTime == null || endTime == null || workerId == null;
    }
}
