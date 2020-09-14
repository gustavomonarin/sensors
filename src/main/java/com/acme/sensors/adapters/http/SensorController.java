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

    private SensorApplicationService applicationService;

    public SensorController(SensorApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping(value = "{uuid}/mesurements", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity> newMeasurement(@PathVariable String uuid, @RequestBody CollectMeasurementRequest req) {
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
    public Mono<ResponseEntity<SensorStatusResponse>> status(@PathVariable String uuid) {
        return
                applicationService
                        .currentStateFor(uuid)
                        .map(currentState ->
                                ResponseEntity.ok()
                                        .body(SensorStatusResponse
                                                .fromState(currentState)));
    }


}
