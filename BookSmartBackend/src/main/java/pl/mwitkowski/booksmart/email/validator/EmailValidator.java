package pl.mwitkowski.booksmart.email.validator;

public class EmailValidator {

    public static boolean validate(String email) {
        boolean allowLocal = false;
        return org.apache.commons.validator.routines.EmailValidator.getInstance(allowLocal).isValid(email);
    }
}
