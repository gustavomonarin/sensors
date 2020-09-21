package com.acme.sensors.infrastructure;

import com.acme.sensors.domain.SensorMeasurement.MeasurementCollected;
import com.acme.sensors.domain.SensorMeasurement.MeasurementEventPublisher;
import com.acme.sensors.domain.SensorState;
import com.acme.sensors.domain.SensorState.SensorStateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@IntegrationTest
class KafkaSensorStateRepositoryTest {

    @Autowired
    MeasurementEventPublisher eventPublisher;

    @Autowired
    SensorStateRepository repository;

    @Test
    void currentStateFor() {
        String anUuid = UUID.randomUUID().toString();

        MeasurementCollected measurement = new MeasurementCollected(anUuid, 1001, ZonedDateTime.now());
        eventPublisher.publish(measurement);


        await().atMost(Duration.ofSeconds(60))
                .untilAsserted(() -> {
                            SensorState.CurrentState currentState = repository.currentStateFor(anUuid).block();

                            assertThat(currentState).isNotNull();
                            assertThat(currentState.status()).isEqualTo(SensorState.Status.OK);
                        }
                );
    }
}