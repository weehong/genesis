package com.resetrix.genesis.shared.exceptions.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomAccessDeniedHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AccessDeniedException accessDeniedException;

    private CustomAccessDeniedHandler handler;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        handler = new CustomAccessDeniedHandler(objectMapper);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Test
    void shouldHandleAccessDeniedExceptionSuccessfully() throws IOException {
        // Given
        String exceptionMessage = "Access denied to resource";
        String requestUri = "/api/protected-resource";

        when(accessDeniedException.getMessage()).thenReturn(exceptionMessage);
        when(request.getRequestURI()).thenReturn(requestUri);
        when(response.getWriter()).thenReturn(printWriter);

        // When
        handler.handle(request, response, accessDeniedException);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Capture the error response map that was written
        ArgumentCaptor<Map<String, Object>> errorResponseCaptor = ArgumentCaptor.forClass(Map.class);
        verify(objectMapper).writeValue(eq(printWriter), errorResponseCaptor.capture());

        Map<String, Object> errorResponse = errorResponseCaptor.getValue();
        assertThat(errorResponse).containsEntry("error", "Forbidden");
        assertThat(errorResponse).containsEntry("message", exceptionMessage);
        assertThat(errorResponse).containsEntry("path", requestUri);
        assertThat(errorResponse).containsKey("timestamp");
        assertThat(errorResponse.get("timestamp")).isInstanceOf(String.class);
    }

    @Test
    void shouldHandleAccessDeniedExceptionWithNullMessage() throws IOException {
        // Given
        String requestUri = "/api/admin";

        when(accessDeniedException.getMessage()).thenReturn(null);
        when(request.getRequestURI()).thenReturn(requestUri);
        when(response.getWriter()).thenReturn(printWriter);

        // When
        handler.handle(request, response, accessDeniedException);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);

        ArgumentCaptor<Map<String, Object>> errorResponseCaptor = ArgumentCaptor.forClass(Map.class);
        verify(objectMapper).writeValue(eq(printWriter), errorResponseCaptor.capture());

        Map<String, Object> errorResponse = errorResponseCaptor.getValue();
        assertThat(errorResponse).containsEntry("error", "Forbidden");
        assertThat(errorResponse).containsEntry("message", null);
        assertThat(errorResponse).containsEntry("path", requestUri);
        assertThat(errorResponse).containsKey("timestamp");
    }

    @Test
    void shouldHandleAccessDeniedExceptionWithEmptyMessage() throws IOException {
        // Given
        String exceptionMessage = "";
        String requestUri = "/api/users";

        when(accessDeniedException.getMessage()).thenReturn(exceptionMessage);
        when(request.getRequestURI()).thenReturn(requestUri);
        when(response.getWriter()).thenReturn(printWriter);

        // When
        handler.handle(request, response, accessDeniedException);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);

        ArgumentCaptor<Map<String, Object>> errorResponseCaptor = ArgumentCaptor.forClass(Map.class);
        verify(objectMapper).writeValue(eq(printWriter), errorResponseCaptor.capture());

        Map<String, Object> errorResponse = errorResponseCaptor.getValue();
        assertThat(errorResponse).containsEntry("error", "Forbidden");
        assertThat(errorResponse).containsEntry("message", exceptionMessage);
        assertThat(errorResponse).containsEntry("path", requestUri);
    }

    @Test
    void shouldHandleAccessDeniedExceptionWithDifferentRequestPaths() throws IOException {
        // Given
        String exceptionMessage = "Insufficient privileges";
        String requestUri = "/api/v1/admin/users/delete";

        when(accessDeniedException.getMessage()).thenReturn(exceptionMessage);
        when(request.getRequestURI()).thenReturn(requestUri);
        when(response.getWriter()).thenReturn(printWriter);

        // When
        handler.handle(request, response, accessDeniedException);

        // Then
        ArgumentCaptor<Map<String, Object>> errorResponseCaptor = ArgumentCaptor.forClass(Map.class);
        verify(objectMapper).writeValue(eq(printWriter), errorResponseCaptor.capture());

        Map<String, Object> errorResponse = errorResponseCaptor.getValue();
        assertThat(errorResponse).containsEntry("path", requestUri);
    }

    @Test
    void shouldHandleAccessDeniedExceptionWithNullRequestUri() throws IOException {
        // Given
        String exceptionMessage = "Access denied";

        when(accessDeniedException.getMessage()).thenReturn(exceptionMessage);
        when(request.getRequestURI()).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        // When
        handler.handle(request, response, accessDeniedException);

        // Then
        ArgumentCaptor<Map<String, Object>> errorResponseCaptor = ArgumentCaptor.forClass(Map.class);
        verify(objectMapper).writeValue(eq(printWriter), errorResponseCaptor.capture());

        Map<String, Object> errorResponse = errorResponseCaptor.getValue();
        assertThat(errorResponse).containsEntry("path", null);
    }

    @Test
    void shouldPropagateIOExceptionFromObjectMapper() throws IOException {
        // Given
        String exceptionMessage = "Access denied";
        String requestUri = "/api/protected";
        IOException ioException = new IOException("Failed to write JSON");

        when(accessDeniedException.getMessage()).thenReturn(exceptionMessage);
        when(request.getRequestURI()).thenReturn(requestUri);
        when(response.getWriter()).thenReturn(printWriter);
        doThrow(ioException).when(objectMapper).writeValue(eq(printWriter), any(Map.class));

        // When & Then
        assertThatThrownBy(() -> handler.handle(request, response, accessDeniedException))
                .isInstanceOf(IOException.class)
                .hasMessage("Failed to write JSON");

        // Verify that response setup was still called
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void shouldPropagateIOExceptionFromResponseGetWriter() throws IOException {
        // Given
        String exceptionMessage = "Access denied";
        String requestUri = "/api/protected";
        IOException ioException = new IOException("Failed to get writer");

        when(accessDeniedException.getMessage()).thenReturn(exceptionMessage);
        when(request.getRequestURI()).thenReturn(requestUri);
        when(response.getWriter()).thenThrow(ioException);

        // When & Then
        assertThatThrownBy(() -> handler.handle(request, response, accessDeniedException))
                .isInstanceOf(IOException.class)
                .hasMessage("Failed to get writer");

        // Verify that response setup was still called
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void shouldCreateHandlerWithObjectMapper() {
        // Given
        ObjectMapper mapper = new ObjectMapper();

        // When
        CustomAccessDeniedHandler newHandler = new CustomAccessDeniedHandler(mapper);

        // Then
        assertThat(newHandler).isNotNull();
    }

    @Test
    void shouldHandleComplexAccessDeniedScenario() throws IOException {
        // Given
        String exceptionMessage = "User 'john.doe' does not have permission to access resource '/admin/settings'";
        String requestUri = "/api/v2/admin/settings/security";

        when(accessDeniedException.getMessage()).thenReturn(exceptionMessage);
        when(request.getRequestURI()).thenReturn(requestUri);
        when(response.getWriter()).thenReturn(printWriter);

        // When
        handler.handle(request, response, accessDeniedException);

        // Then
        verify(response).setStatus(403); // HttpServletResponse.SC_FORBIDDEN
        verify(response).setContentType("application/json");

        ArgumentCaptor<Map<String, Object>> errorResponseCaptor = ArgumentCaptor.forClass(Map.class);
        verify(objectMapper).writeValue(eq(printWriter), errorResponseCaptor.capture());

        Map<String, Object> errorResponse = errorResponseCaptor.getValue();
        assertThat(errorResponse).hasSize(4);
        assertThat(errorResponse).containsEntry("error", "Forbidden");
        assertThat(errorResponse).containsEntry("message", exceptionMessage);
        assertThat(errorResponse).containsEntry("path", requestUri);
        assertThat(errorResponse).containsKey("timestamp");

        // Verify timestamp is a valid ISO string
        String timestamp = (String) errorResponse.get("timestamp");
        assertThat(timestamp).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+Z");
    }

    @Test
    void shouldHandleAccessDeniedWithSpecialCharactersInPath() throws IOException {
        // Given
        String exceptionMessage = "Access denied";
        String requestUri = "/api/users/search?name=John&age=25&city=New%20York";

        when(accessDeniedException.getMessage()).thenReturn(exceptionMessage);
        when(request.getRequestURI()).thenReturn(requestUri);
        when(response.getWriter()).thenReturn(printWriter);

        // When
        handler.handle(request, response, accessDeniedException);

        // Then
        ArgumentCaptor<Map<String, Object>> errorResponseCaptor = ArgumentCaptor.forClass(Map.class);
        verify(objectMapper).writeValue(eq(printWriter), errorResponseCaptor.capture());

        Map<String, Object> errorResponse = errorResponseCaptor.getValue();
        assertThat(errorResponse).containsEntry("path", requestUri);
    }
}
