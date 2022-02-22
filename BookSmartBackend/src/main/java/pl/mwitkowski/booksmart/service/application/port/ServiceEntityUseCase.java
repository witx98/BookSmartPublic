package pl.mwitkowski.booksmart.service.application.port;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import pl.mwitkowski.booksmart.commons.Either;
import pl.mwitkowski.booksmart.service.domain.ServiceEntity;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ServiceEntityUseCase {

    CreateServiceResponse createServiceEntity(CreateServiceCommand command);

    UpdateServiceResponse updateServiceEntity(UpdateServiceCommand command);

    Optional<ServiceEntity> findById(Long id);

    List<ServiceEntity> findAll();

    List<ServiceEntity> findCompanyServices(Long companyId);

    List<ServiceEntity> findCompanyServicesContainingName(Long companyId, String serviceName);

    @Value
    class CreateServiceCommand {
        String serviceName;
        Duration duration;
        BigDecimal price;
        Set<String> keywords;
        Long companyId;
    }

    class CreateServiceResponse extends Either<String, Long> {
        CreateServiceResponse(boolean success, String left, Long right) {
            super(success, left, right);
        }

        public static CreateServiceResponse success(Long serviceId) {
            return new CreateServiceResponse(true, null, serviceId);
        }

        public static CreateServiceResponse failure(String error) {
            return new CreateServiceResponse(false, error, null);
        }
    }

    @Value
    @Builder
    @AllArgsConstructor
    class UpdateServiceCommand {
        Long id;
        String serviceName;
        Duration duration;
        BigDecimal price;
        Set<String> keywords;
    }

    class UpdateServiceResponse extends Either<String, Long> {
        UpdateServiceResponse(boolean success, String left, Long right) {
            super(success, left, right);
        }


        public static UpdateServiceResponse success(Long serviceId) {
            return new UpdateServiceResponse(true, null, serviceId);
        }

        public static UpdateServiceResponse failure(String error) {
            return new UpdateServiceResponse(false, error, null);
        }
    }
}
