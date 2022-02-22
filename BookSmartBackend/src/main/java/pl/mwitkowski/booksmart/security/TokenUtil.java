package pl.mwitkowski.booksmart.security;

import com.auth0.jwt.algorithms.Algorithm;

public class TokenUtil {

    private static final String SECRET = "mySecret";
    public static long ACCESS_TOKEN_EXPIRATION_TIME = 10 * 60 * 1000;
    public static long REFRESH_TOKEN_EXPIRATION_TIME = 30 * 60 * 1000;


    public static Algorithm getAlgorithm() {
        return Algorithm.HMAC256(SECRET.getBytes());
    }


}
