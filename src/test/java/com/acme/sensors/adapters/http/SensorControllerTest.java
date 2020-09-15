package com.acme.sensors.adapters.http;

import com.acme.sensors.domain.SensorApplicationService;
import com.acme.sensors.domain.SensorMeasurement.MeasurementCollected;
import com.acme.sensors.domain.SensorState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;

import static com.acme.sensors.domain.SensorMeasurement.CollectNewMeasurement;
import static com.acme.sensors.domain.SensorState.Status.OK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

@AutoConfigureRestDocs
@WebFluxTest
class SensorControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private SensorApplicationService applicationService;

    @Test
    void collectMeasurement() {

        CollectMeasurementRequest req = new CollectMeasurementRequest(1001, ZonedDateTime.now());

        CollectNewMeasurement command = new CollectNewMeasurement("my-uuid", req.getCo2(), req.getTime());
        when(applicationService.collectNewMeasurement(any()))
                .thenReturn(Mono.just(new MeasurementCollected("my-uuid", req.getCo2(), req.getTime())));

        webTestClient.post()
                .uri("/api/v1/sensors/my-uuid/mesurements")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody().consumeWith(document("collect-measurement")
        );
    }

    @Test
    void status() {
        when(applicationService.currentStateFor("my-uuid"))
                .thenReturn(Mono.just(new SensorState.CurrentState("my-uuid", OK, 0)));

        webTestClient.get()
                .uri("/api/v1/sensors/my-uuid")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.status").isEqualTo("OK")
                .consumeWith(document("current-status"));


    }

}