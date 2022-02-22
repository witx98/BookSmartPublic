package pl.mwitkowski.booksmart.reservation.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.mwitkowski.booksmart.jpa.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
public class ConfirmationToken extends BaseEntity {

    private static final int EXPIRATION = 15;
    private String token;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "reservation_id")
    private ReservationEntity reservation;
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime confirmedAt;

    public ConfirmationToken(String token, ReservationEntity reservation) {
        this.token = token;
        this.reservation = reservation;
        this.expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION);
    }
}
