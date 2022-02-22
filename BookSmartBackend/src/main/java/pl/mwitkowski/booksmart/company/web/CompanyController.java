package pl.mwitkowski.booksmart.company.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.mwitkowski.booksmart.commons.CreatedURI;
import pl.mwitkowski.booksmart.company.application.port.CompanyUseCase;
import pl.mwitkowski.booksmart.company.application.port.CompanyUseCase.CreateCompanyCommand;
import pl.mwitkowski.booksmart.company.application.port.CompanyUseCase.UpdateCompanyPictureCommand;
import pl.mwitkowski.booksmart.company.domain.CompanyEntity;
import pl.mwitkowski.booksmart.service.domain.ServiceEntity;
import pl.mwitkowski.booksmart.service.web.RestService;
import pl.mwitkowski.booksmart.worker.domain.WorkerEntity;
import pl.mwitkowski.booksmart.worker.web.RestWorker;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyUseCase companyService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RestCompany> getAll(HttpServletRequest request, @RequestParam Optional<String> serviceName,
                                    @RequestParam Optional<String> city
    ) {
        List<CompanyEntity> companies;
        if (serviceName.isPresent() && !serviceName.get().isBlank() && city.isPresent() && !city.get().isBlank()) {
            companies = companyService.findByServiceNameAndCity(serviceName.get(), city.get());
        } else if (serviceName.isPresent() && !serviceName.get().isBlank()) {
            companies = companyService.findByServiceName(serviceName.get());
        } else if (city.isPresent() && !city.get().isBlank()) {
            companies = companyService.findByCity(city.get());
        } else {
            companies = companyService.findAll();
        }
        return companies.stream()
                .map(company -> toRestCompany(company, request))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestCompany> getById(@PathVariable Long id, HttpServletRequest request) {
        return companyService
                .findById(id)
                .map(company -> toRestCompany(company, request))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Secured({"ROLE_ADMIN"})
    @PostMapping
    @ResponseStatus
    public ResponseEntity<Object> createCompany(@Valid @RequestBody RestCompanyCommand command) {
        return companyService
                .creatCompany(command.toCreateCommand())
                .handle(
                        companyId -> ResponseEntity.created(createURI(companyId)).build(),
                        error -> ResponseEntity.badRequest().body(error)
                );
    }

    URI createURI(Long id) {
        return new CreatedURI("/" + id).uri();
    }

    @Secured({"ROLE_ADMIN", "ROLE_WORKER"})
    @PutMapping(value = "/{id}/picture", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<Object> addPicture(@PathVariable Long id,
                                             @RequestParam("file") MultipartFile file) throws IOException {
        UpdateCompanyPictureCommand command = new UpdateCompanyPictureCommand(
                id,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes()
        );

        return companyService.updatePicture(command).handle(
                uploadId -> ResponseEntity.created(CreatedURI.uploadUri(uploadId)).build(),
                error -> ResponseEntity.badRequest().body(error)
        );
    }

    private RestCompany toRestCompany(CompanyEntity company, HttpServletRequest request) {
        String pictureUrl = Optional
                .ofNullable(company.getPictureId())
                .map(pictureId -> ServletUriComponentsBuilder
                        .fromContextPath(request)
                        .path("/uploads/{id}/file")
                        .build(pictureId)
                        .toASCIIString())
                .orElse(null);
        return new RestCompany(
                company.getId(),
                company.getCompanyName(),
                company.getPhone(),
                company.getEmail(),
                company.getStreet(),
                company.getCity(),
                company.getZipCode(),
                pictureUrl,
                company.getDescription(),
                toRestWorkers(company.getWorkers(), request),
                toRestServices(company.getServices()));
    }

    private Set<RestWorker> toRestWorkers(Set<WorkerEntity> workers, HttpServletRequest request) {
        return workers.stream()
                .map(worker -> {
                            String pictureUrl = Optional.ofNullable(worker.getPictureId())
                                    .map(pictureId -> ServletUriComponentsBuilder
                                            .fromContextPath(request)
                                            .path("/uploads/{id}/file")
                                            .build(pictureId)
                                            .toASCIIString())
                                    .orElse(null);

                            return new RestWorker(
                                    worker.getId(),
                                    worker.getFirstname(),
                                    worker.getLastname(),
                                    worker.getPhone(),
                                    worker.getEmail(),
                                    pictureUrl);
                        }
                )
                .collect(Collectors.toSet());
    }

    private Set<RestService> toRestServices(Set<ServiceEntity> services) {
        return services.stream()
                .map(service -> new RestService(
                        service.getId(),
                        service.getServiceName(),
                        service.getDuration().toMinutes(),
                        service.getPrice(),
                        service.getKeywords())
                )
                .collect(Collectors.toSet());
    }

    @Data
    private static class RestCompanyCommand {
        @NotNull
        @NotBlank
        private String companyName;

        @NotBlank
        @NotNull
        private String phone;

        @NotBlank
        @NotNull
        @Email
        private String email;

        @NotBlank
        @NotNull
        private String street;

        @NotBlank
        @NotNull
        private String city;

        @NotBlank
        @NotNull
        private String zipCode;

        private String description;


        CreateCompanyCommand toCreateCommand() {
            return new CreateCompanyCommand(companyName, phone, email, street, city, zipCode, description);
        }
    }

}
