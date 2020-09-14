package com.acme.sensors.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("com.acme.sensors.kafka")
public class KafkaConfig {

    private String sensorMeasurementsTopic = "sensor-measurements";

    public String getSensorMeasurementsTopic() {
        return sensorMeasurementsTopic;
    }

    public void setSensorMeasurementsTopic(String sensorMeasurementsTopic) {
        this.sensorMeasurementsTopic = sensorMeasurementsTopic;
    }

}
