package com.resetrix.genesis.shared.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.datasource")
public class DatabaseProperty {

    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private HikariProperties hikari = new HikariProperties();

    @Getter
    @Setter
    public static class HikariProperties {
        private Integer maximumPoolSize;
        private Integer minimumIdle;
        private Long idleTimeout;
        private Long maxLifetime;
        private Long connectionTimeout;
        private Long leakDetectionThreshold;
    }
}