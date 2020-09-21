package com.acme.sensors.domain;

import com.acme.sensors.domain.SensorMetrics.SensorMetric;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SensorMetricsTest {

    private final String uuid1 = UUID.randomUUID().toString();
    private final String uuid2 = UUID.randomUUID().toString();

    @Test
    void metricShouldBeOfTheSameSensor() {
        var sensorMetric = new SensorMetric(uuid1, 0, 0, 0);

        assertThrows(IllegalArgumentException.class, () ->
                sensorMetric.addMeasurement(uuid2, 1));
    }

    @Test
    void avg() {
        var sensorMetric = new SensorMetric(uuid1, 0, 0, 0);

        var updatedMetric = sensorMetric.addMeasurement(uuid1, 10);

        assertThat(updatedMetric.avg()).isEqualTo(Optional.of(10));

        var updatedMetric2 = updatedMetric.addMeasurement(uuid1, 20)
                .addMeasurement(uuid1, 10)
                .addMeasurement(uuid1, 20);
        assertThat(updatedMetric2.avg()).isEqualTo(Optional.of(15));
    }

    @Test
    void max() {

        var metric = SensorMetric.MISSING.addMeasurement(uuid1, 10)
                .addMeasurement(uuid1, 20)
                .addMeasurement(uuid1, 30)
                .addMeasurement(uuid1, 10)
                .addMeasurement(uuid1, 20);

        assertThat(metric.max()).isEqualTo(30);
    }


    @Test
    void fromRange() {
        var day1 = SensorMetric.MISSING
                .addMeasurement(uuid1, 10)
                .addMeasurement(uuid1, 20);

        var day2 = SensorMetric.MISSING
                .addMeasurement(uuid1, 30)
                .addMeasurement(uuid1, 40)
                .addMeasurement(uuid1, 50);

        var day3 = SensorMetric.MISSING
                .addMeasurement(uuid1, 50)
                .addMeasurement(uuid1, 40)
                .addMeasurement(uuid1, 30)
                .addMeasurement(uuid1, 20)
                .addMeasurement(uuid1, 10);

        var sensorMetric = SensorMetric
                .fromRange(
                        List.of(
                                day1,
                                day2,
                                day3
                        ).iterator());

        assertThat(sensorMetric.avg()).isEqualTo(Optional.of(30));
        assertThat(sensorMetric.max()).isEqualTo(50);

    }

}