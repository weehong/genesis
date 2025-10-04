package com.resetrix.genesis.shared.properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CognitoPropertyTest {

    private CognitoProperty cognitoProperty;

    @BeforeEach
    void setUp() {
        cognitoProperty = new CognitoProperty();
        cognitoProperty.setRegion("us-east-1");
        cognitoProperty.setUserPoolId("us-east-1_testpool");
        cognitoProperty.setClientId("test-client-id");
    }

    @Test
    void shouldInitializeJwksUriWhenNull() {
        // Given
        cognitoProperty.setJwksUri(null);
        cognitoProperty.setIssuer("https://custom-issuer.com");

        // When
        cognitoProperty.initializeDerivedProperties();

        // Then
        assertThat(cognitoProperty.getJwksUri()).isEqualTo("https://custom-issuer.com/.well-known/jwks.json");
    }

    @Test
    void shouldInitializeJwksUriWhenBlank() {
        // Given
        cognitoProperty.setJwksUri("");
        cognitoProperty.setIssuer("https://custom-issuer.com");

        // When
        cognitoProperty.initializeDerivedProperties();

        // Then
        assertThat(cognitoProperty.getJwksUri()).isEqualTo("https://custom-issuer.com/.well-known/jwks.json");
    }

    @Test
    void shouldInitializeJwksUriWhenWhitespace() {
        // Given
        cognitoProperty.setJwksUri("   ");
        cognitoProperty.setIssuer("https://custom-issuer.com");

        // When
        cognitoProperty.initializeDerivedProperties();

        // Then
        assertThat(cognitoProperty.getJwksUri()).isEqualTo("https://custom-issuer.com/.well-known/jwks.json");
    }

    @Test
    void shouldNotOverrideJwksUriWhenAlreadySet() {
        // Given
        String existingJwksUri = "https://existing-jwks-uri.com/jwks.json";

        cognitoProperty.setJwksUri(existingJwksUri);
        cognitoProperty.setIssuer("https://custom-issuer.com");

        // When
        cognitoProperty.initializeDerivedProperties();

        // Then
        assertThat(cognitoProperty.getJwksUri()).isEqualTo(existingJwksUri);
    }

    @Test
    void shouldReturnCustomIssuerWhenSet() {
        // Given
        String customIssuer = "https://custom-issuer.com";
        cognitoProperty.setIssuer(customIssuer);

        // When
        String result = cognitoProperty.getIssuerUri();

        // Then
        assertThat(result).isEqualTo(customIssuer);
    }

    @Test
    void shouldReturnDefaultIssuerWhenIssuerIsNull() {
        // Given
        cognitoProperty.setIssuer(null);

        // When
        String result = cognitoProperty.getIssuerUri();

        // Then
        assertThat(result).isEqualTo("https://cognito-idp.us-east-1.amazonaws.com/us-east-1_testpool");
    }

    @Test
    void shouldReturnDefaultIssuerWhenIssuerIsBlank() {
        // Given
        cognitoProperty.setIssuer("");

        // When
        String result = cognitoProperty.getIssuerUri();

        // Then
        assertThat(result).isEqualTo("https://cognito-idp.us-east-1.amazonaws.com/us-east-1_testpool");
    }

    @Test
    void shouldReturnDefaultIssuerWhenIssuerIsWhitespace() {
        // Given
        cognitoProperty.setIssuer("   ");

        // When
        String result = cognitoProperty.getIssuerUri();

        // Then
        assertThat(result).isEqualTo("https://cognito-idp.us-east-1.amazonaws.com/us-east-1_testpool");
    }

    @Test
    void shouldReturnDefaultIssuerWithDifferentRegionAndPoolId() {
        // Given
        cognitoProperty.setRegion("eu-west-1");
        cognitoProperty.setUserPoolId("eu-west-1_anotherpool");
        cognitoProperty.setIssuer(null);

        // When
        String result = cognitoProperty.getIssuerUri();

        // Then
        assertThat(result).isEqualTo("https://cognito-idp.eu-west-1.amazonaws.com/eu-west-1_anotherpool");
    }

    @Test
    void shouldInitializeJwksUriWithDefaultIssuerWhenIssuerIsNull() {
        // Given
        cognitoProperty.setJwksUri(null);
        cognitoProperty.setIssuer(null);

        // When
        cognitoProperty.initializeDerivedProperties();

        // Then
        assertThat(cognitoProperty.getJwksUri()).isEqualTo("https://cognito-idp.us-east-1.amazonaws.com/us-east-1_testpool/.well-known/jwks.json");
    }

    @Test
    void shouldInitializeJwksUriWithDefaultIssuerWhenIssuerIsBlank() {
        // Given
        cognitoProperty.setJwksUri("");
        cognitoProperty.setIssuer("");

        // When
        cognitoProperty.initializeDerivedProperties();

        // Then
        assertThat(cognitoProperty.getJwksUri()).isEqualTo("https://cognito-idp.us-east-1.amazonaws.com/us-east-1_testpool/.well-known/jwks.json");
    }

    @Test
    void shouldSetAndGetAllProperties() {
        // Given
        String region = "ap-southeast-1";
        String userPoolId = "ap-southeast-1_pool123";
        String clientId = "client123";
        String clientSecret = "secret123";
        String jwksUri = "https://custom-jwks.com/jwks.json";
        String issuer = "https://custom-issuer.com";

        // When
        cognitoProperty.setRegion(region);
        cognitoProperty.setUserPoolId(userPoolId);
        cognitoProperty.setClientId(clientId);
        cognitoProperty.setClientSecret(clientSecret);
        cognitoProperty.setJwksUri(jwksUri);
        cognitoProperty.setIssuer(issuer);

        // Then
        assertThat(cognitoProperty.getRegion()).isEqualTo(region);
        assertThat(cognitoProperty.getUserPoolId()).isEqualTo(userPoolId);
        assertThat(cognitoProperty.getClientId()).isEqualTo(clientId);
        assertThat(cognitoProperty.getClientSecret()).isEqualTo(clientSecret);
        assertThat(cognitoProperty.getJwksUri()).isEqualTo(jwksUri);
        assertThat(cognitoProperty.getIssuer()).isEqualTo(issuer);
    }

    @Test
    void shouldCreateCognitoPropertyInstance() {
        // When
        CognitoProperty property = new CognitoProperty();

        // Then
        assertThat(property).isNotNull();
    }

    @Test
    void shouldHandleComplexScenarioWithInitialization() {
        // Given
        cognitoProperty.setRegion("ca-central-1");
        cognitoProperty.setUserPoolId("ca-central-1_complex");
        cognitoProperty.setJwksUri("  "); // whitespace
        cognitoProperty.setIssuer("  "); // whitespace

        // When
        cognitoProperty.initializeDerivedProperties();

        // Then
        String expectedIssuer = "https://cognito-idp.ca-central-1.amazonaws.com/ca-central-1_complex";
        assertThat(cognitoProperty.getIssuerUri()).isEqualTo(expectedIssuer);
        assertThat(cognitoProperty.getJwksUri()).isEqualTo(expectedIssuer + "/.well-known/jwks.json");
    }

    @Test
    void shouldHandleNullValuesGracefully() {
        // Given
        CognitoProperty property = new CognitoProperty();
        property.setRegion("us-west-2");
        property.setUserPoolId("us-west-2_nulltest");
        // Leave issuer, jwksUri, clientSecret as null

        // When
        property.initializeDerivedProperties();

        // Then
        assertThat(property.getIssuer()).isNull();
        assertThat(property.getClientSecret()).isNull();
        assertThat(property.getIssuerUri()).isEqualTo("https://cognito-idp.us-west-2.amazonaws.com/us-west-2_nulltest");
        assertThat(property.getJwksUri()).isEqualTo("https://cognito-idp.us-west-2.amazonaws.com/us-west-2_nulltest/.well-known/jwks.json");
    }
}
