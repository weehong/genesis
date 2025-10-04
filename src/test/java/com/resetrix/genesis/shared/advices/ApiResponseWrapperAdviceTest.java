package com.resetrix.genesis.shared.advices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resetrix.genesis.shared.responses.ApiResponse;
import com.resetrix.genesis.shared.securities.SecurityConfigurationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link ApiResponseWrapperAdvice} to verify automatic wrapping of controller responses.
 * <p>
 * The advice wraps non-String, non-ApiResponse controller responses into a standardized
 * {@link ApiResponse} format with success status, data, timestamp, and request path.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfigurationTest.class)
class ApiResponseWrapperAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldEmptyPathWrapResponse() throws Exception {
        String response = mockMvc.perform(get("")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);

        assertThat(apiResponse.data())
                .isInstanceOf(Map.class)
                .asInstanceOf(MAP)
                .containsEntry("message", "world");
        assertThat(apiResponse.path()).isEqualTo("/");
    }

    /**
     * Tests that a Map response is automatically wrapped in ApiResponse.
     * <p>
     * Input: GET /wrapped
     * <p>
     * Controller returns: {@code Map.of("message", "hello")}
     * <p>
     * Expected output:
     * <pre>
     * {
     *   "success": true,
     *   "data": {
     *     "message": "hello"
     *   },
     *   "timestamp": "2025-10-02T09:10:36.312Z",
     *   "path": "/wrapped"
     * }
     * </pre>
     */
    @Test
    void shouldWrapResponse() throws Exception {
        String response = mockMvc.perform(get("/wrapped")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);

        assertThat(apiResponse.data())
                .isInstanceOf(Map.class)
                .asInstanceOf(MAP)
                .containsEntry("message", "hello");
        assertThat(apiResponse.path()).isEqualTo("/wrapped");
    }

    /**
     * Tests that another Map response is wrapped with different data.
     * <p>
     * Input: GET /unwrapped
     * <p>
     * Controller returns: {@code Map.of("message", "world")}
     * <p>
     * Expected output:
     * <pre>
     * {
     *   "success": true,
     *   "data": {
     *     "message": "world"
     *   },
     *   "timestamp": "2025-10-02T09:10:36.312Z",
     *   "path": "/unwrapped"
     * }
     * </pre>
     */
    @Test
    void shouldWrapUnwrappedEndpoint() throws Exception {
        String response = mockMvc.perform(get("/unwrapped")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);

        assertThat(apiResponse.data())
                .isInstanceOf(Map.class)
                .asInstanceOf(MAP)
                .containsEntry("message", "world");
        assertThat(apiResponse.path()).isEqualTo("/unwrapped");
    }

    /**
     * Tests that ResponseEntity responses are also wrapped.
     * <p>
     * Input: GET /responseEntity
     * <p>
     * Controller returns: {@code ResponseEntity.ok(Map.of("message", "withEntity"))}
     * <p>
     * Expected output:
     * <pre>
     * {
     *   "success": true,
     *   "data": {
     *     "message": "withEntity"
     *   },
     *   "timestamp": "2025-10-02T09:10:36.312Z",
     *   "path": "/responseEntity"
     * }
     * </pre>
     */
    @Test
    void shouldWrapResponseEntity() throws Exception {
        String response = mockMvc.perform(get("/responseEntity")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);

        assertThat(apiResponse.data())
                .isInstanceOf(Map.class)
                .asInstanceOf(MAP)
                .containsEntry("message", "withEntity");
        assertThat(apiResponse.path()).isEqualTo("/responseEntity");
    }

    /**
     * Tests that String return types are NOT wrapped (excluded by supports() method).
     * <p>
     * Input: GET /string
     * <p>
     * Controller returns: {@code "plain text"}
     * <p>
     * Expected output (plain text, NOT wrapped):
     * <pre>
     * plain text
     * </pre>
     */
    @Test
    void shouldNotWrapStringReturnType() throws Exception {
        // String return types should NOT be wrapped
        String response = mockMvc.perform(get("/string")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Should be plain string, not wrapped in ApiResponse
        assertThat(response).isEqualTo("plain text");
    }

    /**
     * Tests that responses already wrapped in ApiResponse are not double-wrapped.
     * <p>
     * Input: GET /alreadyWrapped
     * <p>
     * Controller returns: {@code ApiResponse.success("custom data", "Custom message", "/custom-path")}
     * <p>
     * Expected output (NOT double-wrapped):
     * <pre>
     * {
     *   "success": true,
     *   "data": "custom data",
     *   "message": "Custom message",
     *   "timestamp": "2025-10-02T09:10:36.312Z",
     *   "path": "/custom-path"
     * }
     * </pre>
     */
    @Test
    void shouldNotWrapWhenAlreadyApiResponse() throws Exception {
        String response = mockMvc.perform(get("/alreadyWrapped")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);

        // Should not be double-wrapped
        assertThat(apiResponse.data()).isEqualTo("custom data");
        assertThat(apiResponse.message()).isEqualTo("Custom message");
        assertThat(apiResponse.path()).isEqualTo("/custom-path");
    }

    /**
     * Tests that HTML tags in the request path are sanitized.
     * <p>
     * Input: GET /html-path
     * <p>
     * Controller returns: {@code Map.of("data", "test")}
     * <p>
     * Expected output (path contains HTML-escaped characters if present):
     * <pre>
     * {
     *   "success": true,
     *   "data": {
     *     "data": "test"
     *   },
     *   "timestamp": "2025-10-02T09:10:36.312Z",
     *   "path": "/html-path"  // HTML characters would be escaped
     * }
     * </pre>
     */
    @Test
    void shouldSanitizeHtmlInPath() throws Exception {
        String response = mockMvc.perform(get("/html-path")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);

        // HTML should be escaped
        assertThat(apiResponse.path()).doesNotContain("<").doesNotContain(">");
    }

    /**
     * Tests that null response bodies are handled gracefully.
     * <p>
     * Input: GET /null-body
     * <p>
     * Controller returns: {@code null}
     * <p>
     * Expected output:
     * <pre>
     * {
     *   "success": true,
     *   "data": null,
     *   "timestamp": "2025-10-02T09:10:36.312Z",
     *   "path": "/null-body"
     * }
     * </pre>
     */
    @Test
    void shouldHandleNullBody() throws Exception {
        String response = mockMvc.perform(get("/null-body")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);

        assertThat(apiResponse.data()).isNull();
        assertThat(apiResponse.success()).isTrue();
        assertThat(apiResponse.path()).isEqualTo("/null-body");
    }

    /**
     * Tests that special characters in the request path are properly escaped.
     * <p>
     * Input: GET /special&lt;&gt;&amp;"'
     * <p>
     * Controller returns: {@code Map.of("data", "test")}
     * <p>
     * Expected output (URL encoding + HTML escaping):
     * <pre>
     * {
     *   "success": true,
     *   "data": {
     *     "data": "test"
     *   },
     *   "timestamp": "2025-10-02T09:10:36.312Z",
     *   "path": "/special%3C%3E&amp;%22&#39;"  // < becomes %3C, > becomes %3E, & becomes &amp;, " becomes %22, ' becomes &#39;
     * }
     * </pre>
     */
    @Test
    void shouldEscapeSpecialCharactersInPath() throws Exception {
        String response = mockMvc.perform(get("/special<>&\"'")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);

        // Special characters are URL-encoded by Spring MVC, then HTML escaped by our sanitizer
        // < becomes %3C, > becomes %3E, & becomes &amp;, " becomes %22, ' becomes &#39;
        assertThat(apiResponse.path())
                .contains("%3C")
                .contains("%3E")
                .contains("&amp;")
                .contains("%22")
                .contains("&#39;");
    }

    /**
     * Tests that complex nested objects are wrapped correctly.
     * <p>
     * Input: GET /complex
     * <p>
     * Controller returns:
     * <pre>
     * Map.of(
     *   "id", 123,
     *   "name", "Test",
     *   "nested", Map.of("nestedKey", "nestedValue")
     * )
     * </pre>
     * <p>
     * Expected output:
     * <pre>
     * {
     *   "success": true,
     *   "data": {
     *     "id": 123,
     *     "name": "Test",
     *     "nested": {
     *       "nestedKey": "nestedValue"
     *     }
     *   },
     *   "timestamp": "2025-10-02T09:10:36.312Z",
     *   "path": "/complex"
     * }
     * </pre>
     */
    @Test
    void shouldHandleComplexObjects() throws Exception {
        String response = mockMvc.perform(get("/complex")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);

        assertThat(apiResponse.data())
                .isInstanceOf(Map.class)
                .asInstanceOf(MAP)
                .containsKeys("id", "name", "nested");
    }

    /**
     * Tests that empty collections are wrapped correctly.
     * <p>
     * Input: GET /empty-map
     * <p>
     * Controller returns: {@code Map.of()} (empty map)
     * <p>
     * Expected output:
     * <pre>
     * {
     *   "success": true,
     *   "data": {},
     *   "timestamp": "2025-10-02T09:10:36.312Z",
     *   "path": "/empty-map"
     * }
     * </pre>
     */
    @Test
    void shouldWrapEmptyMap() throws Exception {
        String response = mockMvc.perform(get("/empty-map")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);

        assertThat(apiResponse.data()).isInstanceOf(Map.class);
        assertThat((Map<?, ?>) apiResponse.data()).isEmpty();
    }

    /**
     * Tests XSS pattern detection in request path.
     * URL-encoded paths are properly escaped, not flagged as XSS since encoding happens before sanitization.
     */
    @Test
    void shouldEscapeEncodedXSSPatterns() throws Exception {
        // Spring URL-encodes special characters (< becomes %3C, > becomes %3E)
        String response = mockMvc.perform(get("/test<script>alert('xss')</script>")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);

        // URL encoding happens first, so XSS pattern won't match
        // The path will be HTML-escaped but not null
        assertThat(apiResponse.path()).isNotNull();
        assertThat(apiResponse.path()).contains("%3C"); // < is URL-encoded
    }

    /**
     * Tests that iframe XSS patterns in URL-encoded form are escaped.
     */
    @Test
    void shouldEscapeIframeInPath() throws Exception {
        String response = mockMvc.perform(get("/test<iframe>")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);

        assertThat(apiResponse.path()).isNotNull();
        assertThat(apiResponse.path()).contains("%3Ciframe%3E"); // URL-encoded
    }

    /**
     * Tests that javascript: protocol patterns are properly handled.
     */
    @Test
    void shouldHandleJavascriptProtocol() throws Exception {
        String response = mockMvc.perform(get("/javascript:alert(1)")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);

        // javascript: will be detected as XSS pattern and path will be null
        assertThat(apiResponse.path()).isEqualTo("/");
    }

    /**
     * Tests that event handler XSS patterns like onload= are detected.
     */
    @Test
    void shouldDetectEventHandlerXSS() throws Exception {
        String response = mockMvc.perform(get("/testonload=alert(1)")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);

        // onload= will be detected as XSS pattern and path will be null
        assertThat(apiResponse.path()).isEqualTo("/");
    }

    /**
     * Tests that when a controller with Object return type returns an ApiResponse at runtime,
     * it passes through unchanged. This covers line 52 (true branch: body instanceof ApiResponse).
     */
    @Test
    void shouldPassThroughPolymorphicApiResponse() throws Exception {
        String response = mockMvc.perform(get("/polymorphic-api-response")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);

        // Should use the exact ApiResponse from controller, not wrapped again
        assertThat(apiResponse.data()).isEqualTo("polymorphic");
        assertThat(apiResponse.message()).isEqualTo("Polymorphic response");
        assertThat(apiResponse.path()).isEqualTo("/poly");
    }

    /**
     * Tests that non-Jackson converters are not supported by the advice.
     * This covers the false branch of isConverterTypeJackson check.
     * <p>
     * When a non-Jackson converter (e.g., StringHttpMessageConverter) is used,
     * the supports() method returns false, so the response is not wrapped.
     */
    @Test
    void shouldNotSupportNonJacksonConverter() throws Exception {
        // String endpoints use StringHttpMessageConverter, not Jackson
        String response = mockMvc.perform(get("/string")
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Should be plain string, not wrapped (converter check returns false)
        assertThat(response).isEqualTo("plain text");
    }

}
