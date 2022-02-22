package pl.mwitkowski.booksmart.client.application;

import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.mwitkowski.booksmart.client.application.port.ClientUseCase;
import pl.mwitkowski.booksmart.client.db.ClientRepository;
import pl.mwitkowski.booksmart.client.domain.ClientEntity;
import pl.mwitkowski.booksmart.security.UserSecurity;
import pl.mwitkowski.booksmart.security.userDetails.application.port.AuthenticationProviderUseCase;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
class ClientService implements ClientUseCase {

    private final ClientRepository clientRepository;
    private final UserSecurity userSecurity;
    private final AuthenticationProviderUseCase authenticationProvider;


    @Override
    @Transactional
    public CreateClientResponse getOrCreateClient(CreateClientCommand command) {
        return clientRepository
                .findByEmailIgnoreCase(command.getEmail())
                .map(client -> {
                    if (userSecurity.isOwnerOrAdmin(client.getEmail(), authenticationProvider.getAuthentication())) {
                        return CreateClientResponse.success(client.getId());
                    }
                    return CreateClientResponse.failure("Forbidden action - cannot get client with email: " + command.getEmail());
                })
                .orElseGet(() -> createClient(command));
    }

    @Override
    @Transactional
    public CreateClientResponse createClient(CreateClientCommand command) {
        ClientEntity client = new ClientEntity(command.getFirstname(), command.getLastname(), command.getEmail(), command.getPhone());
        return CreateClientResponse.success(clientRepository.save(client).getId());
    }

    @Override
    @Transactional
    public UpdateClientResponse updateClient(UpdateClientCommand command) {
        return clientRepository.findById(command.getId())
                .map(client -> {
                    if (userSecurity.isOwnerOrAdmin(client.getEmail(), authenticationProvider.getAuthentication())) {
                        UpdateClientResponse response = updateFields(command, client);
                        clientRepository.save(client);
                        return response;
                    }
                    return UpdateClientResponse.failure("Forbidden action - update client with id: " + command.getId());
                })
                .orElse(UpdateClientResponse.failure("Client not found with id: " + command.getId()));
    }

    @Override
    public Optional<ClientEntity> findById(Long id) {
        return clientRepository.findById(id)
                .map(client -> {
                    if (userSecurity.isOwnerOrAdmin(client.getEmail(), authenticationProvider.getAuthentication())) {
                        return client;
                    }
                    throw new AccessDeniedException("Forbidden access to client: " + id);
                });
    }

    @Override
    public List<ClientEntity> findAll() {
        if (userSecurity.isAdmin(authenticationProvider.getAuthentication())) {
            return clientRepository.findAll();
        }
        throw new AccessDeniedException("Forbidden access ");
    }

    @Override
    public Optional<ClientEntity> findByEmail(String email) {
        return clientRepository.findByEmailIgnoreCase(email)
                .map(client -> {
                    if (userSecurity.isOwnerOrAdmin(client.getEmail(), authenticationProvider.getAuthentication())) {
                        return client;
                    }
                    throw new AccessDeniedException("Forbidden access to client: " + email);
                });
    }

    private UpdateClientResponse updateFields(UpdateClientCommand command, ClientEntity client) {
        if (command.getPhone() != null) {
            client.setPhone(command.getPhone());
        }
        if (command.getFirstname() != null) {
            client.setPhone(command.getPhone());
        }
        if (command.getLastname() != null) {
            client.setPhone(command.getPhone());
        }
        return UpdateClientResponse.success(client.getId());
    }
}
