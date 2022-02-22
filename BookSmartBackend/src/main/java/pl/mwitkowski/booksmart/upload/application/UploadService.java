package pl.mwitkowski.booksmart.upload.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.mwitkowski.booksmart.upload.application.port.UploadUseCase;
import pl.mwitkowski.booksmart.upload.db.UploadRepository;
import pl.mwitkowski.booksmart.upload.domain.Upload;

import java.util.Optional;

@AllArgsConstructor
@Service
public class UploadService implements UploadUseCase {

    private final UploadRepository uploadRepository;

    @Override
    @Transactional
    public Upload save(SaveUploadCommand command) {
        return uploadRepository.save(new Upload(command.getFilename(), command.getContentType(), command.getFile()));
    }

    @Override
    public Optional<Upload> getById(Long id) {
        return uploadRepository.findById(id);
    }

    @Override
    public void removeById(Long id) {

    }
}
