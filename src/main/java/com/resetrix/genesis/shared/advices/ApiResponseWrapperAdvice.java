package com.resetrix.genesis.shared.advices;

import com.resetrix.genesis.shared.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

@RestControllerAdvice
public class ApiResponseWrapperAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiResponseWrapperAdvice.class);

    private static final Pattern XSS_PATTERN = Pattern.compile(
        ".*(<script|<iframe|javascript:|onload=|onerror=|onclick=).*",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private final HttpServletRequest httpServletRequest;

    // Constructor injection (Spring will provide HttpServletRequest automatically)
    public ApiResponseWrapperAdvice(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> type = returnType.getParameterType();

        // Skip wrapping if already ApiResponse, ProblemDetail, or a plain String
        if (ApiResponse.class.isAssignableFrom(type)
            || ProblemDetail.class.isAssignableFrom(type)
            || String.class.equals(type)) {
            return false;
        }

        boolean isConverterTypeJackson = converterType.getSimpleName().contains("Json")
            || converterType.getSimpleName().contains("Jackson");

        return HttpMessageConverter.class.isAssignableFrom(converterType)
            && isConverterTypeJackson;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        if (body instanceof ApiResponse
            || body instanceof ProblemDetail) {
            return body;
        }

        return ApiResponse.success(
            body,
            null,
            sanitizeRequestPath(httpServletRequest.getRequestURI())
        );
    }

    private String sanitizeRequestPath(String path) {
        if (XSS_PATTERN.matcher(path).matches()) {
            LOGGER.warn("Potential XSS detected in request path: {}", path);
            return "/";
        }
        return HtmlUtils.htmlEscape(path.trim());
    }
}
