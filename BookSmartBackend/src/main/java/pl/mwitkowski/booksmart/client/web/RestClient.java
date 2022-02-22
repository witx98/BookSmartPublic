package pl.mwitkowski.booksmart.client.web;

import lombok.Value;

@Value
public class RestClient {
    Long id;
    String firstname;
    String lastname;
    String email;
    String phone;
}
