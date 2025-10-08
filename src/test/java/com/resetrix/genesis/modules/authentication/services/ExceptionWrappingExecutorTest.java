package com.resetrix.genesis.modules.authentication.services;

import com.resetrix.genesis.modules.authentication.exceptions.CryptographicException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExceptionWrappingExecutorTest {

    private final ExceptionWrappingExecutor executor = new ExceptionWrappingExecutor();

    @Test
    void run_shouldWrapCheckedException_asCryptographicException() {
        assertThatThrownBy(() -> executor.run(() -> {
            throw new Exception("boom");
        }))
            .isInstanceOf(CryptographicException.class)
            .hasMessage("Unexpected error")
            .hasCauseInstanceOf(Exception.class);
    }

    @Test
    void run_shouldPropagateRuntimeException() {
        assertThatThrownBy(() -> executor.run(() -> {
            throw new IllegalStateException("bad");
        }))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("bad");
    }

    @Test
    void run_shouldReturnValue_onSuccess() {
        String value = executor.run(() -> "ok");
        assertThat(value).isEqualTo("ok");
    }
}
