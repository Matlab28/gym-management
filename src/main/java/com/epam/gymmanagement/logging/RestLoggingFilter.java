package com.epam.gymmanagement.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RestLoggingFilter extends OncePerRequestFilter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String MASK = "***";
    private static final int MAX_LOGGED_BODY_LENGTH = 4096;
    private static final String[] SENSITIVE_FIELDS = {
            "password",
            "oldPassword",
            "newPassword",
            "token"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 1024 * 1024);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            String requestBody = sanitizeBody(getRequestBody(wrappedRequest));
            String responseBody = sanitizeBody(getResponseBody(wrappedResponse));

            log.info(
                    "REST call: method={}, uri={}, requestBody={}, status={}, responseBody={}, durationMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    requestBody,
                    wrappedResponse.getStatus(),
                    responseBody,
                    duration
            );

            wrappedResponse.copyBodyToResponse();
        }
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();

        if (content.length == 0) {
            return "";
        }

        return new String(content, StandardCharsets.UTF_8);
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();

        if (content.length == 0) {
            return "";
        }

        return new String(content, StandardCharsets.UTF_8);
    }

    private String sanitizeBody(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(body);
            maskSensitiveFields(root);
            return truncate(OBJECT_MAPPER.writeValueAsString(root));
        } catch (Exception exception) {
            return truncate(body);
        }
    }

    private void maskSensitiveFields(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            for (String field : SENSITIVE_FIELDS) {
                if (objectNode.has(field)) {
                    objectNode.put(field, MASK);
                }
            }

            objectNode.elements().forEachRemaining(this::maskSensitiveFields);
            return;
        }

        if (node.isArray()) {
            node.elements().forEachRemaining(this::maskSensitiveFields);
        }
    }

    private String truncate(String value) {
        if (value.length() <= MAX_LOGGED_BODY_LENGTH) {
            return value;
        }

        return value.substring(0, MAX_LOGGED_BODY_LENGTH) + "...";
    }
}
