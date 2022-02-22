package pl.mwitkowski.booksmart.worker.application.port;

import lombok.AllArgsConstructor;
import lombok.Value;
import pl.mwitkowski.booksmart.commons.Either;
import pl.mwitkowski.booksmart.worker.domain.WorkerEntity;

import java.util.List;
import java.util.Optional;

public interface WorkerUseCase {
    CreateWorkerResponse createWorker(CreateWorkerCommand command);

    CreateWorkerResponse getOrCreateWorker(CreateWorkerCommand command);

    List<WorkerEntity> findByCompanyId(Long companyId);

    List<WorkerEntity> findAll();

    Optional<WorkerEntity> findById(Long id);

    Optional<WorkerEntity> findByEmail(String email);

    UpdateProfilePictureResponse updatePicture(UpdateProfilePictureCommand command);


    @Value
    class CreateWorkerCommand {
        String firstname;
        String lastname;
        String phone;
        String email;
        Long companyId;
    }

    class CreateWorkerResponse extends Either<String, Long> {

        CreateWorkerResponse(boolean success, String left, Long right) {
            super(success, left, right);
        }

        public static CreateWorkerResponse success(Long workerId) {
            return new CreateWorkerResponse(true, null, workerId);
        }

        public static CreateWorkerResponse failure(String error) {
            return new CreateWorkerResponse(false, error, null);
        }

    }

    @Value
    @AllArgsConstructor
    class UpdateProfilePictureCommand {
        Long workerId;
        String filename;
        String contentType;
        byte[] file;
    }

    class UpdateProfilePictureResponse extends Either<String, Long> {

        UpdateProfilePictureResponse(boolean success, String left, Long right) {
            super(success, left, right);
        }

        public static UpdateProfilePictureResponse success(Long uploadId) {
            return new UpdateProfilePictureResponse(true, null, uploadId);
        }

        public static UpdateProfilePictureResponse failure(String error) {
            return new UpdateProfilePictureResponse(false, error, null);
        }


    }
}
