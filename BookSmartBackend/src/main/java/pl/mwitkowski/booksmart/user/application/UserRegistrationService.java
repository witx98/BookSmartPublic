package pl.mwitkowski.booksmart.user.application;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.mwitkowski.booksmart.client.application.port.ClientUseCase;
import pl.mwitkowski.booksmart.client.application.port.ClientUseCase.CreateClientResponse;
import pl.mwitkowski.booksmart.user.application.port.UserRegistrationUseCase;
import pl.mwitkowski.booksmart.user.application.port.VerificationTokenUseCase;
import pl.mwitkowski.booksmart.user.db.UserEntityRepository;
import pl.mwitkowski.booksmart.user.domain.UserEntity;
import pl.mwitkowski.booksmart.user.domain.VerificationToken;
import pl.mwitkowski.booksmart.worker.application.port.WorkerUseCase;
import pl.mwitkowski.booksmart.worker.application.port.WorkerUseCase.CreateWorkerResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class UserRegistrationService implements UserRegistrationUseCase {

    private final WorkerUseCase workerService;
    private final ClientUseCase clientService;
    private final UserEntityRepository userEntityRepository;
    private final PasswordEncoder encoder;
    private final VerificationTokenUseCase verificationTokenService;
    private final UserDetails systemUser;


    @Override
    @Transactional
    public RegisterResponse register(RegisterClientCommand command) {
        if (userEntityRepository.findByUsernameIgnoreCase(command.getUsername()).isPresent()) {
            return RegisterResponse.failure("Account already exists.");
        }
        UserEntity entity = new UserEntity(command.getUsername(), encoder.encode(command.getPassword()), "ROLE_CLIENT");
        prepareAuthentication();
        CreateClientResponse response = clientService.getOrCreateClient(command.toCreateClientCommand());

        if (response.isSuccess()) {
            log.info("Saving new client User: " + entity.getUsername());
            return RegisterResponse.success(userEntityRepository.save(entity));
        }
        return RegisterResponse.failure(response.getLeft());
    }

    @Override
    @Transactional
    public RegisterResponse registerWorker(RegisterWorkerCommand command) {
        if (userEntityRepository.findByUsernameIgnoreCase(command.getUsername()).isPresent()) {
            return RegisterResponse.failure("Account already exists.");
        }
        UserEntity entity = new UserEntity(command.getUsername(), encoder.encode(command.getPassword()), true ,"ROLE_WORKER");
        CreateWorkerResponse response = workerService.getOrCreateWorker(command.toCreateWorkerCommand());

        if (response.isSuccess()) {
            log.info("Saving new worker User: " + entity.getUsername());
            return RegisterResponse.success(userEntityRepository.save(entity));
        }
        return RegisterResponse.failure(response.getLeft());
    }

    @Override
    public Optional<UserEntity> getUser(String username) {
        return userEntityRepository.findByUsernameIgnoreCase(username);
    }

    @Override
    public List<UserEntity> getAllUsers() {
        return userEntityRepository.findAll();
    }

    @Override
    @Transactional
    public String confirmUser(String token) {
        VerificationToken verificationToken = verificationTokenService.findByToken(token);
        if (verificationToken == null) {
            return "Invalid token";
        }
        if (verificationToken.getConfirmedAt() != null) {
            return "Email already confirmed";
        }
        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return "Token expired at " + verificationToken.getExpiresAt();
        }
        UserEntity user = verificationToken.getUser();
        user.setEnabled(true);
        verificationTokenService.setConfirmedAt(token);
        userEntityRepository.save(user);
        return "Email confirmed";
    }

    private void prepareAuthentication() {
        Authentication auth = new UsernamePasswordAuthenticationToken(systemUser.getUsername(), systemUser.getPassword(), systemUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
