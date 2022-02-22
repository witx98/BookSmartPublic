package pl.mwitkowski.booksmart.initialization.web;

import lombok.AllArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.mwitkowski.booksmart.initialization.application.port.InitializationUseCase;


@RestController
@AllArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final InitializationUseCase initializationService;

    @Secured("ROLE_ADMIN")
    @PostMapping("/initialization")
    @Transactional
    public void initialize() {
        initializationService.initialize();
    }
}
