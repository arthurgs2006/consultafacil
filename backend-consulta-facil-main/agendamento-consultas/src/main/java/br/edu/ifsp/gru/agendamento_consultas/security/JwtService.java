package br.edu.ifsp.gru.agendamento_consultas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Serviço responsável pela geração e validação de tokens JWT.
 *
 * <p>Utiliza HMAC-SHA256 com a chave definida em {@code app.jwt.secret}.
 * O token carrega o e-mail do usuário como {@code subject} e expira após
 * o tempo definido em {@code app.jwt.expiration} (em milissegundos).</p>
 */
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expiration;

    /**
     * Gera um token JWT assinado para o usuário informado.
     *
     * @param user usuário autenticado pelo Spring Security
     * @return token JWT compactado como string
     */
    public String generate(UserDetails user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key())
                .compact();
    }

    /**
     * Extrai o e-mail (subject) de um token JWT.
     *
     * @param token token JWT
     * @return e-mail contido no subject do token
     */
    public String extractUsername(String token) {
        return claims(token).getSubject();
    }

    /**
     * Valida se o token pertence ao usuário e não está expirado.
     *
     * @param token token JWT a ser verificado
     * @param user  usuário carregado do banco de dados
     * @return {@code true} se o token for válido para o usuário
     */
    public boolean isValid(String token, UserDetails user) {
        try {
            return extractUsername(token).equals(user.getUsername())
                    && !claims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}