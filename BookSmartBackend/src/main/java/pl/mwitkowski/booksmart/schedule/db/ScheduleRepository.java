package pl.mwitkowski.booksmart.schedule.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.mwitkowski.booksmart.schedule.domain.ScheduleEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {

    Optional<ScheduleEntity> findByDateAndWorker_Id(LocalDate date, Long workerId);

    List<ScheduleEntity> findByStartTimeAfterAndWorker_Id(LocalDateTime startTime, Long workerId);

    @Query("SELECT DISTINCT s FROM ScheduleEntity s WHERE s.worker.id = :workerId AND s.startTime >= CURRENT_TIMESTAMP")
    List<ScheduleEntity> findCurrentWorkerSchedules(@Param("workerId") Long workerId);

    Optional<ScheduleEntity> findByWorker_IdAndDate(Long workerId, LocalDate date);
}
