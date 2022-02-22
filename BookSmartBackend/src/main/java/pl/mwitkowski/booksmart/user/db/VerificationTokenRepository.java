package pl.mwitkowski.booksmart.user.db;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mwitkowski.booksmart.user.domain.VerificationToken;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
}
