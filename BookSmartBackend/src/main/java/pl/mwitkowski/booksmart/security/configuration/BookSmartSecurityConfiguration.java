package pl.mwitkowski.booksmart.security.configuration;

import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.mwitkowski.booksmart.security.filters.BookSmartAuthenticationFilter;
import pl.mwitkowski.booksmart.security.filters.BookSmartAuthorizationFilter;
import pl.mwitkowski.booksmart.security.userDetails.application.BookSmartUserDetailsService;
import pl.mwitkowski.booksmart.user.db.UserEntityRepository;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
@EnableConfigurationProperties({AdminConfig.class})
@EnableWebSecurity
@AllArgsConstructor
public class BookSmartSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final UserEntityRepository userEntityRepository;
    private final AdminConfig adminConfig;

    @Bean
    User systemUser() {
        return adminConfig.adminUser();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.logout().permitAll();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeRequests()
                .mvcMatchers(HttpMethod.POST, "/reservations/**", "/clients", "/login", "/users/register").permitAll()
                .mvcMatchers(HttpMethod.GET, "/companies/**", "/workers/**",
                        "/services/**", "/schedules/**", "/uploads/**", "/users/token/refresh/**", "/users/registrationConfirm", "/reservations/confirmReservation").permitAll()
                .mvcMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .and()
                .httpBasic()
                .and()
                .addFilter(authenticationFilter())
                .addFilterBefore(authorizationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    private BookSmartAuthorizationFilter authorizationFilter() {
        return new BookSmartAuthorizationFilter();
    }

    private BookSmartAuthenticationFilter authenticationFilter() throws Exception {
        BookSmartAuthenticationFilter filter = new BookSmartAuthenticationFilter();
        filter.setAuthenticationManager(super.authenticationManager());
        return filter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider());
    }

    private AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        BookSmartUserDetailsService detailsService = new BookSmartUserDetailsService(userEntityRepository, adminConfig);
        provider.setUserDetailsService(detailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
