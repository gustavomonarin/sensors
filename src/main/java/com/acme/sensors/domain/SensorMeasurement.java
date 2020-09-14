package com.acme.sensors.domain;

import java.time.ZonedDateTime;

public class SensorMeasurement {
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
