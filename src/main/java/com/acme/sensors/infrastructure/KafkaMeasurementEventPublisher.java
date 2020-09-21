package com.acme.sensors.infrastructure;

import com.acme.sensors.domain.SensorMeasurement.MeasurementCollected;
import com.acme.sensors.domain.SensorMeasurement.MeasurementEventPublisher;
import com.acme.sensors.infrastructure.config.KafkaConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class KafkaMeasurementEventPublisher implements MeasurementEventPublisher {

    private final KafkaTemplate<String, MeasurementCollected> producer;
    private final KafkaConfig config;

    public KafkaMeasurementEventPublisher(final KafkaTemplate<String, MeasurementCollected> producer,
                                          final KafkaConfig config) {
        this.producer = producer;
        this.config = config;
    }

    @Override
    public Mono<MeasurementCollected> publish(final MeasurementCollected measurement) {
        return
                Mono.fromFuture(
                        producer.send(config.getSensorMeasurementsTopic(), measurement.uuid(), measurement)
                                .completable()
                                .thenApply(result -> result.getProducerRecord().value())
                );
    }
}
