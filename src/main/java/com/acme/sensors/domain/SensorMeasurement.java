package com.acme.sensors.domain;

import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;

public class SensorMeasurement {

    public interface MeasurementEventPublisher {
        Mono<MeasurementCollected> publish(MeasurementCollected measurement);
    }

    public static record CollectNewMeasurement(
            String uuid,
            Integer co2,
            ZonedDateTime time) implements Definitions.Command {
    }

    public static record MeasurementCollected (
            String uuid,
            Integer co2,
            ZonedDateTime time) implements Definitions.Event {
    }
}
