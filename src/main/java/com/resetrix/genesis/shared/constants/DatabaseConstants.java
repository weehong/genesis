package com.resetrix.genesis.shared.constants;

import java.util.concurrent.TimeUnit;

public final class DatabaseConstants {
    public static final String JDBC_H2 = "jdbc:h2:";
    public static final String JDBC_POSTGRES = "jdbc:postgresql";
    public static final String JDBC_MYSQL = "jdbc:mysql:";

    public static final String DEFAULT_POOL_NAME = "Genesis-HikariCP";
    public static final String DEFAULT_APPLICATION_NAME = "Genesis-Application";
    public static final int DEFAULT_MAXIMUM_POOL_SIZE = 10;
    public static final int DEFAULT_MINIMUM_IDLE = 10;
    public static final long DEFAULT_CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(30);
    public static final long DEFAULT_IDLE_TIMEOUT = TimeUnit.MINUTES.toMillis(10);
    public static final long DEFAULT_MAX_LIFETIME = TimeUnit.MINUTES.toMillis(30);

    private DatabaseConstants() {
        throw new AssertionError("No instances.");
    }
}
