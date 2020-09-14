package com.acme.sensors.infrastructure;

import com.acme.sensors.domain.SensorApplicationService;
import com.acme.sensors.domain.SensorMeasurement.MeasurementCollected;
import com.acme.sensors.infrastructure.config.KafkaConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class KafkaMeasurementEventPublisher implements SensorApplicationService.MeasurementEventPublisher {


    private final KafkaTemplate<String, MeasurementCollected> producer;
    private final KafkaConfig config;

    public KafkaMeasurementEventPublisher(KafkaTemplate<String, MeasurementCollected> producer, KafkaConfig config) {
        this.producer = producer;
        this.config = config;
    }

    @Override
    public Mono<MeasurementCollected> publish(MeasurementCollected measurement) {
        return
                Mono.fromFuture(
                        producer.send(config.getSensorMeasurementsTopic(), measurement.uuid(), measurement)
                                .completable()
                                .thenApply(result -> result.getProducerRecord().value())
                );
    }
}
