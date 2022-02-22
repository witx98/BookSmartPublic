package pl.mwitkowski.booksmart.company.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import pl.mwitkowski.booksmart.company.application.port.CompanyUseCase;
import pl.mwitkowski.booksmart.company.application.port.CompanyUseCase.CreateCompanyCommand;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
class CompanyControllerIntegrationTest {

    @Autowired
    CompanyUseCase companyUseCase;

    @Autowired
    CompanyController companyController;

    @Autowired
    HttpServletRequest request;

    @Test
    void getAll() {
        // given
        givenTwoCompanies();

        // when
        List<RestCompany> allCompanies = companyController
                .getAll(request, Optional.empty(), Optional.empty());

        List<RestCompany> filteredCompanies = companyController
                .getAll(request, Optional.empty(), Optional.of("Krakow"));

        // then
        assertEquals(2, allCompanies.size());
        assertEquals(1, filteredCompanies.size());
    }

    private void givenTwoCompanies() {
        CreateCompanyCommand command1 = new CreateCompanyCommand("One","123123123", "one@gmail.com", "Wielicka", "Krakow", "30-500", "tttttttttttttttt");
        CreateCompanyCommand command2 = new CreateCompanyCommand("Two","111111111", "two@gmail.com", "Wielicka", "Warszawa", "30-500", "tttttttttttttttt");
        companyUseCase.creatCompany(command1);
        companyUseCase.creatCompany(command2);
    }
}