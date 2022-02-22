package pl.mwitkowski.booksmart.worker.db;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mwitkowski.booksmart.worker.domain.WorkerEntity;

import java.util.List;
import java.util.Optional;

public interface WorkerRepository extends JpaRepository<WorkerEntity, Long> {
    Optional<WorkerEntity> findByPhoneIgnoreCase(String phone);

    Optional<WorkerEntity> findByEmailIgnoreCase(String phone);

    List<WorkerEntity> findByCompany_Id(Long id);
}
