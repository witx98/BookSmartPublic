package pl.mwitkowski.booksmart.worker.web;

import lombok.Value;

@Value
public class RestWorker {
    Long id;
    String firstname;
    String lastname;
    String phone;
    String email;
    String pictureUrl;
}
