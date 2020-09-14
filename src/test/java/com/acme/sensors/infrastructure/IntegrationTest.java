package com.acme.sensors.infrastructure;

import org.springframework.boot.test.context.SpringBootTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(properties = {
        "spring.kafka.streams.replication-factor=1"
})
@ConfiguredEmbeddedKafka
public @interface IntegrationTest {
}
