package pl.mwitkowski.booksmart.upload.application.port;

import lombok.AllArgsConstructor;
import lombok.Value;
import pl.mwitkowski.booksmart.upload.domain.Upload;

import java.util.Optional;

public interface UploadUseCase {
    Upload save(SaveUploadCommand command);

    Optional<Upload> getById(Long id);

    void removeById(Long id);

    @Value
    @AllArgsConstructor
    class SaveUploadCommand {
        String filename;
        String contentType;
        byte[] file;
    }


}
