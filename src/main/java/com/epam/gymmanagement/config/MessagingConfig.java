package com.epam.gymmanagement.config;

import com.epam.gymmanagement.dto.request.TrainerWorkloadRequestDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.Map;

@Configuration(proxyBeanMethods = false)
public class MessagingConfig {

    public static final String WORKLOAD_EVENT_TYPE = "trainerWorkloadEvent";

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setTypeIdMappings(Map.of(WORKLOAD_EVENT_TYPE, TrainerWorkloadRequestDTO.class));
        return converter;
    }
}
