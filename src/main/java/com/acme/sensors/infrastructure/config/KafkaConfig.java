package com.acme.sensors.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("com.acme.sensors.kafka")
public class KafkaConfig {

    private String sensorMeasurementsTopic = "sensor-measurements";
    private String sensorStateStateStoreName = "sensor-status";
    private String sensorStateEventsTopic = "sensor-events";



    public String getSensorMeasurementsTopic() {
        return sensorMeasurementsTopic;
    }

    public void setSensorMeasurementsTopic(String sensorMeasurementsTopic) {
        this.sensorMeasurementsTopic = sensorMeasurementsTopic;
    }

    public String getSensorStateStateStoreName() {
        return sensorStateStateStoreName;
    }

    public void setSensorStateStateStoreName(String sensorStateStateStoreName) {
        this.sensorStateStateStoreName = sensorStateStateStoreName;
    }

    public String getSensorStateEventsTopic() {
        return sensorStateEventsTopic;
    }

    public void setSensorStateEventsTopic(String sensorStateEventsTopic) {
        this.sensorStateEventsTopic = sensorStateEventsTopic;
    }
}
