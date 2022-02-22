package pl.mwitkowski.booksmart.service.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import pl.mwitkowski.booksmart.commons.CreatedURI;
import pl.mwitkowski.booksmart.service.application.port.ServiceEntityUseCase;
import pl.mwitkowski.booksmart.service.application.port.ServiceEntityUseCase.CreateServiceCommand;
import pl.mwitkowski.booksmart.service.application.port.ServiceEntityUseCase.UpdateServiceCommand;
import pl.mwitkowski.booksmart.service.domain.ServiceEntity;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/services")
public class ServiceEntityController {
    private final ServiceEntityUseCase service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RestService> getAll(@RequestParam Optional<Long> companyId, @RequestParam Optional<String> serviceName) {
        List<ServiceEntity> services;
        if (companyId.isPresent() && serviceName.isPresent() && !serviceName.get().isBlank()) {
            services = service.findCompanyServicesContainingName(companyId.get(), serviceName.get());
        } else if (companyId.isPresent()) {
            services = service.findCompanyServices(companyId.get());
        } else {
            services = service.findAll();
        }
        return services.stream()
                .map(this::toRestService)
                .collect(Collectors.toList());
    }

    private RestService toRestService(ServiceEntity serviceEntity) {
        return new RestService(
                serviceEntity.getId(),
                serviceEntity.getServiceName(),
                serviceEntity.getDuration().toMinutes(),
                serviceEntity.getPrice(),
                serviceEntity.getKeywords()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestService> getById(@PathVariable Long id) {
        return service
                .findById(id)
                .map(this::toRestService)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Secured({"ROLE_ADMIN", "ROLE_WORKER"})
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createService(@Valid @RequestBody RestServiceCommand command) {
        return service
                .createServiceEntity(command.toUpdateCommand())
                .handle(
                        serviceId -> ResponseEntity.created(createURI(serviceId)).build(),
                        error -> ResponseEntity.badRequest().body(error)
                );
    }

    URI createURI(Long id) {
        return new CreatedURI("/" + id).uri();
    }

    @Secured({"ROLE_ADMIN", "ROLE_WORKER"})
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateService(@PathVariable Long id, @RequestBody RestServiceCommand command) {
        service.updateServiceEntity(command.toUpdateCommand(id))
                .handle(
                        serviceId -> ResponseEntity.accepted().build(),
                        error -> ResponseEntity.badRequest().body(error)
                );
    }

    @Data
    private static class RestServiceCommand {

        @NotNull
        private Long companyId;

        @NotNull
        @NotBlank
        private String serviceName;

        @NotNull
        @NotBlank
        private Duration duration;

        @NotNull
        @NotBlank
        @DecimalMin("0.00")
        private BigDecimal price;

        @NotNull
        @NotBlank
        private Set<String> keywords;


        public CreateServiceCommand toUpdateCommand() {
            return new CreateServiceCommand(serviceName, duration, price, keywords, companyId);
        }

        public UpdateServiceCommand toUpdateCommand(Long id) {
            return new UpdateServiceCommand(id, serviceName, duration, price, keywords);
        }
    }

}
