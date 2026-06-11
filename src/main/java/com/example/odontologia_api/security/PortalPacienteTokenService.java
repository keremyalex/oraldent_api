package com.example.odontologia_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PortalPacienteTokenService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration-ms}")
    private long expirationMs;

    public String generateToken(Long pacienteId, String codigoPaciente) {
        Date now = new Date();
        return Jwts.builder()
                .subject("portal-paciente:" + pacienteId)
                .claim("tipo", "PORTAL_PACIENTE")
                .claim("pacienteId", pacienteId)
                .claim("codigoPaciente", codigoPaciente)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public PortalPacienteDetails validarToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (!"PORTAL_PACIENTE".equals(claims.get("tipo", String.class))) {
            return null;
        }
        if (claims.getExpiration() == null || !claims.getExpiration().after(new Date())) {
            return null;
        }

        Number pacienteIdClaim = claims.get("pacienteId", Number.class);
        Long pacienteId = pacienteIdClaim == null ? null : pacienteIdClaim.longValue();
        String codigoPaciente = claims.get("codigoPaciente", String.class);
        if (pacienteId == null || codigoPaciente == null || codigoPaciente.isBlank()) {
            return null;
        }
        return new PortalPacienteDetails(pacienteId, codigoPaciente);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}


