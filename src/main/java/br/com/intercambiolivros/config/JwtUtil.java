
package br.com.intercambiolivros.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class JwtUtil {

    private static final String ISSUER = "intercambiolivros-api";

    private static final String SECRET =
            System.getenv("JWT_SECRET");

    private static final Algorithm ALGORITHM =
            criarAlgorithm();

    private static Algorithm criarAlgorithm() {

        if (SECRET == null || SECRET.length() < 32) {
            throw new IllegalStateException(
                    "Configure JWT_SECRET com pelo menos 32 caracteres.");
        }

        return Algorithm.HMAC256(SECRET);
    }

    public static String gerarToken(
            Long usuarioId,
            Long perfilId) {

        Instant expiracao = Instant.now()
                .plus(2, ChronoUnit.HOURS);

        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(usuarioId.toString())
                .withClaim("perfilId", perfilId)
                .withIssuedAt(Instant.now())
                .withExpiresAt(expiracao)
                .sign(ALGORITHM);
    }

    public static DecodedJWT validarToken(String token) {

        JWTVerifier verifier = JWT.require(ALGORITHM)
                .withIssuer(ISSUER)
                .build();

        return verifier.verify(token);
    }
}
