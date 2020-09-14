package com.acme.sensors.domain;

import com.acme.sensors.adapters.http.SensorStatusResponse;
import com.acme.sensors.domain.SensorMeasurement.CollectNewMeasurement;
import com.acme.sensors.domain.SensorMeasurement.MeasurementCollected;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SensorApplicationService {

    public interface MeasurementEventPublisher {
        Mono<MeasurementCollected> publish(MeasurementCollected measurement);
    }

    public interface SensorStateRepository {
        Mono<SensorState.CurrentState> currentStateFor(String anUuid);
    }

    private final MeasurementEventPublisher eventPublisher;
    private SensorStateRepository stateRepository;

    public SensorApplicationService(MeasurementEventPublisher eventPublisher,
                                    SensorStateRepository stateRepository) {
        this.eventPublisher = eventPublisher;
        this.stateRepository = stateRepository;
    }

    public Mono<MeasurementCollected> collectNewMeasurement(CollectNewMeasurement command) {
        return eventPublisher.publish(
                new MeasurementCollected(
                        command.uuid(),
                        command.co2(),
                        command.time()));
    }

    public Mono<SensorState.CurrentState> currentStateFor(String uuid) {
        return stateRepository.currentStateFor(uuid);
    }

}