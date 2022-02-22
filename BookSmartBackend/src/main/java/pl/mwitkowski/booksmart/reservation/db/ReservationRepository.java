package pl.mwitkowski.booksmart.reservation.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.mwitkowski.booksmart.reservation.domain.ReservationEntity;
import pl.mwitkowski.booksmart.reservation.domain.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    @Query("SELECT DISTINCT r FROM ReservationEntity r WHERE " +
            "r.worker.id = :workerId " +
            "AND ((r.startTime BETWEEN :startTime AND :endTime) " +
            "   OR (r.endTime BETWEEN :startTime AND :endTime)) " +
            "AND (r.status <> :cancelled OR r.status <> :abandoned)")
    List<ReservationEntity> findReservationsInBetweenDates(@Param("workerId") Long workerId,
                                                           @Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime,
                                                           @Param("cancelled") ReservationStatus cancelled,
                                                           @Param("abandoned") ReservationStatus abandoned);

    @Query("SELECT r FROM ReservationEntity r WHERE " +
            "r.worker.id = :workerId " +
            "AND ((r.startTime  <= :startTime) " +
            "AND (r.endTime >= :endTime)) " +
            "AND (r.status <> :cancelled OR r.status <> :abandoned)")
    Optional<ReservationEntity> findOverlappingReservation(@Param("workerId") Long workerId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime,
                                                           @Param("cancelled") ReservationStatus cancelled,
                                                           @Param("abandoned") ReservationStatus abandoned);


    List<ReservationEntity> findByStatusAndCreatedAtLessThanEqual(ReservationStatus status, LocalDateTime date);

    List<ReservationEntity> findByStatusAndEndTimeLessThanEqual(ReservationStatus status, LocalDateTime date);

    @Query("SELECT r FROM ReservationEntity r JOIN r.client c WHERE c.email = :email")
    List<ReservationEntity> findByClientEmail(String email);

    List<ReservationEntity> findByWorker_Id(Long workerId);

    List<ReservationEntity> findByClient_Id(Long workerId);


    @Query("SELECT distinct r FROM ReservationEntity r WHERE r.worker.id = :workerId " +
            "AND r.endTime >= current_timestamp " +
            "AND (r.status = 'NEW' OR r.status = 'CONFIRMED') ")
    List<ReservationEntity> findCurrentWorkerReservations(@Param("workerId") Long workerId);

    @Query("SELECT distinct r FROM ReservationEntity r WHERE r.worker.id = :workerId " +
            "AND r.endTime < current_timestamp " +
            "OR (r.status <> 'NEW' AND r.status <> 'CONFIRMED') ")
    List<ReservationEntity> findPastWorkerReservations(@Param("workerId") Long workerId);
}
