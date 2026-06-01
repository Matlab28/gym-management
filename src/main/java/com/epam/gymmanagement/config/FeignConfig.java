package com.epam.gymmanagement.config;

import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor transactionIdRequestInterceptor() {
        return requestTemplate -> {
            String transactionId = MDC.get("transactionId");

            if (transactionId != null) {
                requestTemplate.header("X-Transaction-Id", transactionId);
            }
        };
    }
}