package com.ecommerce.api_gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Stamps every request with a correlation ID (reusing an inbound one if the
 * client already supplied it) so a single request can be traced across every
 * service's logs.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String existing = request.getHeader(HEADER);
        String requestId = (existing == null || existing.isBlank()) ? UUID.randomUUID().toString() : existing;
        String finalRequestId = requestId;

        response.setHeader(HEADER, requestId);
        MDC.put(MDC_KEY, requestId);
        try {
            HttpServletRequest wrapped = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    return HEADER.equalsIgnoreCase(name) ? finalRequestId : super.getHeader(name);
                }
            };
            filterChain.doFilter(wrapped, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
