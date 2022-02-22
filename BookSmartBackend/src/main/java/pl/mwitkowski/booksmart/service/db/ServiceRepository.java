package pl.mwitkowski.booksmart.service.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.mwitkowski.booksmart.service.domain.ServiceEntity;

import java.util.List;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

    @Query("SELECT s FROM CompanyEntity c JOIN c.services s WHERE c.id = :companyId")
    List<ServiceEntity> findCompanyServices(@Param("companyId") Long companyId);

    @Query("SELECT s FROM CompanyEntity c JOIN c.services s " +
            "WHERE c.id = :companyId " +
            "AND lower(s.serviceName) LIKE lower(concat('%', :serviceName,'%'))")
    List<ServiceEntity> findCompanyServicesContainingName(@Param("companyId") Long companyId, @Param("serviceName") String serviceName);
}
