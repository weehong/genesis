package com.resetrix.genesis.shared.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for reading and writing JSON files in test resources.
 * Provides both file-based deserialization and object serialization capabilities.
 */
public class JsonFileReader {

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();
    private static final String REQUESTS_DIR = "requests/";
    private static final String RESPONSES_DIR = "responses/";

    private JsonFileReader() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Reads a JSON file and converts it to the specified class type
     *
     * @param filePath relative path from test/resources directory
     * @param clazz    target class type
     * @param <T>      generic type
     * @return deserialized object
     * @throws IOException if file cannot be read or parsed
     */
    public static <T> T readJson(String filePath, Class<T> clazz) throws IOException {
        String jsonContent = readFileAsString(filePath);
        return OBJECT_MAPPER.readValue(jsonContent, clazz);
    }

    /**
     * Reads a JSON file from test/resources directory and returns as String
     *
     * @param filePath relative path from test/resources directory (classpath root)
     * @return JSON content as String
     * @throws IOException if file cannot be read
     */
    public static String readFileAsString(String filePath) throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                return FileCopyUtils.copyToString(reader);
            }
        } catch (IOException e) {
            throw new IOException("Failed to read JSON file: " + filePath, e);
        }
    }

    /**
     * Reads a request JSON file from test/resources/{module}/requests/{endpoint}/{scenario}.json
     *
     * @param module   module name (e.g., "user", "company", "product", "order")
     * @param endpoint endpoint name (e.g., "create-user", "update-company")
     * @param scenario scenario name (e.g., "valid", "missing-email", "invalid-id")
     * @param clazz    target class type
     * @param <T>      generic type
     * @return deserialized request object
     * @throws IOException if file cannot be read or parsed
     */
    public static <T> T readRequest(String module, String endpoint, String scenario, Class<T> clazz) throws IOException {
        String filePath = module + "/" + REQUESTS_DIR + endpoint + "/" + ensureJsonExtension(scenario);
        return readJson(filePath, clazz);
    }

    /**
     * Reads a response JSON file from test/resources/{module}/responses/{endpoint}/{scenario}.json
     *
     * @param module   module name (e.g., "user", "company", "product", "order")
     * @param endpoint endpoint name (e.g., "create-user", "update-company")
     * @param scenario scenario name (e.g., "success", "validation-error", "not-found")
     * @param clazz    target class type
     * @param <T>      generic type
     * @return deserialized response object
     * @throws IOException if file cannot be read or parsed
     */
    public static <T> T readResponse(String module, String endpoint, String scenario, Class<T> clazz) throws IOException {
        String filePath = module + "/" + RESPONSES_DIR + endpoint + "/" + ensureJsonExtension(scenario);
        return readJson(filePath, clazz);
    }

    /**
     * Reads a request JSON file and returns as String
     *
     * @param module   module name (e.g., "user", "company", "product", "order")
     * @param endpoint endpoint name (e.g., "create-user")
     * @param scenario scenario name (e.g., "valid", "missing-email")
     * @return JSON content as String
     * @throws IOException if file cannot be read
     */
    public static String readRequestAsString(String module, String endpoint, String scenario) throws IOException {
        String filePath = module + "/" + REQUESTS_DIR + endpoint + "/" + ensureJsonExtension(scenario);
        return readFileAsString(filePath);
    }

    /**
     * Reads a response JSON file and returns as String
     *
     * @param module   module name (e.g., "user", "company", "product", "order")
     * @param endpoint endpoint name (e.g., "create-user")
     * @param scenario scenario name (e.g., "success", "validation-error")
     * @return JSON content as String
     * @throws IOException if file cannot be read
     */
    public static String readResponseAsString(String module, String endpoint, String scenario) throws IOException {
        String filePath = module + "/" + RESPONSES_DIR + endpoint + "/" + ensureJsonExtension(scenario);
        return readFileAsString(filePath);
    }

    /**
     * Converts an object to JSON string
     *
     * @param object object to serialize
     * @return JSON string representation
     */
    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * Creates a new builder instance
     *
     * @return Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Ensures the filename has .json extension
     *
     * @param fileName original filename
     * @return filename with .json extension
     */
    private static String ensureJsonExtension(String fileName) {
        return fileName.endsWith(".json") ? fileName : fileName + ".json";
    }

    /**
     * Builder pattern for more readable and fluent API
     */
    public static class Builder {
        private String module;
        private String endpoint;
        private String scenario;

        public Builder module(String module) {
            this.module = module;
            return this;
        }

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder scenario(String scenario) {
            this.scenario = scenario;
            return this;
        }

        public <T> T readRequest(Class<T> clazz) throws IOException {
            return JsonFileReader.readRequest(module, endpoint, scenario, clazz);
        }

        public <T> T readResponse(Class<T> clazz) throws IOException {
            return JsonFileReader.readResponse(module, endpoint, scenario, clazz);
        }

        public String readRequestAsString() throws IOException {
            return JsonFileReader.readRequestAsString(module, endpoint, scenario);
        }

        public String readResponseAsString() throws IOException {
            return JsonFileReader.readResponseAsString(module, endpoint, scenario);
        }
    }
}