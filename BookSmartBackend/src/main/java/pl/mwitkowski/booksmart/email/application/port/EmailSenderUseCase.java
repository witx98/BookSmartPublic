package pl.mwitkowski.booksmart.email.application.port;

public interface EmailSenderUseCase {

    void send(String to, String subject, String email);
}
