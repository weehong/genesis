package com.resetrix.genesis.modules.authentication.constants;

import java.util.regex.Pattern;

public final class ValidationConstants {
    public static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");

    private ValidationConstants() {
        throw new AssertionError("No instances.");
    }
}
