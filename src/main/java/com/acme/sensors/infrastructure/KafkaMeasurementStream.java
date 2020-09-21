package com.acme.sensors.infrastructure;

import com.acme.sensors.domain.SensorMeasurement.MeasurementCollected;
import com.acme.sensors.infrastructure.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.processor.TimestampExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

@Component
public class KafkaMeasurementStream {

    @Bean
    KStream<String, MeasurementCollected> measurementsStream(final KafkaConfig config,
                                                             final StreamsBuilder builder,
                                                             final ObjectMapper objectMapper) {

        return
                builder
                        .stream(
                                config.getSensorMeasurementsTopic(),
                                Consumed.with(
                                        Serdes.String(),
                                        new JsonSerde<>(
                                                MeasurementCollected.class,
                                                objectMapper))
                                        .withTimestampExtractor(new MeasurementCollectedTimeExtractor())
                        );

    }


    private static class MeasurementCollectedTimeExtractor implements TimestampExtractor {
        @Override
        public long extract(ConsumerRecord<Object, Object> record, long partitionTime) {
            MeasurementCollected measurement = (MeasurementCollected) record.value();
            return measurement.time().toInstant().toEpochMilli();
        }
    }
}
