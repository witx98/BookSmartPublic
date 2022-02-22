package pl.mwitkowski.booksmart.user.application.port;

import pl.mwitkowski.booksmart.user.domain.UserEntity;
import pl.mwitkowski.booksmart.user.domain.VerificationToken;

public interface VerificationTokenUseCase {

    UserEntity getUserByToken(String token);

    VerificationToken findByToken(String token);

    void createVerificationToken(UserEntity user, String token);

    void setConfirmedAt(String token);
}
