package pl.mwitkowski.booksmart.security.userDetails.application.port;

import org.springframework.security.core.Authentication;

public interface AuthenticationProviderUseCase {

    Authentication getAuthentication();
}
