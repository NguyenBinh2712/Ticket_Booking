package com.example.ticket.service;

import com.example.ticket.dto.auth.JwtRequest;
import com.example.ticket.enums.Role;
import com.example.ticket.exception.AppException;
import com.example.ticket.exception.ErrorCode;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class JwtService {
    @Value("${jwt.valid-duration}") @NonFinal
    long VALID_DURATION;

    @Value("${jwt.secretKey}")
    @NonFinal
    String SECRET_KEY;

    public String generateAccessToken(JwtRequest jwtRequest){
        JWSHeader jwsHeader=new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet=new JWTClaimsSet.Builder()
                .subject(jwtRequest.getEmail())
                .issuer("ticket")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.MINUTES).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("userId", jwtRequest.getUserId())
                .claim("scope",jwtRequest.getRole().name())
                .build();

        Payload payload=new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject=new JWSObject(jwsHeader,payload);

        try{
            jwsObject.sign(new MACSigner(SECRET_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (KeyLengthException e) {
            throw new RuntimeException(e);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

     public SignedJWT verifyAccessToken(String token ) throws JOSEException, ParseException {
        JWSVerifier jwsVerifier=new MACVerifier(SECRET_KEY.getBytes());
        SignedJWT signedJWT= SignedJWT.parse(token);
        var verify=signedJWT.verify(jwsVerifier);
        Date exp= signedJWT.getJWTClaimsSet().getExpirationTime();
        if(!(verify&&exp.after(new Date()))){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }
    public Long extractUserId(String token)
            throws ParseException, JOSEException {

        return verifyAccessToken(token)
                .getJWTClaimsSet()
                .getLongClaim("userId");
    }

    public Role extractRole(String accessToken)
            throws ParseException, JOSEException {

        SignedJWT jwt = verifyAccessToken(accessToken);

        String role = jwt.getJWTClaimsSet().getStringClaim("scope");

        return Role.valueOf(role);
    }

    public String extractEmail(String accessToken)
            throws ParseException, JOSEException {

        SignedJWT jwt = verifyAccessToken(accessToken);

        return jwt.getJWTClaimsSet().getSubject();
    }
}