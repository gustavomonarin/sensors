package com.acme.sensors.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration(proxyBeanMethods = false)
public class JavaRecordsConfigurator {

    /*
     * Since java 14 has not been released yet and java records is a preview feature of java 14,
     * it is not fully supported by the serialization frameworks. hence this customization expose
     * private fields during serialization.
     *
     * UPDATE: should be already fixed by https://github.com/FasterXML/jackson-databind/issues/2709
     */
    @Bean
    Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return customizer ->
                customizer.visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    /*
     * Default kafka producer factory does not use the ObjctMapper bean, failing for the same issue as
     * in the previous jackson customizer.
     * This producer factory uses the globally configured object mapper instead.
     * As of spring boot 2.2 auto-configuration related to transactions is not performed.
     * This has to be reviewed this in case use of transactions
     */
    @Bean
    public DefaultKafkaProducerFactory<?,?> kafkaProducerFactory(
            KafkaProperties properties,
            ObjectMapper objectMapper) {
        return new DefaultKafkaProducerFactory<>(properties.buildProducerProperties(),
                new StringSerializer(),
                new JsonSerializer<>(objectMapper));
    }


}
