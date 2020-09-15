package com.acme.sensors.adapters.http;

import com.acme.sensors.domain.SensorState;

import static com.acme.sensors.domain.SensorState.Status.*;

public class SensorStatusResponse {

    private String status;

    public SensorStatusResponse() {
    }

    public SensorStatusResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static SensorStatusResponse fromState(SensorState.CurrentState currentState) {
        String status;
        if (currentState.status() == OK) {
            status = "OK";
        } else if (currentState.status() == ALERT) {
            status = "ALERT";
        } else {
            status = "WARN";
        }

        return new SensorStatusResponse(status);
    }
}
