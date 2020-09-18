package com.acme.sensors.domain;

public abstract class SensorMetrics {

    public record SensorMetric(String uuid, Integer maxLast30Days, Integer avgLast30Days ){

    }

}
