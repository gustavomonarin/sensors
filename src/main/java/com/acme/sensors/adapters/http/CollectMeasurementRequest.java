package com.acme.sensors.adapters.http;

import java.time.ZonedDateTime;

public class CollectMeasurementRequest {
    private Integer co2;
    private ZonedDateTime time;

    public CollectMeasurementRequest() {
    }

    public CollectMeasurementRequest(Integer co2, ZonedDateTime time) {
        this.co2 = co2;
        this.time = time;
    }

    public Integer getCo2() {
        return co2;
    }

    public void setCo2(Integer co2) {
        this.co2 = co2;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public void setTime(ZonedDateTime time) {
        this.time = time;
    }
}
