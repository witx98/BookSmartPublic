package pl.mwitkowski.booksmart.user.web;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import pl.mwitkowski.booksmart.commons.events.OnRegistrationCompleteEvent;
import pl.mwitkowski.booksmart.security.TokenUtil;
import pl.mwitkowski.booksmart.user.application.port.UserRegistrationUseCase;
import pl.mwitkowski.booksmart.user.application.port.UserRegistrationUseCase.RegisterClientCommand;
import pl.mwitkowski.booksmart.user.application.port.UserRegistrationUseCase.RegisterWorkerCommand;
import pl.mwitkowski.booksmart.user.domain.UserEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UsersController {

    private final UserRegistrationUseCase register;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/register")
    public ResponseEntity<?> register(HttpServletRequest request,
                                      @Valid @RequestBody RegisterClientAccountCommand command) {
        return register
                .register(command.toRegisterClientCommand())
                .handle(
                        userEntity -> {
                            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(userEntity, request.getContextPath()));
                            return ResponseEntity.accepted().build();
                        },
                        error -> ResponseEntity.badRequest().body(error)
                );
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/registerWorker")
    public ResponseEntity<?> registerWorker(@Valid @RequestBody RegisterWorkerAccountCommand command) {
        return register
                .registerWorker(command.toRegisterWorkerCommand())
                .handle(
                        userEntity -> ResponseEntity.accepted().build(),
                        error -> ResponseEntity.badRequest().body(error)
                );
    }

    @GetMapping("/registrationConfirm")
    public String confirmRegistration(@RequestParam("token") String token) {
        return register.confirmUser(token);
    }

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String refresh_token = authorizationHeader.substring("Bearer ".length());
                //TODO REFACTOR - utilClass
                JWTVerifier verifier = JWT.require(TokenUtil.getAlgorithm()).build();
                DecodedJWT decodedJWT = verifier.verify(refresh_token);
                String username = decodedJWT.getSubject();

                UserEntity user = register.getUser(username).orElseThrow(() -> new IllegalArgumentException("Unknown username: " + username));

                String access_token = JWT.create()
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + TokenUtil.ACCESS_TOKEN_EXPIRATION_TIME)) //TODO - refactor
                        .withIssuer(request.getRequestURL().toString())
                        .withClaim("roles", user.getRoles().stream().toList())
                        .sign(TokenUtil.getAlgorithm());

                Map<String, String> tokens = new HashMap<>();
                tokens.put("access_token", access_token);
                tokens.put("refresh_token", refresh_token);
                response.setContentType(APPLICATION_JSON_VALUE);
                mapper.writeValue(response.getOutputStream(), tokens);

            } catch (Exception exception) {
                response.setHeader("error", exception.getMessage());
                response.setStatus(FORBIDDEN.value());

                Map<String, String> error = new HashMap<>();
                error.put("error_message", exception.getMessage());
                response.setContentType(APPLICATION_JSON_VALUE);
                mapper.writeValue(response.getOutputStream(), error);
            }
        } else {
            throw new RuntimeException("Refresh token is missing");
        }
    }

    @Data
    static class RegisterWorkerAccountCommand {
        @Email
        String username;

        @Size(min = 3, max = 100)
        String password;

        @NotNull
        private Long companyId;

        @NotNull
        @NotBlank
        private String firstname;

        @NotNull
        @NotBlank
        private String lastname;

        @NotNull
        @NotBlank
        private String phone;

        public RegisterWorkerCommand toRegisterWorkerCommand() {
            return new RegisterWorkerCommand(username, password, companyId, firstname, lastname, phone);
        }
    }

    @Data
    static class RegisterClientAccountCommand {
        @Email
        String username;

        @Size(min = 3, max = 100)
        String password;

        @NotNull
        @NotBlank
        private String firstname;

        @NotNull
        @NotBlank
        private String lastname;

        @NotNull
        @NotBlank
        private String phone;

        public RegisterClientCommand toRegisterClientCommand() {
            return new RegisterClientCommand(username, password, firstname, lastname, phone);
        }
    }
}
