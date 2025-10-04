package com.resetrix.genesis.shared.properties;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperty {

    @NotNull
    private List<String> allowedOrigins = new ArrayList<>();

    @NotNull
    private List<String> allowedMethods = new ArrayList<>();

    @NotNull
    private List<String> allowedHeaders = new ArrayList<>();

    private Boolean allowCredentials = false;
    private Long maxAge = 1800L;
    private String pathPattern = "/**";
}