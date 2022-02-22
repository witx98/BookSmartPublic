package pl.mwitkowski.booksmart.company.web;

import lombok.Value;
import pl.mwitkowski.booksmart.service.web.RestService;
import pl.mwitkowski.booksmart.worker.web.RestWorker;

import java.util.Set;

@Value
public class RestCompany {
    Long id;
    String companyName;
    String phone;
    String email;
    String street;
    String city;
    String zipCode;
    String pictureUrl;
    String description;
    Set<RestWorker> workers;
    Set<RestService> services;
}
