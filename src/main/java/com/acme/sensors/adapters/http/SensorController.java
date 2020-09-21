package com.acme.sensors.adapters.http;

import com.acme.sensors.domain.SensorApplicationService;
import com.acme.sensors.domain.SensorMeasurement.CollectNewMeasurement;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/sensors")
public class SensorController {

    private final SensorApplicationService applicationService;

    public SensorController(final SensorApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping(value = "{uuid}/mesurements", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity> newMeasurement(final @PathVariable String uuid,
                                               final @RequestBody CollectMeasurementRequest req) {
        return
                applicationService
                        .collectNewMeasurement(new CollectNewMeasurement(
                                uuid,
                                req.getCo2(),
                                req.getTime()
                        ))
                        .map(__ -> ResponseEntity.ok().build());
    }

    @GetMapping(value = "{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<SensorStatusResponse>> status(final @PathVariable String uuid) {

        return
                applicationService
                        .currentStateFor(uuid)
                        .map(SensorStatusResponse::fromState)
                        .map(ResponseEntity::ok);
    }

    @GetMapping(value = "{uuid}/metrics", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<SensorMetricResponse>> metrics(final @PathVariable String uuid) {

        return
                applicationService
                        .metricsForTheLast30Days(uuid)
                        .map(SensorMetricResponse::fromMetric)
                        .map(ResponseEntity::ok);

    }
}
