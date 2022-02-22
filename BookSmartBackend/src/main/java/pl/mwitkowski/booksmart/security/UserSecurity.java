package pl.mwitkowski.booksmart.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserSecurity {

    public boolean isOwnerWorkerOrAdmin(String objectOwner, String relatedWorker, Authentication user) {
        return isOwnerOrAdmin(objectOwner, user) || isRelatedWorker(relatedWorker, user);
    }

    public boolean isOwnerOrAdmin(String objectOwner, Authentication user) {
        return isAdmin(user) || isOwner(objectOwner, user);
    }

    public boolean isWorkerOrAdmin(String relatedWorker, Authentication user) {
        return isAdmin(user) || isRelatedWorker(relatedWorker, user);
    }

    public boolean isOwner(String objectOwner, Authentication user) {
        return user.getName().equalsIgnoreCase(objectOwner);
    }

    public boolean isAdmin(Authentication user) {

        return user.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_ADMIN"));
    }

    public boolean isRelatedWorker(String relatedWorker, Authentication user) {
        return user.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_WORKER"))
                && user.getName().equalsIgnoreCase(relatedWorker);
    }
}
