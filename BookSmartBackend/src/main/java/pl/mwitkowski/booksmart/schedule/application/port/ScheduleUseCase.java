package pl.mwitkowski.booksmart.schedule.application.port;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import pl.mwitkowski.booksmart.commons.Either;
import pl.mwitkowski.booksmart.schedule.domain.ScheduleEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleUseCase {

    CreateScheduleResponse createSchedule(CreateScheduleCommand command);

    UpdateScheduleResponse updateSchedule(UpdateScheduleCommand command);

    void deleteById(Long id);

    List<ScheduleEntity> findAllCurrentWorkerSchedules(Long workerId);

    List<List<ScheduleEntity>> findAllCurrentCompanySchedules(Long companyId);

    Optional<ScheduleEntity> findWorkerScheduleForDate(Long workerId, LocalDate date);

    Optional<ScheduleEntity> findById(Long id);

    @Value
    class CreateScheduleCommand {
        LocalDate date;
        LocalDateTime startTime;
        LocalDateTime endTime;
        Long workerId;
    }

    class CreateScheduleResponse extends Either<String, Long> {

        CreateScheduleResponse(boolean success, String left, Long right) {
            super(success, left, right);
        }

        public static CreateScheduleResponse success(Long scheduleId) {
            return new CreateScheduleResponse(true, null, scheduleId);
        }

        public static CreateScheduleResponse failure(String error) {
            return new CreateScheduleResponse(false, error, null);
        }
    }

    @Value
    @Builder
    @AllArgsConstructor
    class UpdateScheduleCommand {
        Long id;
        LocalDate date;
        LocalDateTime startTime;
        LocalDateTime endTime;
        Long workerId;
    }

    class UpdateScheduleResponse extends Either<String, Long> {

        UpdateScheduleResponse(boolean success, String left, Long right) {
            super(success, left, right);
        }

        public static UpdateScheduleResponse success(Long scheduleId) {
            return new UpdateScheduleResponse(true, null, scheduleId);
        }

        public static UpdateScheduleResponse failure(String error) {
            return new UpdateScheduleResponse(false, error, null);
        }
    }
}
