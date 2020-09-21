package com.acme.sensors.infrastructure;

import com.acme.sensors.domain.SensorMeasurement.MeasurementCollected;
import com.acme.sensors.domain.SensorMeasurement.MeasurementEventPublisher;
import com.acme.sensors.domain.SensorMetrics.SensorMetric;
import com.acme.sensors.domain.SensorMetrics.SensorMetricRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@IntegrationTest
class KafkaSensorMetricRepositoryTest {

    @Autowired
    MeasurementEventPublisher eventPublisher;

    @Autowired
    SensorMetricRepository repository;


    private String uuid = UUID.randomUUID().toString();
    private String uuid2 = UUID.randomUUID().toString();


    @Test
    void notFound() {

        eventPublisher.publish(new MeasurementCollected(uuid, 2_000_000, ZonedDateTime.now()));

        var missing = repository.forTheLast30Days(uuid2).block();
        assertThat(missing).isEqualTo(SensorMetric.MISSING);
    }


    @Test
    void metrics() {

        var now = ZonedDateTime.now();
        var yesterday = now.minus(Duration.ofDays(1));
        var moreThanAMonthAgo = yesterday.minusDays(31);

        eventPublisher.publish(new MeasurementCollected(uuid, 2_000_000, moreThanAMonthAgo));
        eventPublisher.publish(new MeasurementCollected(uuid, 10, yesterday.minusMinutes(20)));
        eventPublisher.publish(new MeasurementCollected(uuid, 20, yesterday.minusMinutes(10)));
        eventPublisher.publish(new MeasurementCollected(uuid, 20, now.minusMinutes(10)));
        eventPublisher.publish(new MeasurementCollected(uuid, 10, now.minusMinutes(9)));


        await().atMost(Duration.ofSeconds(60))
                .untilAsserted(() -> {
                            var sensorMetric = repository.forTheLast30Days(uuid).block();

                            assertThat(sensorMetric).isNotNull();
                            assertThat(sensorMetric.max()).isEqualTo(20);
                            assertThat(sensorMetric.avg()).isEqualTo(Optional.of(15));
                        }
                );

        assertThat(repository.forTheLast30Days(uuid2).block()).isEqualTo(SensorMetric.MISSING);

    }
}