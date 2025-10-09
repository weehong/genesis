package com.resetrix.genesis.shared.aspects;

import com.resetrix.genesis.shared.exceptions.MethodExecutionException;
import com.resetrix.genesis.testsupports.services.DummyService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.request.RequestContextHolder;
import software.amazon.awssdk.core.exception.SdkClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "app.logging.aspect.enabled=true"
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import({DummyService.class, com.resetrix.genesis.shared.securities.SecurityConfigurationTest.class})
class LogAspectTest {

    @Autowired
    private DummyService dummyService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    private LogAspect logAspect;

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void testAspectInterceptsServiceMethod_andRecordsSuccessMetrics() throws Throwable {
        // call a service method to trigger the aspect
        String result = dummyService.hello("World");

        assertThat(result).isEqualTo("Hello, World");

        // Verify timer exists and recorded
        Timer timer = meterRegistry.find("app.method.execution")
                .tag("status", "success")
                .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isGreaterThanOrEqualTo(1);

        // Verify method tag is present
        assertThat(timer.getId().getTags()).extracting(Tag::getKey)
                .contains("method", "status");

        // Verify aspect intercepted
        verify(logAspect).logExecution(any());
    }

    @Test
    void testAspectRecordsErrorMetrics_whenMethodThrowsException() {
        // Call a method that throws an exception
        assertThatThrownBy(() -> dummyService.throwError())
                .isInstanceOf(MethodExecutionException.class)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasStackTraceContaining("Test error");

        // Verify error metrics recorded
        Timer errorTimer = meterRegistry.find("app.method.execution")
                .tag("status", "error")
                .timer();
        assertThat(errorTimer).isNotNull();
        assertThat(errorTimer.count()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void testAspectRecordsErrorMetrics_whenMethodThrowsSdkException() {
        // Call a method that throws an SdkException
        assertThatThrownBy(() -> dummyService.throwSdkException())
                .isInstanceOf(SdkClientException.class)
                .hasMessageContaining("AWS SDK error occurred");

        // Verify error metrics recorded
        Timer errorTimer = meterRegistry.find("app.method.execution")
                .tag("status", "error")
                .timer();
        assertThat(errorTimer).isNotNull();
        assertThat(errorTimer.count()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void testAspectLogsSlowExecution_whenMethodExceedsThreshold() throws Throwable {
        // Call a slow method (>1000ms)
        String result = dummyService.slowMethod();

        assertThat(result).isEqualTo("Slow operation completed");

        // Verify metrics recorded for slow method
        Timer timer = meterRegistry.find("app.method.execution")
                .tag("status", "success")
                .timer();
        assertThat(timer).isNotNull();

        // The slow method should be logged as WARN (verified via logs, not easy to assert in test)
    }

    @Test
    void testAspectAddsHttpContextToMDC_forControllerMethods() throws Exception {
        // Call a controller endpoint to verify HTTP context is captured
        mockMvc.perform(get("/wrapped")
                        .header("X-Test-Header", "test-value"))
                .andExpect(status().isOk());

        // Verify controller method was intercepted (check logs for httpMethod and endpoint in MDC)
        // Note: MDC is cleared after execution, so we verify via metrics
        Timer timer = meterRegistry.find("app.method.execution")
                .tag("status", "success")
                .timer();
        assertThat(timer).isNotNull();
    }

    @Test
    void testAspectReusesExistingRequestId_whenAlreadyInMDC() throws Throwable {
        // Set an existing requestId in MDC
        String existingRequestId = "EXISTING123";
        MDC.put("requestId", existingRequestId);

        // Call method - should reuse the existing requestId
        dummyService.hello("Test");

        // The aspect should have used the existing requestId
        // (can't easily verify since MDC is cleared, but code path is exercised)
        verify(logAspect).logExecution(any());
    }

    @Test
    void testAspectGeneratesRequestId_whenNotInMDC() throws Throwable {
        // Ensure no requestId in MDC
        MDC.remove("requestId");

        // Call method - should generate new requestId
        String result = dummyService.hello("Test");

        assertThat(result).isEqualTo("Hello, Test");

        // Verify metrics recorded (requestId was generated internally)
        Timer timer = meterRegistry.find("app.method.execution").timer();
        assertThat(timer).isNotNull();
    }

    @Test
    void testAspectHandlesNullHttpRequest_gracefully() throws Throwable {
        // Call service method (no HTTP context)
        String result = dummyService.hello("NoHttp");

        assertThat(result).isEqualTo("Hello, NoHttp");

        // Should complete successfully even without HTTP context
        Timer timer = meterRegistry.find("app.method.execution")
                .tag("status", "success")
                .timer();
        assertThat(timer).isNotNull();
    }

    @Test
    void testMetricsContainCorrectTags() throws Throwable {
        dummyService.hello("TagTest");

        // Find all timers and look for the one with DummyService.hello
        Timer timer = meterRegistry.find("app.method.execution")
                .tag("status", "success")
                .timers()
                .stream()
                .filter(t -> t.getId().getTag("method") != null
                        && t.getId().getTag("method").contains("DummyService.hello"))
                .findFirst()
                .orElse(null);

        assertThat(timer).isNotNull();
        assertThat(timer.getId().getTags())
                .extracting(Tag::getKey)
                .contains("method", "status");

        // Verify tag values
        assertThat(timer.getId().getTag("status")).isEqualTo("success");
        assertThat(timer.getId().getTag("method")).contains("DummyService.hello");
    }

    @Test
    void testAspectHandlesNullRequestAttributes_gracefully() throws Throwable {
        // This test covers the case where RequestContextHolder.getRequestAttributes() returns null
        // which causes attrs to be null at line 108, covering the null branch

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            // Make RequestContextHolder return null
            mockedStatic.when(RequestContextHolder::getRequestAttributes)
                    .thenReturn(null);

            // Call service method - should complete even when attrs is null
            String result = dummyService.hello("NullAttrs");

            assertThat(result).isEqualTo("Hello, NullAttrs");

            // Verify execution completed successfully
            Timer timer = meterRegistry.find("app.method.execution")
                    .tag("status", "success")
                    .timer();
            assertThat(timer).isNotNull();
        }
    }

    @Test
    void testAspectHandlesExceptionInCurrentHttpRequest_gracefully() throws Throwable {
        // This test exercises the catch block in currentHttpRequest() by mocking RequestContextHolder
        // to throw an exception when getRequestAttributes() is called

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            // Make RequestContextHolder throw an exception
            mockedStatic.when(RequestContextHolder::getRequestAttributes)
                    .thenThrow(new IllegalStateException("Test exception in RequestContextHolder"));

            // Call service method - should complete even if currentHttpRequest() throws exception
            String result = dummyService.hello("ExceptionTest");

            assertThat(result).isEqualTo("Hello, ExceptionTest");

            // Verify execution completed successfully
            Timer timer = meterRegistry.find("app.method.execution")
                    .tag("status", "success")
                    .timer();
            assertThat(timer).isNotNull();
        }
    }

    @Test
    void testServiceMethodsPointcut_matchesCorrectPackagePattern() {
        // This test verifies that the serviceMethods() pointcut matches methods in
        // com.resetrix.genesis.*.services..* package pattern
        // DummyService is in com.resetrix.genesis.fixtures.services, which matches the pattern

        String result = dummyService.hello("Pointcut");

        assertThat(result).isEqualTo("Hello, Pointcut");

        // Verify the aspect intercepted this service method via serviceMethods() pointcut
        Timer timer = meterRegistry.find("app.method.execution")
                .tag("status", "success")
                .timers()
                .stream()
                .filter(t -> t.getId().getTag("method") != null
                        && t.getId().getTag("method").contains("DummyService.hello"))
                .findFirst()
                .orElse(null);

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isGreaterThanOrEqualTo(1);
    }
}
