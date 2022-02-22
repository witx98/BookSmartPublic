package pl.mwitkowski.booksmart.commons;

import lombok.AllArgsConstructor;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@AllArgsConstructor
public class CreatedURI {
    private final String path;

    public URI uri() {
        return ServletUriComponentsBuilder.fromCurrentRequestUri().path(path).build().toUri();
    }

    public static URI uploadUri(Long id) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/uploads/" + id).build().toUri();
    }
}
