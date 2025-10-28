package com.resetrix.horaion.shared.securities;

import com.resetrix.horaion.shared.properties.CognitoProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
public class CognitoConfiguration {

    @Bean
    public CognitoIdentityProviderClient cognitoIdentityProviderClient(
            CognitoProperty cognitoProperty) {
        return CognitoIdentityProviderClient.builder()
                .region(Region.of(cognitoProperty.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider
                        .builder()
                        .build())
                .build();
    }
}