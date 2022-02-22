package pl.mwitkowski.booksmart.commons.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pl.mwitkowski.booksmart.user.domain.UserEntity;

@Getter
public class OnRegistrationCompleteEvent extends ApplicationEvent {

    private final String appUrl;
    private final UserEntity user;

    public OnRegistrationCompleteEvent(UserEntity user, String appUrl) {
        super(user);
        this.user = user;
        this.appUrl = appUrl;
    }
}
