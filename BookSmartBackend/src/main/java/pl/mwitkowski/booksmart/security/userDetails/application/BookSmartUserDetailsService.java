package pl.mwitkowski.booksmart.security.userDetails.application;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.mwitkowski.booksmart.security.configuration.AdminConfig;
import pl.mwitkowski.booksmart.security.userDetails.domain.UserEntityDetails;
import pl.mwitkowski.booksmart.user.db.UserEntityRepository;

@AllArgsConstructor
public class BookSmartUserDetailsService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MSG =
            "user with email %s not found";
    private final UserEntityRepository userRepository;
    private final AdminConfig config;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (config.getUsername().equalsIgnoreCase(username)) {
            return config.adminUser();
        }
        return userRepository.findByUsernameIgnoreCase(username)
                .map(UserEntityDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, username)));
    }
}
