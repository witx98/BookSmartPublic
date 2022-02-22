package pl.mwitkowski.booksmart.upload.db;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mwitkowski.booksmart.upload.domain.Upload;

public interface UploadRepository extends JpaRepository<Upload, Long> {
}
