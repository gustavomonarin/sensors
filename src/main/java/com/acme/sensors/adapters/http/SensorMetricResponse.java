package com.acme.sensors.adapters.http;

import com.acme.sensors.domain.SensorMetrics.SensorMetric;

public class SensorMetricResponse {

    private int maxLast30Days;
    private int avgLast30Days;

    public SensorMetricResponse(final int maxLast30Days, final int avgLast30Days) {
        this.maxLast30Days = maxLast30Days;
        this.avgLast30Days = avgLast30Days;
    }

    public int getMaxLast30Days() {
        return maxLast30Days;
    }

    public int getAvgLast30Days() {
        return avgLast30Days;
    }

    public static SensorMetricResponse fromMetric(final SensorMetric sensorMetric) {

        return
                new SensorMetricResponse(
                        sensorMetric.max(),
                        sensorMetric.avg().orElse(0)
                );
    }
}
