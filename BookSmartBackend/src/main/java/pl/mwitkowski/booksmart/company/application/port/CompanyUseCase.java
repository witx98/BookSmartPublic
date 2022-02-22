package pl.mwitkowski.booksmart.company.application.port;

import lombok.AllArgsConstructor;
import lombok.Value;
import pl.mwitkowski.booksmart.commons.Either;
import pl.mwitkowski.booksmart.company.domain.CompanyEntity;

import java.util.List;
import java.util.Optional;

public interface CompanyUseCase {

    CreateCompanyResponse creatCompany(CreateCompanyCommand command);

    Optional<CompanyEntity> findById(Long id);

    List<CompanyEntity> findByName(String name);

    Optional<CompanyEntity> findOneByName(String name);

    List<CompanyEntity> findAll();

    UpdateCompanyPictureResponse updatePicture(UpdateCompanyPictureCommand command);

    List<CompanyEntity> findByServiceNameAndCity(String serviceName, String city);

    List<CompanyEntity> findByServiceName(String serviceName);

    List<CompanyEntity> findByCity(String city);

    Optional<CompanyEntity> findByServiceId(Long serviceId);

    Optional<CompanyEntity> findByCompanyEmail(String email);

    @Value
    class CreateCompanyCommand {
        String companyName;
        String phone;
        String email;
        String street;
        String city;
        String zipCode;
        String description;
    }

    class CreateCompanyResponse extends Either<String, Long> {

        CreateCompanyResponse(boolean success, String left, Long right) {
            super(success, left, right);
        }

        public static CreateCompanyResponse success(Long companyId) {
            return new CreateCompanyResponse(true, null, companyId);
        }

        public static CreateCompanyResponse failure(String error) {
            return new CreateCompanyResponse(false, error, null);
        }

    }

    @Value
    @AllArgsConstructor
    class UpdateCompanyPictureCommand {
        Long companyId;
        String filename;
        String contentType;
        byte[] file;
    }

    class UpdateCompanyPictureResponse extends Either<String, Long> {

        UpdateCompanyPictureResponse(boolean success, String left, Long right) {
            super(success, left, right);
        }

        public static UpdateCompanyPictureResponse success(Long uploadId) {
            return new UpdateCompanyPictureResponse(true, null, uploadId);
        }

        public static UpdateCompanyPictureResponse failure(String error) {
            return new UpdateCompanyPictureResponse(false, error, null);
        }


    }

}
