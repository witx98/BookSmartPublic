package pl.mwitkowski.booksmart.service.web;

import lombok.Value;

import java.math.BigDecimal;
import java.util.Set;

@Value
public class RestService {
    Long id;
    String serviceName;
    long duration;
    BigDecimal price;
    Set<String> keywords;
}
