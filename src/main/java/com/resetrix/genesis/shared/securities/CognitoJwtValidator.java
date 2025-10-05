package com.resetrix.genesis.shared.securities;

import com.resetrix.genesis.shared.properties.CognitoProperty;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

@Component
public class CognitoJwtValidator implements OAuth2TokenValidator<Jwt> {

    private final CognitoProperty cognitoProperty;

    public CognitoJwtValidator(CognitoProperty cognitoProperty) {
        this.cognitoProperty = cognitoProperty;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        OAuth2TokenValidatorResult issuerResult = validateIssuer(jwt);
        if (issuerResult.hasErrors()) {
            return issuerResult;
        }

        OAuth2TokenValidatorResult clientResult = validateClientId(jwt);
        if (clientResult.hasErrors()) {
            return clientResult;
        }

        OAuth2TokenValidatorResult tokenUseResult = validateTokenUse(jwt);
        if (tokenUseResult.hasErrors()) {
            return tokenUseResult;
        }

        OAuth2TokenValidatorResult issuedAtResult = validateIssuedAt(jwt);
        if (issuedAtResult.hasErrors()) {
            return issuedAtResult;
        }

        return OAuth2TokenValidatorResult.success();
    }

    private OAuth2TokenValidatorResult validateIssuer(Jwt jwt) {
        String issuer = jwt.getIssuer() != null
                ? jwt.getIssuer().toString()
                : null;
        String expectedIssuer = cognitoProperty.getIssuerUri();

        if (!Objects.equals(issuer, expectedIssuer)) {
            OAuth2Error error = new OAuth2Error(
                    "invalid_issuer",
                    "Expected issuer " + expectedIssuer + " but got " + issuer,
                    null);
            return OAuth2TokenValidatorResult.failure(error);
        }
        return OAuth2TokenValidatorResult.success();
    }

    private OAuth2TokenValidatorResult validateClientId(Jwt jwt) {
        String clientId = jwt.getClaimAsString("client_id");

        if (!Objects.equals(clientId, cognitoProperty.getClientId())) {
            OAuth2Error error = new OAuth2Error(
                    "invalid_client",
                    "Expected client_id " + cognitoProperty.getClientId() + " but got " + clientId,
                    null);
            return OAuth2TokenValidatorResult.failure(error);
        }
        return OAuth2TokenValidatorResult.success();
    }

    private OAuth2TokenValidatorResult validateTokenUse(Jwt jwt) {
        String tokenUse = jwt.getClaimAsString("token_use");
        if (!Objects.equals(tokenUse, "access")) {
            OAuth2Error error = new OAuth2Error(
                    "invalid_token_use",
                    "ID tokens are not acceptable for API authorization. "
                            + "Expected token_use to be 'access' but got '" + tokenUse + "'",
                    null);
            return OAuth2TokenValidatorResult.failure(error);
        }
        return OAuth2TokenValidatorResult.success();
    }

    private OAuth2TokenValidatorResult validateIssuedAt(Jwt jwt) {
        Instant issuedAt = jwt.getIssuedAt();

        if (issuedAt == null || issuedAt.isAfter(Instant.now())) {
            OAuth2Error error = new OAuth2Error(
                    "invalid_issued_at",
                    "JWT issuedAt is missing or in the future",
                    null);
            return OAuth2TokenValidatorResult.failure(error);
        }
        return OAuth2TokenValidatorResult.success();
    }
}
