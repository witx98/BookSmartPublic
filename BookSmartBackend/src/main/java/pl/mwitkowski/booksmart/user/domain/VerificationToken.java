package pl.mwitkowski.booksmart.user.domain;

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
public class VerificationToken extends BaseEntity {

    private static final int EXPIRATION = 60 * 20;
    private String token;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private UserEntity user;


    public VerificationToken(String token, UserEntity user) {
        this.token = token;
        this.user = user;
        this.expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION);
    }

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private LocalDateTime confirmedAt;
}
