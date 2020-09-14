package com.acme.sensors.domain;

import com.acme.sensors.domain.SensorMeasurement.CollectNewMeasurement;
import com.acme.sensors.domain.SensorMeasurement.MeasurementCollected;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SensorApplicationService {

    public interface MeasurementEventPublisher {
        Mono<MeasurementCollected> publish(MeasurementCollected measurement);
    }

    private final MeasurementEventPublisher eventPublisher;

    public SensorApplicationService(MeasurementEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public Mono<MeasurementCollected> collectNewMeasurement(CollectNewMeasurement command) {
        return eventPublisher.publish(
                new MeasurementCollected(
                        command.uuid(),
                        command.co2(),
                        command.time()));
    }

}
