package pl.mwitkowski.booksmart.user.application.port;

import lombok.AllArgsConstructor;
import lombok.Value;
import pl.mwitkowski.booksmart.client.application.port.ClientUseCase.CreateClientCommand;
import pl.mwitkowski.booksmart.commons.Either;
import pl.mwitkowski.booksmart.user.domain.UserEntity;
import pl.mwitkowski.booksmart.worker.application.port.WorkerUseCase.CreateWorkerCommand;

import java.util.List;
import java.util.Optional;

public interface UserRegistrationUseCase {
    RegisterResponse register(RegisterClientCommand command);

    RegisterResponse registerWorker(RegisterWorkerCommand command);

    Optional<UserEntity> getUser(String username);

    List<UserEntity> getAllUsers();

    String confirmUser(String token);

    @Value
    @AllArgsConstructor
    class RegisterWorkerCommand {
        String username;
        String password;
        Long companyId;
        String firstname;
        String lastname;
        String phone;

        public CreateWorkerCommand toCreateWorkerCommand() {
            return new CreateWorkerCommand(firstname, lastname, phone, username, companyId);
        }
    }

    @Value
    @AllArgsConstructor
    class RegisterClientCommand {
        String username;
        String password;
        String firstname;
        String lastname;
        String phone;

        public CreateClientCommand toCreateClientCommand() {
            return new CreateClientCommand(firstname, lastname, phone, username);
        }
    }

    class RegisterResponse extends Either<String, UserEntity> {
        public RegisterResponse(boolean success, String left, UserEntity right) {
            super(success, left, right);
        }

        public static RegisterResponse success(UserEntity right) {
            return new RegisterResponse(true, null, right);
        }

        public static RegisterResponse failure(String left) {
            return new RegisterResponse(false, left, null);

        }
    }
}
