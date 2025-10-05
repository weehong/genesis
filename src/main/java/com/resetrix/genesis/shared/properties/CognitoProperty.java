package com.resetrix.genesis.shared.properties;


import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "aws.cognito")
public class CognitoProperty {

    @NotBlank(message = "Cognito region is required.")
    private String region;

    @NotBlank(message = "Cognito user pool ID is required.")
    private String userPoolId;

    @NotBlank(message = "Cognito client ID is required.")
    private String clientId;

    private String clientSecret;
    private String jwksUri;
    private String issuer;

    @PostConstruct
    public void initializeDerivedProperties() {
        if (jwksUri == null || jwksUri.isBlank()) {
            jwksUri = getIssuerUri() + "/.well-known/jwks.json";
        }
    }

    public String getIssuerUri() {
        if (issuer != null && !issuer.isBlank()) {
            return issuer;
        }

        return String.format("https://cognito-idp.%s.amazonaws.com/%s", region,
                userPoolId);
    }
}