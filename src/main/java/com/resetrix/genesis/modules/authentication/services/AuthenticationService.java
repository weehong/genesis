package com.resetrix.genesis.modules.authentication.services;

import com.resetrix.genesis.modules.authentication.exceptions.AuthenticationConfigurationException;
import com.resetrix.genesis.modules.authentication.exceptions.CryptographicException;
import com.resetrix.genesis.modules.authentication.requests.CognitoSignInRequest;
import com.resetrix.genesis.modules.authentication.requests.CognitoSignUpRequest;
import com.resetrix.genesis.modules.authentication.responses.CognitoSignInResponse;
import com.resetrix.genesis.modules.authentication.responses.CognitoSignUpResponse;
import com.resetrix.genesis.shared.properties.CognitoProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidParameterException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.resetrix.genesis.modules.authentication.constants.AuthenticationConstant.ATTR_EMAIL;
import static com.resetrix.genesis.modules.authentication.constants.AuthenticationConstant.ATTR_PHONE;
import static com.resetrix.genesis.modules.authentication.constants.AuthenticationConstant.BASIC_EMAIL;
import static com.resetrix.genesis.modules.authentication.constants.AuthenticationConstant.HMAC_SHA256;
import static com.resetrix.genesis.modules.authentication.constants.AuthenticationConstant.PARAM_PASSWORD;
import static com.resetrix.genesis.modules.authentication.constants.AuthenticationConstant.PARAM_SECRET_HASH;
import static com.resetrix.genesis.modules.authentication.constants.AuthenticationConstant.PARAM_USERNAME;

@Service
public non-sealed class AuthenticationService implements IAuthenticationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    private final CognitoProperty cognitoProperty;
    private final CognitoIdentityProviderClient cognitoClient;
    private final ExceptionWrappingExecutor executor = new ExceptionWrappingExecutor();

    public AuthenticationService(CognitoProperty cognitoProperty,
                                 CognitoIdentityProviderClient cognitoClient) {
        this.cognitoProperty = cognitoProperty;
        this.cognitoClient = cognitoClient;
    }

    @Override
    public CognitoSignUpResponse signUp(CognitoSignUpRequest request) {
        return executeCognitoOperation(
            () -> {
                SignUpRequest signUpRequest = buildSignUpRequest(request);
                SignUpResponse awsResponse =
                    cognitoClient.signUp(signUpRequest);
                return new CognitoSignUpResponse(
                    awsResponse.userSub(),
                    awsResponse.codeDeliveryDetails().toString()
                );
            },
            "Sign up",
            request.email()
        );
    }

    @Override
    public CognitoSignInResponse signIn(CognitoSignInRequest request) {
        return executeCognitoOperation(
            () -> {
                InitiateAuthRequest authRequest = buildSignInRequest(request);
                InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);

                return new CognitoSignInResponse(
                    authResponse.authenticationResult().accessToken(),
                    authResponse.authenticationResult().refreshToken(),
                    authResponse.authenticationResult().idToken()
                );
            },
            "Sign in",
            request.email()
        );
    }

    /**
     * Executes a Cognito SDK operation with unified exception handling.
     * Returns the raw result (DTO) — your @RestControllerAdvice handles wrapping.
     */
    private <T> T executeCognitoOperation(ThrowingSupplier<T> operation, String operationName, String email) {
        try {
            return executor.run(operation::get);
        } catch (NotAuthorizedException e) {
            handleNotAuthorizedException(e, operationName, email);
            throw e;
        }
    }

    private SignUpRequest buildSignUpRequest(CognitoSignUpRequest request) {
        List<AttributeType> userAttributes = buildUserAttributes(request);

        SignUpRequest.Builder signUpRequestBuilder = SignUpRequest.builder()
            .clientId(cognitoProperty.getClientId())
            .username(request.email())
            .password(request.password())
            .userAttributes(userAttributes);

        if (hasClientSecret()) {
            String secretHash = calculateSecretHash(request.email());
            signUpRequestBuilder.secretHash(secretHash);
        }

        return signUpRequestBuilder.build();
    }

    private InitiateAuthRequest buildSignInRequest(CognitoSignInRequest request) {
        String email = requireValidEmail(request.email());
        String password = Objects.requireNonNull(request.password(), "password");

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_USERNAME, email);
        params.put(PARAM_PASSWORD, password);

        if (hasClientSecret()) {
            params.put(PARAM_SECRET_HASH, calculateSecretHash(email));
        }

        return InitiateAuthRequest.builder()
            .clientId(cognitoProperty.getClientId())
            .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
            .authParameters(params)
            .build();
    }

    private List<AttributeType> buildUserAttributes(
        CognitoSignUpRequest request) {
        List<AttributeType> userAttributes = new ArrayList<>();

        String email = request.email();
        if (email != null && !email.trim().isEmpty()) {
            String validEmail = requireValidEmail(email);
            userAttributes.add(AttributeType.builder()
                .name(ATTR_EMAIL)
                .value(validEmail)
                .build());
        }

        if (request.phoneNumber() != null
            && !request.phoneNumber().trim().isEmpty()) {
            userAttributes.add(AttributeType.builder()
                .name(ATTR_PHONE)
                .value(request.phoneNumber().trim())
                .build());
        }

        return userAttributes;
    }

    private String calculateSecretHash(String username) {
        String user = trimToNull(username);

        if (user == null) {
            throw new IllegalArgumentException("Username cannot be null or empty for SECRET_HASH calculation");
        }

        String clientSecret = trimToNull(cognitoProperty.getClientSecret());

        if (clientSecret == null) {
            throw new IllegalStateException("Client secret is not configured but required for SECRET_HASH");
        }

        try {
            String message = user + cognitoProperty.getClientId();

            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(
                new SecretKeySpec(clientSecret.getBytes(StandardCharsets.UTF_8),
                    HMAC_SHA256));

            byte[] digest = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptographicException("HmacSHA256 algorithm not available for SECRET_HASH calculation", e);
        } catch (InvalidKeyException e) {
            throw new AuthenticationConfigurationException("Invalid client secret key for SECRET_HASH calculation", e);
        } catch (Exception e) {
            throw new CryptographicException("Unexpected error during SECRET_HASH calculation for user: " + user, e);
        }
    }

    private boolean hasClientSecret() {
        return trimToNull(cognitoProperty.getClientSecret()) != null;
    }

    private void handleNotAuthorizedException(NotAuthorizedException e,
                                              String operationName,
                                              String email) {
        String msg = e.getMessage();
        if (msg != null
            && msg.contains("SECRET_HASH")) {
            LOGGER.error("{} failed - SECRET_HASH missing or invalid for user: {}",
                operationName,
                safe(email), e);
        } else if (msg != null
            && msg.toLowerCase(Locale.ROOT).contains("password")) {
            LOGGER.warn("{} failed - invalid credentials for user: {}",
                operationName,
                safe(email));
        } else if (msg != null
            && msg.toLowerCase(Locale.ROOT).contains("client")) {
            LOGGER.error("{} failed - invalid client configuration: {}",
                operationName,
                msg);
        } else {
            LOGGER.warn("{} failed - not authorized for user: {} - {}",
                operationName,
                safe(email),
                msg);
        }
    }

    private String trimToNull(String string) {
        if (string == null) {
            return null;
        }

        String trimString = string.trim();
        return trimString.isEmpty()
            ? null
            : trimString;
    }

    private String requireValidEmail(String email) {
        String trimEmail = trimToNull(email);

        if (trimEmail == null
            || !BASIC_EMAIL.matcher(trimEmail).matches()) {
            throw InvalidParameterException.builder()
                .message("Invalid email format: " + safe(email))
                .build();
        }

        return trimEmail;
    }

    private String safe(String string) {
        return string == null
            ? "<null>"
            : string;
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
