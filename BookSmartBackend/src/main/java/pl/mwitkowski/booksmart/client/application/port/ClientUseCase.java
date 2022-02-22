package pl.mwitkowski.booksmart.client.application.port;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import pl.mwitkowski.booksmart.client.domain.ClientEntity;
import pl.mwitkowski.booksmart.commons.Either;

import java.util.List;
import java.util.Optional;

public interface ClientUseCase {

    CreateClientResponse getOrCreateClient(CreateClientCommand command);

    CreateClientResponse createClient(CreateClientCommand command);

    UpdateClientResponse updateClient(UpdateClientCommand command);

    Optional<ClientEntity> findById(Long id);

    List<ClientEntity> findAll();

    Optional<ClientEntity> findByEmail(String email);


    @Value
    class CreateClientCommand {
        String firstname;
        String lastname;
        String phone;
        String email;
    }

    @Value
    @Builder
    @AllArgsConstructor
    class UpdateClientCommand {
        Long id;
        String firstname;
        String lastname;
        String phone;
    }


    class CreateClientResponse extends Either<String, Long> {

        CreateClientResponse(boolean success, String left, Long right) {
            super(success, left, right);
        }

        public static CreateClientResponse success(Long clientId) {
            return new CreateClientResponse(true, null, clientId);
        }

        public static CreateClientResponse failure(String error) {
            return new CreateClientResponse(false, error, null);
        }

    }

    class UpdateClientResponse extends Either<String, Long> {
        UpdateClientResponse(boolean success, String left, Long right) {
            super(success, left, right);
        }

        public static UpdateClientResponse success(Long clientId) {
            return new UpdateClientResponse(true, null, clientId);
        }

        public static UpdateClientResponse failure(String error) {
            return new UpdateClientResponse(false, error, null);
        }
    }
}
