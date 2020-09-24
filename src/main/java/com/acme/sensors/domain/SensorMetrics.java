package com.acme.sensors.domain;

import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public abstract class SensorMetrics {

    public interface SensorMetricRepository {
        Mono<SensorMetric> forTheLast30Days(String anUuid);
    }

    public record SensorMetric(
            String uuid,
            Integer measurements,
            Integer sum,
            Integer max) {

        public static final SensorMetric MISSING = new SensorMetric(null, 0, 0, 0);

        public static Optional<SensorMetric> fromRange(final Iterator<SensorMetric> metrics) {

            return StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(metrics, 0), false)
                    .reduce(
                            SensorMetric::mergeMeasurement);

        }

        public SensorMetric mergeMeasurement(final SensorMetric anotherMeasurement) {
            return new SensorMetric(
                    this.uuid(),
                    this.measurementsSummed(anotherMeasurement.measurements()),
                    this.sum(anotherMeasurement.sum()),
                    this.max(anotherMeasurement.max())
            );
        }

        public SensorMetric addMeasurement(final String uuid, final Integer newMeasurement) {
            if (this.uuid != null && !this.uuid.equals(uuid))
                throw new IllegalArgumentException("the provided new measurement is from a different sensor.");

            return new SensorMetric(
                    uuid,
                    this.measurementsIncremented(),
                    this.sum(newMeasurement),
                    this.max(newMeasurement)
            );
        }

        public Optional<Integer> avg() {
            return measurements > 0 ? Optional.of(sum / measurements) : Optional.empty();
        }

        private Integer sum(Integer anotherMeasurement) {
            return this.sum() + anotherMeasurement;
        }

        private Integer max(Integer anotherMax) {
            return Math.max(this.max(), anotherMax);
        }

        private Integer measurementsIncremented() {
            return this.measurements() + 1;
        }

        private Integer measurementsSummed(Integer anotherMeasurement) {
            return this.measurements() + anotherMeasurement;
        }

    }

}
