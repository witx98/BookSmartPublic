package pl.mwitkowski.booksmart.client.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import pl.mwitkowski.booksmart.client.application.port.ClientUseCase;
import pl.mwitkowski.booksmart.client.application.port.ClientUseCase.CreateClientCommand;
import pl.mwitkowski.booksmart.client.application.port.ClientUseCase.UpdateClientCommand;
import pl.mwitkowski.booksmart.client.domain.ClientEntity;
import pl.mwitkowski.booksmart.commons.CreatedURI;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientUseCase clientService;

    @Secured({"ROLE_ADMIN"})
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RestClient> getAll() {
        return clientService.findAll()
                .stream().map(this::toRestClient)
                .collect(Collectors.toList());
    }

    @Secured({"ROLE_ADMIN", "ROLE_CLIENT"})
    @GetMapping("/{id}")
    public ResponseEntity<RestClient> getById(@PathVariable Long id) {
        return clientService
                .findById(id)
                .map(this::toRestClient)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Secured({"ROLE_ADMIN", "ROLE_CLIENT"})
    @GetMapping("/byEmail/{email}")
    public ResponseEntity<RestClient> getByEmail(@PathVariable String email) {
        return clientService
                .findByEmail(email)
                .map(this::toRestClient)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Secured({"ROLE_ADMIN", "ROLE_CLIENT"})
    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateClientData(@PathVariable Long id, @RequestBody RestClientCommand command) {
        return clientService.updateClient(command.toUpdateCommand(id)).handle(
                clientId -> ResponseEntity.accepted().build(),
                error -> ResponseEntity.badRequest().body(error)
        );
    }

    @PostMapping
    public ResponseEntity<Object> createOrGetClient(@Valid @RequestBody RestClientCommand command) {
        return clientService.getOrCreateClient(command.toCreateClientCommand())
                .handle(
                        clientId -> ResponseEntity.created(createURI(clientId)).build(),
                        error -> ResponseEntity.badRequest().body(error)
                );

    }

    URI createURI(Long id) {
        return new CreatedURI("/" + id).uri();
    }

    private RestClient toRestClient(ClientEntity client) {
        return new RestClient(
                client.getId(),
                client.getFirstname(),
                client.getLastname(),
                client.getEmail(),
                client.getPhone()
        );
    }

    @Data
    private static class RestClientCommand {
        @NotNull
        @NotBlank
        private String firstname;

        @NotNull
        @NotBlank
        private String lastname;

        @NotNull
        @NotBlank
        private String phone;

        @NotNull
        @NotBlank
        @Email
        private String email;

        CreateClientCommand toCreateClientCommand() {
            return new CreateClientCommand(firstname, lastname, phone, email);
        }

        UpdateClientCommand toUpdateCommand(Long id) {
            return new UpdateClientCommand(id, firstname, lastname, phone);
        }
    }
}
