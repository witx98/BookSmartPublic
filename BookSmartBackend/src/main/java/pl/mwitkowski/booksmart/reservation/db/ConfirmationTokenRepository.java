package pl.mwitkowski.booksmart.reservation.db;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mwitkowski.booksmart.reservation.domain.ConfirmationToken;

import java.util.Optional;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {

    Optional<ConfirmationToken> findByToken(String token);
}
