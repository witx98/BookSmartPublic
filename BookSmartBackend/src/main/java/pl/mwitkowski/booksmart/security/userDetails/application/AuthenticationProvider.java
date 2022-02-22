package pl.mwitkowski.booksmart.security.userDetails.application;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pl.mwitkowski.booksmart.security.userDetails.application.port.AuthenticationProviderUseCase;

@Component
public class AuthenticationProvider implements AuthenticationProviderUseCase {

    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

}
