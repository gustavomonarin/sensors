package com.acme.sensors.infrastructure;


import com.acme.sensors.domain.SensorMeasurement.MeasurementCollected;
import com.acme.sensors.domain.SensorMetrics.SensorMetric;
import com.acme.sensors.infrastructure.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;

@EnableKafkaStreams
@Configuration(proxyBeanMethods = false)
public class KafkaSensorMetricTopologyProvider {

    @Bean
    public KTable<Windowed<String>, SensorMetric> sensorMetricsTopology(
            final KafkaConfig config,
            final KStream<String, MeasurementCollected> measurementsStream,
            final ObjectMapper objectMapper) {


        return
                measurementsStream
                        .groupByKey()
                        .windowedBy(
                                TimeWindows.of(
                                        Duration.ofDays(1)
                                ))
                        .aggregate(
                                () -> SensorMetric.MISSING,
                                (k, measurement, metric) -> metric.addMeasurement(k, measurement.co2()),
                                Materialized.<String, SensorMetric>as(
                                        Stores.persistentWindowStore(
                                                config.getSensorMetricsStateStoreName(),
                                                Duration.ofDays(45),
                                                Duration.ofDays(1),
                                                false
                                        ))
                                        .withKeySerde(Serdes.String())
                                        .withValueSerde(new JsonSerde<>(SensorMetric.class, objectMapper))
                        );


    }

}
