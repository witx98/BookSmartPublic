package pl.mwitkowski.booksmart.user.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.mwitkowski.booksmart.user.application.port.VerificationTokenUseCase;
import pl.mwitkowski.booksmart.user.db.VerificationTokenRepository;
import pl.mwitkowski.booksmart.user.domain.UserEntity;
import pl.mwitkowski.booksmart.user.domain.VerificationToken;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class VerificationTokenService implements VerificationTokenUseCase {

    private final VerificationTokenRepository verificationTokenRepository;


    @Override
    public UserEntity getUserByToken(String token) {
        return verificationTokenRepository.findByToken(token)
                .map(VerificationToken::getUser)
                .orElse(null);
    }

    @Override
    public VerificationToken findByToken(String token) {
        return verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
    }

    @Override
    @Transactional
    public void createVerificationToken(UserEntity user, String token) {
        verificationTokenRepository.save(new VerificationToken(token, user));
    }

    @Override
    @Transactional
    public void setConfirmedAt(String token) {
        verificationTokenRepository.findByToken(token)
                .map(verificationToken -> {
                    verificationToken.setConfirmedAt(LocalDateTime.now());
                    return verificationTokenRepository.save(verificationToken);
                }).orElseThrow(() -> new IllegalArgumentException("Invalid token"));
    }
}
