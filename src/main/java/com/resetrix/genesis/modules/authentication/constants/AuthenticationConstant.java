package com.resetrix.genesis.modules.authentication.constants;

import java.util.regex.Pattern;

public final class AuthenticationConstant {
    public static final String HMAC_SHA256 = "HmacSHA256";
    public static final String ATTR_EMAIL = "email";
    public static final String ATTR_PHONE = "phone_number";
    public static final String PARAM_USERNAME = "USERNAME";
    public static final String PARAM_PASSWORD = "PASSWORD";
    public static final String PARAM_SECRET_HASH = "SECRET_HASH";

    public static final Pattern BASIC_EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private AuthenticationConstant() {
        throw new AssertionError("No instances.");
    }
}
