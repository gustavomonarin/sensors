package com.acme.sensors.adapters.http;

import com.acme.sensors.domain.SensorState;

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
        String state = switch (currentState.status()) {
            case OK -> "OK";
            case ALERT -> "ALERT";
            case ESCALATED, WARN -> "WARN";
        };

        return new SensorStatusResponse(state);
    }
}
