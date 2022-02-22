package pl.mwitkowski.booksmart.company.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.mwitkowski.booksmart.company.domain.CompanyEntity;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    Optional<CompanyEntity> findByCompanyNameIgnoreCaseOrEmailIgnoreCaseOrPhoneIgnoreCase(String companyName, String email, String phone);

    Optional<CompanyEntity> findByCompanyNameIgnoreCase(String companyName);

    List<CompanyEntity> findByCompanyNameContainsIgnoreCase(String companyName);

    Optional<CompanyEntity> findByEmailIgnoreCase(String email);

    Optional<CompanyEntity> findByPhoneIgnoreCase(String phone);


    @Query("SELECT c FROM CompanyEntity c JOIN c.services s " +
            "WHERE s.id = :serviceId")
    Optional<CompanyEntity> findByServiceId(@Param("serviceId") Long serviceId);

    @Query("SELECT DISTINCT c FROM CompanyEntity c JOIN c.services s JOIN s.keywords k " +
            "WHERE " +
            "(" +
            "   lower(s.serviceName) LIKE lower(concat('%', :serviceName,'%'))" +
            "   OR lower(k) LIKE lower(concat('%', :serviceName, '%')) " +
            ") " +
            "AND lower(c.city) LIKE lower(concat('%', :city,'%'))")
    List<CompanyEntity> findByServiceNameAndCity(@Param("serviceName") String serviceName, @Param("city") String city);

    @Query("SELECT DISTINCT c FROM CompanyEntity c JOIN c.services s " +
            "WHERE lower(s.serviceName) LIKE lower(concat('%', :serviceName,'%'))")
    List<CompanyEntity> findByServiceName(@Param("serviceName") String serviceName);

    @Query("SELECT DISTINCT c FROM CompanyEntity c JOIN c.services s JOIN s.keywords k " +
            "WHERE lower(s.serviceName) LIKE lower(concat('%', :serviceName,'%')) " +
            "OR lower(k) LIKE lower(concat('%', :serviceName, '%')) ")
    List<CompanyEntity> findByServiceNameOrKeyword(@Param("serviceName") String serviceName);

    @Query("SELECT DISTINCT c FROM CompanyEntity c " +
            "WHERE lower(c.city) LIKE lower(concat('%', :city,'%'))")
    List<CompanyEntity> findByCity(@Param("city") String city);
}
