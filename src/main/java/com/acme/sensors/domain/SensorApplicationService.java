package com.acme.sensors.domain;

import com.acme.sensors.domain.SensorMeasurement.CollectNewMeasurement;
import com.acme.sensors.domain.SensorMeasurement.MeasurementCollected;
import com.acme.sensors.domain.SensorMeasurement.MeasurementEventPublisher;
import com.acme.sensors.domain.SensorMetrics.SensorMetric;
import com.acme.sensors.domain.SensorState.CurrentState;
import com.acme.sensors.domain.SensorState.SensorStateRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.acme.sensors.domain.SensorMetrics.*;

@Service
public class SensorApplicationService {

    private final MeasurementEventPublisher eventPublisher;
    private SensorStateRepository stateRepository;
    private SensorMetricRepository metricRepository;

    public SensorApplicationService(final MeasurementEventPublisher eventPublisher,
                                    final SensorStateRepository stateRepository,
                                    final SensorMetricRepository metricRepository) {
        this.eventPublisher = eventPublisher;
        this.stateRepository = stateRepository;
        this.metricRepository = metricRepository;
    }

    public Mono<MeasurementCollected> collectNewMeasurement(final CollectNewMeasurement command) {
        return eventPublisher.publish(
                new MeasurementCollected(
                        command.uuid(),
                        command.co2(),
                        command.time()));
    }

    public Mono<CurrentState> currentStateFor(final String uuid) {
        return stateRepository.currentStateFor(uuid);
    }


    public Mono<SensorMetric> metricsForTheLast30Days(final String anUuid) {
        return metricRepository.forTheLast30Days(anUuid);
    }

}