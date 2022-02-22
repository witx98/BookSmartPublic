package pl.mwitkowski.booksmart.commons.listeners;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import pl.mwitkowski.booksmart.commons.events.OnRegistrationCompleteEvent;
import pl.mwitkowski.booksmart.email.application.port.EmailSenderUseCase;
import pl.mwitkowski.booksmart.email.domain.EmailBuilder;
import pl.mwitkowski.booksmart.user.application.port.VerificationTokenUseCase;
import pl.mwitkowski.booksmart.user.domain.UserEntity;

import java.util.UUID;

@Component
@AllArgsConstructor
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private final EmailSenderUseCase emailSender;
    private final VerificationTokenUseCase verificationTokenService;

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        UserEntity user = event.getUser();
        String token = UUID.randomUUID().toString();
        verificationTokenService.createVerificationToken(user, token);

        String recipientAddress = user.getUsername();
        String subject = "Registration Confirmation";
        String confirmationUrl = "http://localhost:8080" + event.getAppUrl() + "/users/registrationConfirm?token=" + token;

        emailSender.send(recipientAddress, subject, EmailBuilder.buildRegistrationEmail(user.getUsername(), confirmationUrl));
    }
}
