package pl.mwitkowski.booksmart.client.db;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mwitkowski.booksmart.client.domain.ClientEntity;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

    Optional<ClientEntity> findByEmailIgnoreCase(String email);


}
