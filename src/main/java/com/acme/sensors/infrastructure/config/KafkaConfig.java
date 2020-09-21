package com.acme.sensors.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("com.acme.sensors.kafka")
public class KafkaConfig {

    private String sensorMeasurementsTopic = "sensors-measurements";
    private String sensorStateStateStoreName = "state";
    private String sensorStateEventsTopic = "sensors-events";
    private String sensorMetricsStateStoreName = "metrics";

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

    public String getSensorMetricsStateStoreName() {
        return sensorMetricsStateStoreName;
    }

    public void setSensorMetricsStateStoreName(String sensorMetricsStateStoreName) {
        this.sensorMetricsStateStoreName = sensorMetricsStateStoreName;
    }
}
