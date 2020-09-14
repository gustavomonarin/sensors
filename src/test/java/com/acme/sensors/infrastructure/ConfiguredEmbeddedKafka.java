package com.acme.sensors.infrastructure;

import org.springframework.kafka.test.context.EmbeddedKafka;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "sensor-measurements",
                "sensor-events",
        },
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
public @interface ConfiguredEmbeddedKafka {
}
