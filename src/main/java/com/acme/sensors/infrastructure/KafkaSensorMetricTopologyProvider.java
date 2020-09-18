package com.acme.sensors.infrastructure;


import com.acme.sensors.domain.SensorMetrics.SensorMetric;
import com.acme.sensors.infrastructure.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Windows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafkaStreams
@Configuration(proxyBeanMethods = false)
public class KafkaSensorMetricTopologyProvider {

    @Bean
    public KTable<String, SensorMetric> sensorStatusTopology(KafkaConfig config,
                                                             StreamsBuilder builder,
                                                             ObjectMapper objectMapper) {

        builder.stream(config.getSensorMeasurementsTopic())
                .groupByKey()
                .windowedBy(


    }

}
