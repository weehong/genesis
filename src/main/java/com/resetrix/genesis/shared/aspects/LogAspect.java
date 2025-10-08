package com.resetrix.genesis.shared.aspects;

import com.resetrix.genesis.shared.exceptions.MethodExecutionException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import software.amazon.awssdk.core.exception.SdkException;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.resetrix.genesis.shared.constants.AspectConstants.SLOW_EXECUTION_THRESHOLD_MS;

@Aspect
@Component
@ConditionalOnProperty(
        prefix = "app.logging.aspect",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class LogAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogAspect.class);

    private final MeterRegistry meterRegistry;

    public LogAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Pointcut("execution(public * com.resetrix.genesis..services..*(..))")
    public void serviceMethods() {
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) || "
            + "within(@org.springframework.stereotype.Controller *)")
    public void controllerMethods() {
    }

    @Around("serviceMethods() || controllerMethods()")
    public Object logExecution(ProceedingJoinPoint pjp) throws Throwable {
        long startNs = System.nanoTime();
        String method = pjp.getSignature().toShortString();

        // Correlation ID
        String requestId = initRequestId();
        MDC.put("requestId", requestId);
        MDC.put("method", method);

        // Add HTTP context if available
        HttpServletRequest request = currentHttpRequest();
        if (request != null) {
            MDC.put("httpMethod", request.getMethod());
            MDC.put("endpoint", request.getRequestURI());
        }

        try {
            Object result = pjp.proceed();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

            recordMetrics(method, durationMs, false);
            logSuccess(method, durationMs);

            return result;
        } catch (SdkException ex) {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

            recordMetrics(method, durationMs, true);
            LOGGER.error("Method {} failed in {}ms: {}", method, durationMs, ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

            recordMetrics(method, durationMs, true);
            LOGGER.error("Method {} failed in {}ms: {}", method, durationMs, ex.getMessage(), ex);
            throw new MethodExecutionException(method, durationMs, ex);
        } finally {
            MDC.clear();
        }
    }

    private void logSuccess(String method, long durationMs) {
        if (durationMs > SLOW_EXECUTION_THRESHOLD_MS) {
            LOGGER.warn("Method {} completed in {}ms (SLOW)", method, durationMs);
        } else {
            LOGGER.info("Method {} completed in {}ms", method, durationMs);
        }
    }

    private void recordMetrics(String method, long durationMs, boolean failed) {
        Timer.builder("app.method.execution")
                .description("Service/Controller execution times")
                .tag("method", method)
                .tag("status", failed ? "error" : "success")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    private String initRequestId() {
        String existing = MDC.get("requestId");
        return (existing != null)
                ? existing
                : UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private HttpServletRequest currentHttpRequest() {
        try {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes servletRequestAttributes) {
                return servletRequestAttributes.getRequest();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
