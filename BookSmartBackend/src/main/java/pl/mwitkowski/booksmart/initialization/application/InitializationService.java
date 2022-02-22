package pl.mwitkowski.booksmart.initialization.application;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import pl.mwitkowski.booksmart.client.application.port.ClientUseCase.CreateClientCommand;
import pl.mwitkowski.booksmart.company.application.port.CompanyUseCase;
import pl.mwitkowski.booksmart.company.application.port.CompanyUseCase.CreateCompanyCommand;
import pl.mwitkowski.booksmart.company.application.port.CompanyUseCase.UpdateCompanyPictureCommand;
import pl.mwitkowski.booksmart.company.domain.CompanyEntity;
import pl.mwitkowski.booksmart.initialization.application.port.InitializationUseCase;
import pl.mwitkowski.booksmart.initialization.domain.CsvCompany;
import pl.mwitkowski.booksmart.initialization.domain.CsvSchedule;
import pl.mwitkowski.booksmart.initialization.domain.CsvService;
import pl.mwitkowski.booksmart.initialization.domain.CsvWorker;
import pl.mwitkowski.booksmart.reservation.application.port.ReservationUseCase;
import pl.mwitkowski.booksmart.reservation.application.port.ReservationUseCase.CreateReservationCommand;
import pl.mwitkowski.booksmart.reservation.application.port.ReservationUseCase.CreateReservationResponse;
import pl.mwitkowski.booksmart.schedule.application.port.ScheduleUseCase;
import pl.mwitkowski.booksmart.schedule.application.port.ScheduleUseCase.CreateScheduleCommand;
import pl.mwitkowski.booksmart.service.application.port.ServiceEntityUseCase;
import pl.mwitkowski.booksmart.service.application.port.ServiceEntityUseCase.CreateServiceCommand;
import pl.mwitkowski.booksmart.service.domain.ServiceEntity;
import pl.mwitkowski.booksmart.user.application.port.UserRegistrationUseCase;
import pl.mwitkowski.booksmart.worker.application.port.WorkerUseCase;
import pl.mwitkowski.booksmart.worker.application.port.WorkerUseCase.UpdateProfilePictureCommand;
import pl.mwitkowski.booksmart.worker.domain.WorkerEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static pl.mwitkowski.booksmart.user.application.port.UserRegistrationUseCase.*;

@Slf4j
@Service
@AllArgsConstructor
public class InitializationService implements InitializationUseCase {
    private final CompanyUseCase companyService;
    private final WorkerUseCase workerService;
    private final ServiceEntityUseCase service;
    private final ScheduleUseCase scheduleService;
    private final RestTemplate restTemplate;
    private final ReservationUseCase reservationService;
    private final UserRegistrationUseCase userRegistrationService;
    private final UserDetails systemUser;


    @Override
    @Transactional
    public void initialize() {
        initCompaniesData();
        initWorkersData();
        initSchedulesData();
        initServicesData();
        makeReservation();
    }

    private void makeReservation() {

        CreateClientCommand clientCommand = new CreateClientCommand("Robert", "Kamiński", "123-456-789", "robert@example.org");
        CreateClientCommand clientCommand2 = new CreateClientCommand("Mateusz", "Kowalski", "123-456-789", "mateusz@example.org");


        List<ServiceEntity> serviceAll = service.findAll();

        WorkerEntity worker = workerService.findByEmail("jan@example.org")
                .orElseThrow(() -> new IllegalArgumentException("Worker with email: " + "jan@example.org" + " not found"));

        CreateReservationCommand command = new CreateReservationCommand(
                LocalDateTime.of(2022, 2, 1, 10, 30),
                worker.getId(),
                clientCommand,
                serviceAll.get(0).getId()
        );
        CreateReservationCommand command2 = new CreateReservationCommand(
                LocalDateTime.of(2022, 2, 1, 13, 30),
                worker.getId(),
                clientCommand2,
                serviceAll.get(0).getId()
        );

        CreateReservationResponse response = reservationService.createReservation(command);
        String result = response.handle(
                reservationId -> "Created RESERVATION with id: " + reservationId.getId(),
                error -> "Failed to create RESERVATION: " + error
        );
        log.info(result);

        CreateReservationResponse response2 = reservationService.createReservation(command2);
        String result2 = response2.handle(
                reservationId -> "Created RESERVATION with id: " + reservationId.getId(),
                error -> "Failed to create RESERVATION: " + error
        );
        log.info(result2);

        reservationService.findAll()
                .forEach(reservation -> log.info("GOT RESERVATION: \n"
                        + "\tStart time:" + reservation.getStartTime() + "\n"
                        + "\tEnd time:" + reservation.getEndTime() + "\n"
                        + "\tService:" + reservation.getService().getServiceName() + "\n"
                        + "\tStatus:" + reservation.getStatus() + "\n"
                        + "\tClient Email:" + reservation.getClient().getEmail() + "\n"
                        + "\tWorker Email:" + reservation.getWorker().getEmail() + "\n"
                ));
    }


    private void initServicesData() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource("service.csv").getInputStream()))) {
            CsvToBean<CsvService> build = new CsvToBeanBuilder<CsvService>(reader)
                    .withType(CsvService.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            build.stream().forEach(this::initService);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse CSV file", e);
        }
    }

    private void initService(CsvService csvService) {
        CompanyEntity company = companyService.findByCompanyEmail(csvService.getCompanyEmail())
                .orElseThrow(() -> new IllegalArgumentException("Company with name: " + csvService.getCompanyEmail() + " not found."));

        Set<String> keywords = Arrays.stream(csvService.getKeywords().split(","))
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toSet());

        CreateServiceCommand command = new CreateServiceCommand(
                csvService.getServiceName(),
                Duration.parse(csvService.getDuration()),
                csvService.getPrice(),
                keywords,
                company.getId()
        );

        service.createServiceEntity(command);
    }


    private void initSchedulesData() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource("schedule.csv").getInputStream()))) {
            CsvToBean<CsvSchedule> build = new CsvToBeanBuilder<CsvSchedule>(reader)
                    .withType(CsvSchedule.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            build.stream().forEach(this::initSchedule);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse CSV file", e);
        }
    }

    private void initSchedule(CsvSchedule csvSchedule) {
        WorkerEntity worker = workerService.findByEmail(csvSchedule.getWorkerEmail())
                .orElseThrow(() -> new IllegalArgumentException("Worker with email: " + csvSchedule.getWorkerEmail() + " not found."));
        CreateScheduleCommand command = new CreateScheduleCommand(
                LocalDate.parse(csvSchedule.getDate()),
                LocalDateTime.parse(csvSchedule.getStartTime()),
                LocalDateTime.parse(csvSchedule.getEndTime()),
                worker.getId()
        );

        scheduleService.createSchedule(command);
    }


    private void initCompaniesData() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource("company.csv").getInputStream()))) {
            CsvToBean<CsvCompany> build = new CsvToBeanBuilder<CsvCompany>(reader)
                    .withType(CsvCompany.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            build.stream().forEach(this::initCompany);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse CSV file", e);
        }
    }

    private void initCompany(CsvCompany csvCompany) {

        CreateCompanyCommand command = new CreateCompanyCommand(
                csvCompany.getCompanyName(),
                csvCompany.getPhone(),
                csvCompany.getEmail(),
                csvCompany.getStreet(),
                csvCompany.getCity(),
                csvCompany.getZipCode(),
                csvCompany.getDescription()
        );

        companyService.creatCompany(command).handle(
                companyId -> {
                    if (csvCompany.getPicture().isBlank()) {
                        return companyId;
                    }
                    UpdateCompanyPictureCommand updateCompanyImage = updateCompanyImage(companyId, csvCompany.getPicture());
                    return companyService.updatePicture(updateCompanyImage);
                },
                RuntimeException::new
        );
    }

    private UpdateCompanyPictureCommand updateCompanyImage(Long companyId, String picture) {
        ResponseEntity<byte[]> response = restTemplate.exchange(picture, HttpMethod.GET, null, byte[].class);
        String contentType = Objects.requireNonNull(response.getHeaders().getContentType()).toString();
        return new UpdateCompanyPictureCommand(companyId, "companyPic", contentType, response.getBody());
    }


    private UpdateProfilePictureCommand updateWorkerImage(Long workerId, String picture) {
        ResponseEntity<byte[]> response = restTemplate.exchange(picture, HttpMethod.GET, null, byte[].class);
        String contentType = Objects.requireNonNull(response.getHeaders().getContentType()).toString();
        return new UpdateProfilePictureCommand(workerId, "workerPic", contentType, response.getBody());
    }

    private void initWorkersData() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource("worker.csv").getInputStream()))) {
            CsvToBean<CsvWorker> build = new CsvToBeanBuilder<CsvWorker>(reader)
                    .withType(CsvWorker.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            build.stream().forEach(this::initWorker);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse CSV file", e);
        }
    }

    private void initWorker(CsvWorker csvWorker) {

        CompanyEntity company = companyService.findByCompanyEmail(csvWorker.getCompanyEmail())
                .orElseThrow(() -> new IllegalArgumentException("Company with name: " + csvWorker.getCompanyEmail() + " not found."));

        RegisterWorkerCommand command = new RegisterWorkerCommand(
                csvWorker.getEmail(),
                "xxx",
                company.getId(),
                csvWorker.getFirstname(),
                csvWorker.getLastname(),
                csvWorker.getPhone()
        );

        userRegistrationService.registerWorker(command)
                .handle(
                        user -> {
                            if (csvWorker.getPicture().isBlank()) {
                                return user;
                            }
                            return workerService.findByEmail(user.getUsername()).map(worker ->
                                 workerService.updatePicture(updateWorkerImage(worker.getId(), csvWorker.getPicture()))
                            ).orElseThrow(() -> new IllegalStateException("User not found."));

                        },
                        RuntimeException::new
                );
    }
}
