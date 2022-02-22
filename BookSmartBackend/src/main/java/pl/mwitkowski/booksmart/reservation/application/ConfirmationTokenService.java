package pl.mwitkowski.booksmart.reservation.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.mwitkowski.booksmart.reservation.application.port.ConfirmationTokenUseCase;
import pl.mwitkowski.booksmart.reservation.db.ConfirmationTokenRepository;
import pl.mwitkowski.booksmart.reservation.domain.ConfirmationToken;
import pl.mwitkowski.booksmart.reservation.domain.ReservationEntity;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ConfirmationTokenService implements ConfirmationTokenUseCase {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    @Override
    public ConfirmationToken findByToken(String token) {
        return confirmationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
    }

    @Override
    @Transactional
    public void createConfirmationToken(ReservationEntity reservation, String token) {
        confirmationTokenRepository.save(new ConfirmationToken(token, reservation));
    }

    @Override
    @Transactional
    public void setConfirmedAt(String token) {
        confirmationTokenRepository.findByToken(token)
                .map(confirmationToken -> {
                    confirmationToken.setConfirmedAt(LocalDateTime.now());
                    return confirmationTokenRepository.save(confirmationToken);
                }).orElseThrow(() -> new IllegalArgumentException("Invalid token"));
    }
}
