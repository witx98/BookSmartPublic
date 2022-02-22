package pl.mwitkowski.booksmart.service.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.mwitkowski.booksmart.company.application.port.CompanyUseCase;
import pl.mwitkowski.booksmart.company.domain.CompanyEntity;
import pl.mwitkowski.booksmart.security.UserSecurity;
import pl.mwitkowski.booksmart.security.userDetails.application.port.AuthenticationProviderUseCase;
import pl.mwitkowski.booksmart.service.application.port.ServiceEntityUseCase;
import pl.mwitkowski.booksmart.service.db.ServiceRepository;
import pl.mwitkowski.booksmart.service.domain.ServiceEntity;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
class ServiceEntityService implements ServiceEntityUseCase {

    private final ServiceRepository serviceRepository;
    private final CompanyUseCase companyUseCase;
    private final UserSecurity userSecurity;
    private final AuthenticationProviderUseCase authenticationProvider;

    @Override
    @Transactional
    public CreateServiceResponse createServiceEntity(CreateServiceCommand command) {
        Optional<CompanyEntity> company = companyUseCase.findById(command.getCompanyId());

        if (company.isEmpty()) {
            return CreateServiceResponse.failure("Company with id: " + command.getCompanyId() + " not found");
        }
        boolean anyMatch = company.get().getWorkers().stream().anyMatch(worker -> userSecurity.isRelatedWorker(worker.getEmail(), authenticationProvider.getAuthentication()));
        if (!userSecurity.isAdmin(authenticationProvider.getAuthentication()) && !anyMatch) {
            return CreateServiceResponse.failure("Forbidden action - unauthorized service creation");
        }

        ServiceEntity serviceEntity = new ServiceEntity(command.getServiceName(), command.getDuration(),
                command.getPrice(), command.getKeywords());
        ServiceEntity savedService = serviceRepository.save(serviceEntity);
        company.get().getServices().add(savedService);
        return CreateServiceResponse.success(savedService.getId());
    }

    @Override
    @Transactional
    public UpdateServiceResponse updateServiceEntity(UpdateServiceCommand command) {

        return serviceRepository.findById(command.getId())
                .map(service -> {
                    CompanyEntity company = companyUseCase.findByServiceId(service.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Company with service id: " + service.getId() + " not found."));
                    boolean anyMatch = company.getWorkers().stream().anyMatch(worker -> userSecurity.isRelatedWorker(worker.getEmail(), authenticationProvider.getAuthentication()));
                    if (userSecurity.isAdmin(authenticationProvider.getAuthentication()) || anyMatch) {
                        UpdateServiceResponse response = updateFields(command, service);
                        serviceRepository.save(service);
                        return response;
                    }
                    return UpdateServiceResponse.failure("Forbidden action - unauthorized service update");
                })
                .orElseGet(() -> UpdateServiceResponse.failure("Service not found with id: " + command.getId()));
    }

    @Override
    public Optional<ServiceEntity> findById(Long id) {
        return serviceRepository.findById(id);
    }

    @Override
    public List<ServiceEntity> findAll() {
        return serviceRepository.findAll();
    }

    @Override
    public List<ServiceEntity> findCompanyServices(Long companyId) {
        return serviceRepository.findCompanyServices(companyId);
    }

    @Override
    public List<ServiceEntity> findCompanyServicesContainingName(Long companyId, String serviceName) {
        return serviceRepository.findCompanyServicesContainingName(companyId, serviceName);
    }

    private UpdateServiceResponse updateFields(UpdateServiceCommand command, ServiceEntity serviceEntity) {
        if (command.getServiceName() != null) {
            serviceEntity.setServiceName(command.getServiceName());
        }
        if (command.getDuration() != null) {
            serviceEntity.setDuration(command.getDuration());
        }
        if (command.getPrice() != null) {
            serviceEntity.setPrice(command.getPrice());
        }
        if (command.getKeywords() != null) {
            serviceEntity.setKeywords(command.getKeywords());
        }
        return UpdateServiceResponse.success(serviceEntity.getId());
    }
}
