package com.resetrix.genesis.shared.advices;

import com.resetrix.genesis.shared.responses.ApiResponse;
import com.resetrix.genesis.testsupports.converters.MockJacksonConverter;
import com.resetrix.genesis.testsupports.converters.MockJsonConverter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ApiResponseWrapperAdvice} to test the supports() method
 * with different converter types to ensure full branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class ApiResponseWrapperAdviceUnitTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private ServerHttpRequest serverHttpRequest;

    @Mock
    private ServerHttpResponse serverHttpResponse;

    private ApiResponseWrapperAdvice advice;

    @BeforeEach
    void setUp() {
        advice = new ApiResponseWrapperAdvice(httpServletRequest);
    }

    @Test
    void shouldSupportJacksonJsonConverter() throws Exception {
        // Given: Method parameter returning Map (not String or ApiResponse)
        when(methodParameter.getParameterType()).thenReturn((Class) Map.class);

        // When: Using MappingJackson2HttpMessageConverter (contains "Json")
        boolean supports = advice.supports(methodParameter, MappingJackson2HttpMessageConverter.class);

        // Then: Should support (true)
        assertThat(supports).isTrue();
    }

    @Test
    void shouldSupportJacksonConverterWithJacksonInName() throws Exception {
        // Given: Method parameter returning Map (not String or ApiResponse)
        when(methodParameter.getParameterType()).thenReturn((Class) Map.class);

        // When: Using a converter with "Jackson" in the name
        boolean supports = advice.supports(methodParameter, MockJacksonConverter.class);

        // Then: Should support (true) - this covers the second branch of the OR condition
        assertThat(supports).isTrue();
    }

    @Test
    void shouldSupportJsonConverterWithoutJacksonInName() throws Exception {
        // Given: Method parameter returning Map (not String or ApiResponse)
        when(methodParameter.getParameterType()).thenReturn((Class) Map.class);

        // When: Using a converter with "Json" but not "Jackson" in the name
        boolean supports = advice.supports(methodParameter, MockJsonConverter.class);

        // Then: Should support (true) - this covers the first branch of the OR condition
        assertThat(supports).isTrue();
    }

    @Test
    void shouldNotSupportNonJacksonConverter() throws Exception {
        // Given: Method parameter returning Map (not String or ApiResponse)
        when(methodParameter.getParameterType()).thenReturn((Class) Map.class);

        // When: Using StringHttpMessageConverter (doesn't contain "Json" or "Jackson")
        boolean supports = advice.supports(methodParameter, StringHttpMessageConverter.class);

        // Then: Should not support (false)
        assertThat(supports).isFalse();
    }

    @Test
    void shouldNotSupportStringReturnType() throws Exception {
        // Given: Method parameter returning String
        when(methodParameter.getParameterType()).thenReturn((Class) String.class);

        // When: Using any converter (even Jackson)
        boolean supports = advice.supports(methodParameter, MappingJackson2HttpMessageConverter.class);

        // Then: Should not support (false) because return type is String
        assertThat(supports).isFalse();
    }

    @Test
    void shouldNotSupportApiResponseReturnType() throws Exception {
        // Given: Method parameter returning ApiResponse
        when(methodParameter.getParameterType()).thenReturn((Class) ApiResponse.class);

        // When: Using any converter (even Jackson)
        boolean supports = advice.supports(methodParameter, MappingJackson2HttpMessageConverter.class);

        // Then: Should not support (false) because return type is ApiResponse
        assertThat(supports).isFalse();
    }

    @Test
    void shouldNotSupportProblemDetailReturnType() throws Exception {
        // Given: Method parameter returning ProblemDetail
        when(methodParameter.getParameterType()).thenReturn((Class) ProblemDetail.class);

        // When: Using any converter (even Jackson)
        boolean supports = advice.supports(methodParameter, MappingJackson2HttpMessageConverter.class);

        // Then: Should not support (false) because return type is ProblemDetail
        assertThat(supports).isFalse();
    }

    @Test
    void shouldNotSupportNonHttpMessageConverterUsingReflection() throws Exception {
        // Given: Method parameter returning Map (not String or ApiResponse)
        when(methodParameter.getParameterType()).thenReturn((Class) Map.class);

        // When: Using reflection to bypass type safety and test with a non-HttpMessageConverter class
        Method supportsMethod = ApiResponseWrapperAdvice.class.getMethod(
                "supports",
                MethodParameter.class,
                Class.class);

        // Use String.class which doesn't implement HttpMessageConverter
        Object result = supportsMethod.invoke(advice, methodParameter, String.class);

        // Then: Should not support (false) because converter is not an HttpMessageConverter
        assertThat(result)
                .asInstanceOf(type(Boolean.class))
                .isEqualTo(false);
    }

    @Test
    void shouldReturnApiResponseUnchangedWhenBodyIsAlreadyApiResponse() {
        // Given: An ApiResponse object as the body
        ApiResponse<String> existingApiResponse = ApiResponse.success(
                "test data",
                "test message",
                "/test");

        // When: beforeBodyWrite is called with an ApiResponse body
        Object result = advice.beforeBodyWrite(
                existingApiResponse,
                methodParameter,
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                serverHttpRequest,
                serverHttpResponse
        );

        // Then: Should return the same ApiResponse unchanged (covers the true branch of body instanceof ApiResponse)
        assertThat(result)
                .isSameAs(existingApiResponse)
                .asInstanceOf(type(ApiResponse.class))
                .satisfies(response -> {
                    assertThat(response.data()).isEqualTo("test data");
                    assertThat(response.message()).isEqualTo("test message");
                    assertThat(response.path()).isEqualTo("/test");
                });
    }

    @Test
    void shouldReturnProblemDetailUnchangedWhenBodyIsProblemDetail() {
        // Given: A ProblemDetail object as the body
        ProblemDetail problemDetail = ProblemDetail.forStatus(404);
        problemDetail.setDetail("Resource not found");

        // When: beforeBodyWrite is called with a ProblemDetail body
        Object result = advice.beforeBodyWrite(
                problemDetail,
                methodParameter,
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                serverHttpRequest,
                serverHttpResponse
        );

        // Then: Should return the same ProblemDetail unchanged
        assertThat(result)
                .isSameAs(problemDetail)
                .asInstanceOf(type(ProblemDetail.class))
                .satisfies(pd -> {
                    assertThat(pd.getStatus()).isEqualTo(404);
                    assertThat(pd.getDetail()).isEqualTo("Resource not found");
                });
    }
}
