package pl.mwitkowski.booksmart.worker.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.mwitkowski.booksmart.company.application.port.CompanyUseCase;
import pl.mwitkowski.booksmart.company.domain.CompanyEntity;
import pl.mwitkowski.booksmart.security.UserSecurity;
import pl.mwitkowski.booksmart.security.userDetails.application.port.AuthenticationProviderUseCase;
import pl.mwitkowski.booksmart.upload.application.port.UploadUseCase;
import pl.mwitkowski.booksmart.upload.application.port.UploadUseCase.SaveUploadCommand;
import pl.mwitkowski.booksmart.upload.domain.Upload;
import pl.mwitkowski.booksmart.worker.application.port.WorkerUseCase;
import pl.mwitkowski.booksmart.worker.db.WorkerRepository;
import pl.mwitkowski.booksmart.worker.domain.WorkerEntity;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
class WorkerService implements WorkerUseCase {

    private final WorkerRepository workerRepository;
    private final CompanyUseCase companyService;
    private final UploadUseCase uploadService;
    private final UserSecurity userSecurity;
    private final AuthenticationProviderUseCase authenticationProvider;

    @Override
    @Transactional
    public CreateWorkerResponse createWorker(CreateWorkerCommand command) {
        Optional<CompanyEntity> company = companyService.findById(command.getCompanyId());

        if (company.isEmpty()) {
            return CreateWorkerResponse.failure("Company with entered id: " + command.getCompanyId() + "doesn't exists");
        }

        if (workerRepository.findByPhoneIgnoreCase(command.getPhone()).isPresent()) {
            return CreateWorkerResponse.failure("Worker with entered phone already exists: " + command.getPhone());
        }

        WorkerEntity worker = WorkerEntity.builder()
                .firstname(command.getFirstname())
                .lastname(command.getLastname())
                .phone(command.getPhone())
                .email(command.getEmail())
                .company(company.get())
                .build();
        WorkerEntity savedWorker = workerRepository.save(worker);
        company.get().getWorkers().add(savedWorker);
        return CreateWorkerResponse.success(savedWorker.getId());
    }

    @Override
    public CreateWorkerResponse getOrCreateWorker(CreateWorkerCommand command) {
        return workerRepository.findByEmailIgnoreCase(command.getEmail())
                .map(worker -> CreateWorkerResponse.success(worker.getId()))
                .orElseGet(() -> createWorker(command));
    }


    @Override
    public List<WorkerEntity> findByCompanyId(Long companyId) {
        return workerRepository.findByCompany_Id(companyId);
    }

    @Override
    public List<WorkerEntity> findAll() {
        return workerRepository.findAll();
    }

    @Override
    public Optional<WorkerEntity> findById(Long id) {
        return workerRepository.findById(id);
    }

    @Override
    public Optional<WorkerEntity> findByEmail(String email) {
        return workerRepository.findByEmailIgnoreCase(email);
    }

    @Override
    public UpdateProfilePictureResponse updatePicture(UpdateProfilePictureCommand command) {
        return workerRepository.findById(command.getWorkerId())
                .map(worker -> {
                    if (userSecurity.isWorkerOrAdmin(worker.getEmail(), authenticationProvider.getAuthentication())) {
                        Upload savedUpload = uploadService.save(new SaveUploadCommand(command.getFilename(), command.getContentType(), command.getFile()));
                        worker.setPictureId(savedUpload.getId());
                        workerRepository.save(worker);
                        return UpdateProfilePictureResponse.success(savedUpload.getId());
                    }
                    return UpdateProfilePictureResponse.failure("Forbidden action - unauthorized picture update.");
                }).orElse(UpdateProfilePictureResponse.failure("Worker with provided id: " + command.getWorkerId() + " not found."));
    }


}
