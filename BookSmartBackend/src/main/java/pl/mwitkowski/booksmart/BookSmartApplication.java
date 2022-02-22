package pl.mwitkowski.booksmart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import pl.mwitkowski.booksmart.reservation.application.ReservationProperties;

@EnableScheduling
@EnableConfigurationProperties({ReservationProperties.class})
@SpringBootApplication
public class BookSmartApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookSmartApplication.class, args);
    }


    @Bean
    RestTemplate restTemplate() {
        return new RestTemplateBuilder().build();
    }
}
