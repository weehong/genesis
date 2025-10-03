package com.resetrix.genesis.fixtures.services;

import org.springframework.stereotype.Service;

@Service
public class DummyService {

    public String hello(String name) {
        return "Hello, " + name;
    }

    public void throwError() {
        throw new RuntimeException("Test error");
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
