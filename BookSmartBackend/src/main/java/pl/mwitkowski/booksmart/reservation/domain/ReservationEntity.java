package pl.mwitkowski.booksmart.reservation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.mwitkowski.booksmart.client.domain.ClientEntity;
import pl.mwitkowski.booksmart.jpa.BaseEntity;
import pl.mwitkowski.booksmart.service.domain.ServiceEntity;
import pl.mwitkowski.booksmart.worker.domain.WorkerEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Builder
@Entity
@Table(name = "reservations")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ReservationEntity extends BaseEntity {

    private LocalDateTime startTime;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private WorkerEntity worker;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private ClientEntity client;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private ServiceEntity service;

    private LocalDateTime endTime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.NEW;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public UpdateStatusResult updateStatus(ReservationStatus newStatus) {
        UpdateStatusResult result = this.status.updateStatus(newStatus);
        this.status = result.getNewStatus();
        return result;
    }


}
