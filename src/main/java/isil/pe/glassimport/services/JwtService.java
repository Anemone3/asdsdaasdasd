package isil.pe.glassimport.services;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import isil.pe.glassimport.dto.response.JwtPayload;

@Service
public class JwtService {

    public String generateToken(JwtPayload payload, long expirationInSeconds) {

        Date issuedAt = new Date(System.currentTimeMillis());
        Date expirationDate = new Date(issuedAt.getTime() + expirationInSeconds * 1000L);

        return JWT.create()
                .withSubject(payload.sub())
                .withClaim("email", payload.email())
                // .withClaim("role",
                // payload.role().getPermissions().stream().map(Enum::name).toList())
                .withIssuedAt(issuedAt)
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256("KEY_TOKEN"));

    }

    public boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256("KEY_TOKEN")).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            System.err.println("Invalid token: " + e.getMessage());
            return false;
        }
    }

    public DecodedJWT decodeToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256("KEY_TOKEN")).build().verify(token);
        } catch (JWTVerificationException e) {
            System.err.println("Token decoding failed: " + e.getMessage());
            return null;
        }
    }

}