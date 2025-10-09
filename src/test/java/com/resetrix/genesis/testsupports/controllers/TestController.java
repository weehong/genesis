package com.resetrix.genesis.testsupports.controllers;

import com.resetrix.genesis.shared.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping
    public Map<String, String> emptyString() {
        return Map.of("message", "world");
    }

    @GetMapping(
            value = "/wrapped",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> wrapped() {
        return Map.of("message", "hello");
    }

    @GetMapping(
            value = "/unwrapped",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> unwrapped() {
        return Map.of("message", "world");
    }

    @GetMapping(
            value = "/responseEntity",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> responseEntity() {
        return ResponseEntity.ok(Map.of("message", "withEntity"));
    }

    @GetMapping(
            value = "/string",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public String plainString() {
        return "plain text";
    }

    @GetMapping(
            value = "/alreadyWrapped",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<String> alreadyWrapped() {
        return ApiResponse.success(
                "custom data",
                "Custom message",
                "/custom-path"
        );
    }

    @GetMapping(
            value = "/html-path",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> htmlPath() {
        return Map.of("data", "test");
    }

    @GetMapping(
            value = "/null-body",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> nullBody() {
        return null;
    }

    @GetMapping(
            value = "/special<>&\"'",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> specialChars() {
        return Map.of("data", "test");
    }

    @GetMapping(
            value = "/complex",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> complex() {
        Map<String, Object> nested = new HashMap<>();
        nested.put("nestedKey", "nestedValue");

        return Map.of(
                "id", 123,
                "name", "Test",
                "nested", nested
        );
    }

    @GetMapping(
            value = "/empty-map",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> emptyMap() {
        return Map.of();
    }

    @GetMapping(
            value = "/test<script>alert('xss')</script>",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> scriptXss() {
        return Map.of("data", "test");
    }

    @GetMapping(
            value = "/test<iframe>",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> iframeXss() {
        return Map.of("data", "test");
    }

    @GetMapping(
            value = "/javascript:alert(1)",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> javascriptXss() {
        return Map.of("data", "test");
    }

    @GetMapping(
            value = "/testonload=alert(1)",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> onloadXss() {
        return Map.of("data", "test");
    }

    @GetMapping(
            value = "/polymorphic-api-response",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Object polymorphicApiResponse() {
        // Return type is Object, but runtime type is ApiResponse
        // This will cause supports() to return true, but beforeBodyWrite receives ApiResponse
        return ApiResponse.success("polymorphic", "Polymorphic response", "/poly");
    }
}
