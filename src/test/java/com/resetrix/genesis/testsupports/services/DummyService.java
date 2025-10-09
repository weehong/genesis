package com.resetrix.genesis.testsupports.services;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkClientException;

@Service
public class DummyService {

    public String hello(String name) {
        return "Hello, " + name;
    }

    public void throwError() {
        throw new RuntimeException("Test error");
    }

    public void throwSdkException() {
        throw SdkClientException.builder()
                .message("AWS SDK error occurred")
                .build();
    }

    public String slowMethod() {
        try {
            // Sleep for 1100ms to exceed the 1000ms threshold
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted", e);
        }
        return "Slow operation completed";
    }
}
