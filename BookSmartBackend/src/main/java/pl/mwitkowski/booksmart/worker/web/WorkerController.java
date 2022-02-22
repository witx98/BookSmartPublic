package pl.mwitkowski.booksmart.worker.web;

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
import pl.mwitkowski.booksmart.worker.application.port.WorkerUseCase;
import pl.mwitkowski.booksmart.worker.application.port.WorkerUseCase.UpdateProfilePictureCommand;
import pl.mwitkowski.booksmart.worker.domain.WorkerEntity;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/workers")
public class WorkerController {

    private final WorkerUseCase workerService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RestWorker> getAll(@RequestParam Optional<Long> companyId, HttpServletRequest request) {
        List<WorkerEntity> workers;
        if (companyId.isPresent()) {
            workers = workerService.findByCompanyId(companyId.get());
        } else {
            workers = workerService.findAll();
        }
        return workers
                .stream()
                .map(worker -> toRestWorker(worker, request))
                .collect(Collectors.toList());
    }

    private RestWorker toRestWorker(WorkerEntity worker, HttpServletRequest request) {
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
                pictureUrl
        );

    }

    @GetMapping("/{id}")
    public ResponseEntity<RestWorker> getById(@PathVariable Long id, HttpServletRequest request) {
        return workerService
                .findById(id)
                .map(worker -> toRestWorker(worker, request))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/byEmail/{email}")
    public ResponseEntity<RestWorker> getByEmail(@PathVariable String email, HttpServletRequest request) {
        return workerService
                .findByEmail(email)
                .map(worker -> toRestWorker(worker, request))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @Secured({"ROLE_ADMIN"})
    @PostMapping()
    public ResponseEntity<Object> addWorker(@Valid @RequestBody RestWorkerCommand command) {
        return workerService
                .createWorker(command.toCreateCommand())
                .handle(
                        workerId -> ResponseEntity.created(createURI(workerId)).build(),
                        error -> ResponseEntity.badRequest().body(error)
                );
    }

    URI createURI(Long id) {
        return new CreatedURI("/" + id).uri();
    }


    @Secured({"ROLE_ADMIN", "ROLE_WORKER"})
    @PutMapping(value = "/{id}/picture", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<Object> addPicture(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        UpdateProfilePictureCommand command = new UpdateProfilePictureCommand(
                id,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes()
        );

        return workerService.updatePicture(command).handle(
                uploadId -> ResponseEntity.created(CreatedURI.uploadUri(uploadId)).build(),
                error -> ResponseEntity.badRequest().body(error)
        );
    }

    @Data
    private static class RestWorkerCommand {

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

        @NotNull
        @NotBlank
        @Email
        private String email;

        WorkerUseCase.CreateWorkerCommand toCreateCommand() {
            return new WorkerUseCase.CreateWorkerCommand(firstname, lastname, phone, email, companyId);
        }

    }
}
