package pl.mwitkowski.booksmart.company.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.mwitkowski.booksmart.company.application.port.CompanyUseCase;
import pl.mwitkowski.booksmart.company.db.CompanyRepository;
import pl.mwitkowski.booksmart.company.domain.CompanyEntity;
import pl.mwitkowski.booksmart.security.UserSecurity;
import pl.mwitkowski.booksmart.security.userDetails.application.port.AuthenticationProviderUseCase;
import pl.mwitkowski.booksmart.upload.application.port.UploadUseCase;
import pl.mwitkowski.booksmart.upload.domain.Upload;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
class CompanyService implements CompanyUseCase {

    private final CompanyRepository companyRepository;
    private final UploadUseCase uploadService;
    private final UserSecurity userSecurity;
    private final AuthenticationProviderUseCase authenticationProvider;

    @Override
    @Transactional
    public CreateCompanyResponse creatCompany(CreateCompanyCommand command) {
        if (companyRepository.findByCompanyNameIgnoreCase(command.getCompanyName()).isPresent()) {
            return CreateCompanyResponse.failure("Company with entered name already exists: " + command.getCompanyName());
        }
        if (companyRepository.findByEmailIgnoreCase(command.getEmail()).isPresent()) {
            return CreateCompanyResponse.failure("Company with entered email already exists: " + command.getEmail());
        }
        if (companyRepository.findByPhoneIgnoreCase(command.getPhone()).isPresent()) {
            return CreateCompanyResponse.failure("Company with entered phone already exists: " + command.getPhone());
        }
        CompanyEntity company = CompanyEntity.builder()
                .companyName(command.getCompanyName())
                .email(command.getEmail())
                .phone(command.getPhone())
                .street(command.getStreet())
                .city(command.getCity())
                .zipCode(command.getZipCode())
                .description(command.getDescription())
                .build();
        CompanyEntity savedCompany = companyRepository.save(company);
        return CreateCompanyResponse.success(savedCompany.getId());
    }

    @Override
    public Optional<CompanyEntity> findById(Long id) {
        return companyRepository.findById(id);
    }

    @Override
    public List<CompanyEntity> findByName(String name) {
        return companyRepository.findByCompanyNameContainsIgnoreCase(name);
    }

    @Override
    public Optional<CompanyEntity> findOneByName(String name) {
        return companyRepository.findByCompanyNameIgnoreCase(name);
    }

    @Override
    public List<CompanyEntity> findAll() {
        return companyRepository.findAll();
    }

    @Override
    public UpdateCompanyPictureResponse updatePicture(UpdateCompanyPictureCommand command) {
        return companyRepository.findById(command.getCompanyId())
                .map(company -> {
                    boolean anyMatch = company.getWorkers().stream().anyMatch(worker -> userSecurity.isRelatedWorker(worker.getEmail(), authenticationProvider.getAuthentication()));
                    if (userSecurity.isAdmin(authenticationProvider.getAuthentication()) || anyMatch) {
                        Upload savedUpload = uploadService.save(new UploadUseCase.SaveUploadCommand(command.getFilename(), command.getContentType(), command.getFile()));
                        company.setPictureId(savedUpload.getId());
                        companyRepository.save(company);
                        return UpdateCompanyPictureResponse.success(savedUpload.getId());
                    }
                    return UpdateCompanyPictureResponse.failure("Forbidden action");
                }).orElse(UpdateCompanyPictureResponse.failure("Company with provided id: " + command.getCompanyId() + " not found."));
    }

    @Override
    public List<CompanyEntity> findByServiceNameAndCity(String serviceName, String city) {
        return companyRepository.findByServiceNameAndCity(serviceName, city);
    }

    @Override
    public List<CompanyEntity> findByServiceName(String serviceName) {
        return companyRepository.findByServiceNameOrKeyword(serviceName);
    }

    @Override
    public List<CompanyEntity> findByCity(String city) {
        return companyRepository.findByCity(city);
    }

    @Override
    public Optional<CompanyEntity> findByServiceId(Long serviceId) {
        return companyRepository.findByServiceId(serviceId);
    }

    @Override
    public Optional<CompanyEntity> findByCompanyEmail(String email) {
        return companyRepository.findByEmailIgnoreCase(email);
    }

}
