package com.resetrix.genesis.shared.configurations;

import com.resetrix.genesis.shared.properties.DatabaseProperty;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.metrics.micrometer.MicrometerMetricsTrackerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Configuration
public class DatabaseConfiguration {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(DatabaseConfiguration.class);
    private static final String JDBC_H2 = "jdbc:h2:";
    private static final String JDBC_POSTGRES = "jdbc:postgresql";
    private static final String JDBC_MYSQL = "jdbc:mysql:";

    private static final String DEFAULT_POOL_NAME = "Genesis-HikariCP";
    private static final String DEFAULT_APPLICATION_NAME = "Genesis-Application";
    private static final int DEFAULT_MAXIMUM_POOL_SIZE = 10;
    private static final int DEFAULT_MINIMUM_IDLE = 10;
    private static final long DEFAULT_CONNECTION_TIMEOUT =
        TimeUnit.SECONDS.toMillis(30);
    private static final long DEFAULT_IDLE_TIMEOUT =
        TimeUnit.MINUTES.toMillis(10);
    private static final long DEFAULT_MAX_LIFETIME =
        TimeUnit.MINUTES.toMillis(30);

    private final DatabaseProperty databaseProperty;
    private final Environment environment;
    private final Optional<MeterRegistry> meterRegistry;

    public DatabaseConfiguration(DatabaseProperty databaseProperty,
                                 Environment environment,
                                 MeterRegistry meterRegistry) {
        this.databaseProperty = databaseProperty;
        this.environment = environment;
        this.meterRegistry = Optional.ofNullable(meterRegistry);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "app.database", name = "enabled", havingValue = "true", matchIfMissing = true)
    public DataSource dataSource() {
        String jdbcUrl = databaseProperty.getUrl();
        String driverClassName = getDriverClassName(jdbcUrl);
        String databaseType = getDatabaseType(jdbcUrl);

        LOGGER.info("Initializing HikariCP DataSource for {}", databaseType);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(databaseProperty.getUsername());
        config.setPassword(getResolvedPassword());
        config.setDriverClassName(driverClassName);
        config.setPoolName(resolvePoolName());

        config.setAutoCommit(false);
        config.setReadOnly(false);

        applyPoolSizing(config);
        applyTimeouts(config);

        if (jdbcUrl.startsWith("jdbc:postgresql:")) {
            applyPostgreSQLOptimizations(config);
        } else if (jdbcUrl.startsWith(JDBC_H2)) {
            applyH2Optimizations(config);
        }

        applyMonitoring(config);

        logConfigurationSummary(config, driverClassName);
        return new HikariDataSource(config);
    }

    private String getDriverClassName(String jdbcUrl) {
        if (jdbcUrl.startsWith(JDBC_H2)) {
            return "org.h2.Driver";
        } else if (jdbcUrl.startsWith(JDBC_POSTGRES)) {
            return "org.postgresql.Driver";
        } else if (jdbcUrl.startsWith(JDBC_MYSQL)) {
            return "com.mysql.cj.jdbc.Driver";
        }
        return "org.h2.Driver";
    }

    private String getDatabaseType(String jdbcUrl) {
        if (jdbcUrl.startsWith(JDBC_H2)) {
            return "H2";
        } else if (jdbcUrl.startsWith(JDBC_POSTGRES)) {
            return "PostgreSQL";
        } else if (jdbcUrl.startsWith(JDBC_MYSQL)) {
            return "MySQL";
        }
        return "Unknown";
    }

    private void applyPoolSizing(HikariConfig config) {
        config.setMaximumPoolSize(getMaximumPoolSize());
        config.setMinimumIdle(getMinimumIdle());
    }

    private void applyTimeouts(HikariConfig config) {
        config.setConnectionTimeout(getConnectionTimeout());
        config.setIdleTimeout(getIdleTimeout());
        config.setMaxLifetime(getMaxLifetime());

        if (databaseProperty.getHikari().getLeakDetectionThreshold() != null
            && databaseProperty.getHikari().getLeakDetectionThreshold() > 0) {
            config.setLeakDetectionThreshold(
                databaseProperty.getHikari().getLeakDetectionThreshold());
        }

        config.setInitializationFailTimeout(0);
    }

    private void applyPostgreSQLOptimizations(HikariConfig config) {
        config.addDataSourceProperty("tcpKeepAlive", "true");
        config.addDataSourceProperty("ApplicationName", resolveApplicationName());
        config.addDataSourceProperty("assumeMinServerVersion", "12.0");
        config.addDataSourceProperty("reWriteBatchedInserts", "true");

        config.addDataSourceProperty("prepareThreshold", "5");
        config.addDataSourceProperty("preparedStatementCacheQueries", "250");
        config.addDataSourceProperty("preparedStatementCacheSizeMiB", "5");

        config.addDataSourceProperty("maintainTimeStats", "false");
    }

    private void applyH2Optimizations(HikariConfig config) {
        config.addDataSourceProperty("DB_CLOSE_DELAY", "-1");
        config.addDataSourceProperty("DB_CLOSE_ON_EXIT", "FALSE");

        config.addDataSourceProperty("CACHE_SIZE", "65536");
        config.addDataSourceProperty("LOCK_TIMEOUT", "10000");

        LOGGER.debug("Applied H2-specific optimizations");
    }

    private void applyMonitoring(HikariConfig config) {
        config.setRegisterMbeans(false);

        meterRegistry.ifPresent(reg -> {
            config.setMetricsTrackerFactory(new MicrometerMetricsTrackerFactory(reg));
            LOGGER.info("Metrics enabled with {}", reg.getClass().getSimpleName());
        });
    }

    private String getResolvedPassword() {
        if (StringUtils.hasText(System.getenv("DB_PASSWORD"))) {
            return System.getenv("DB_PASSWORD");
        }

        if (StringUtils.hasText(System.getProperty("db.password"))) {
            return System.getProperty("db.password");
        }

        return databaseProperty.getPassword();
    }

    private String resolvePoolName() {
        String[] profiles = environment.getActiveProfiles();
        return profiles.length > 0
            ? DEFAULT_POOL_NAME + "-" + String.join("-", profiles)
            : DEFAULT_POOL_NAME;
    }

    private String resolveApplicationName() {
        String springAppName = environment.getProperty("spring.application.name");
        if (StringUtils.hasText(springAppName)) {
            String[] profiles = environment.getActiveProfiles();
            return profiles.length > 0
                ? springAppName + "-" + String.join("-", profiles)
                : springAppName;
        }
        return DEFAULT_APPLICATION_NAME;
    }

    private int getMaximumPoolSize() {
        return databaseProperty.getHikari().getMaximumPoolSize() != null
            ? databaseProperty.getHikari().getMaximumPoolSize()
            : DEFAULT_MAXIMUM_POOL_SIZE;
    }

    private int getMinimumIdle() {
        return databaseProperty.getHikari().getMinimumIdle() != null
            ? databaseProperty.getHikari().getMinimumIdle()
            : DEFAULT_MINIMUM_IDLE;
    }

    private long getConnectionTimeout() {
        return databaseProperty.getHikari().getConnectionTimeout() != null
            ? databaseProperty.getHikari().getConnectionTimeout()
            : DEFAULT_CONNECTION_TIMEOUT;
    }

    private long getIdleTimeout() {
        return databaseProperty.getHikari().getIdleTimeout() != null
            ? databaseProperty.getHikari().getIdleTimeout()
            : DEFAULT_IDLE_TIMEOUT;
    }

    private long getMaxLifetime() {
        return databaseProperty.getHikari().getMaxLifetime() != null
            ? databaseProperty.getHikari().getMaxLifetime()
            : DEFAULT_MAX_LIFETIME;
    }

    private void logConfigurationSummary(HikariConfig config, String driver) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("HikariCP configured:");
            LOGGER.info("  Pool Name: {}", config.getPoolName());
            LOGGER.info("  Pool Size: min={}, max={}", config.getMinimumIdle(),
                config.getMaximumPoolSize());
            LOGGER.info("  Timeouts: connection={}ms, idle={}ms, maxLifetime={}ms",
                config.getConnectionTimeout(), config.getIdleTimeout(),
                config.getMaxLifetime());
            LOGGER.info("  Leak Detection: {}",
                config.getLeakDetectionThreshold() > 0
                    ? config.getLeakDetectionThreshold() + "ms"
                    : "disabled");
            LOGGER.info("  Database URL: {}",
                maskSensitiveUrl(databaseProperty.getUrl()));
            LOGGER.info("  Driver: {}", driver);
            LOGGER.info("  Application Name: {}", resolveApplicationName());
        }
    }

    private String maskSensitiveUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return "N/A";
        }

        return url.replaceAll("://[^@/]*@", "://***:***@")
            .replaceAll("([?&]password=)[^&]*", "$1***");
    }
}
