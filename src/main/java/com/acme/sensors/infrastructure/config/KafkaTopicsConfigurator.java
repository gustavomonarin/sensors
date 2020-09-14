package com.acme.sensors.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class KafkaTopicsConfigurator {

    @Bean
    NewTopic sensorsData(KafkaConfig config){
        return new NewTopic(
                config.getSensorMeasurementsTopic(),
                3,
                (short) 1
        );
    }

    @Bean
    NewTopic stateEvents(KafkaConfig config){
        return new NewTopic(
                config.getSensorStateEventsTopic(),
                3,
                (short) 1
        );
    }
}
