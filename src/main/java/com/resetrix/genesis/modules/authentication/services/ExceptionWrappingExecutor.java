package com.resetrix.genesis.modules.authentication.services;

import com.resetrix.genesis.modules.authentication.exceptions.CryptographicException;
import software.amazon.awssdk.core.exception.SdkException;

/**
 * Small helper to run operations that may throw checked exceptions and
 * wrap only truly unexpected exceptions as CryptographicException, while
 * preserving AWS SDK/Cognito exceptions and other runtime exceptions so
 * callers can handle business errors explicitly.
 */
class ExceptionWrappingExecutor {

    <T> T run(ThrowingSupplier<T> operation) {
        try {
            return operation.get();
        } catch (SdkException awsEx) {
            throw awsEx;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new CryptographicException("Unexpected error", e);
        }
    }

    @FunctionalInterface
    interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
