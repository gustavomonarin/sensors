package com.acme.sensors.domain;

import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.Optional;

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

        public static SensorMetric fromRange(final Iterator<SensorMetric> metrics) {

            return aggregateRange(MISSING, metrics);

        }

        private static SensorMetric aggregateRange(final SensorMetric initial, final Iterator<SensorMetric> metrics) {

            var aggregated = initial;

            if (metrics.hasNext()) {
                var current = metrics.next();

                aggregated = aggregateRange(
                        new SensorMetric(
                                initial.uuid,
                                initial.measurements() + current.measurements(),
                                initial.sum() + current.sum(),
                                Math.max(initial.max(), current.max())
                        ),
                        metrics);
            }

            return aggregated;
        }

        public SensorMetric addMeasurement(final String uuid, final Integer newMeasurement) {
            if (this.uuid != null && !this.uuid.equals(uuid))
                throw new IllegalArgumentException("the provided new measurement is from a different sensor.");

            return new SensorMetric(
                    uuid,
                    this.measurements + 1,
                    sum + newMeasurement,
                    Math.max(newMeasurement, max)
            );
        }

        public Optional<Integer> avg() {
            return measurements > 0 ? Optional.of(sum / measurements) : Optional.empty();
        }
    }

}
