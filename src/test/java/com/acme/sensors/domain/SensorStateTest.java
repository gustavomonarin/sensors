package com.acme.sensors.domain;

import com.acme.sensors.domain.SensorState.*;
import com.acme.sensors.domain.SensorState.StateEvent.*;
import org.junit.jupiter.api.Test;

import static com.acme.sensors.domain.SensorState.CurrentState.REQ_CONSECUTIVE_HEALTH_TO_RECOVER;
import static com.acme.sensors.domain.SensorState.CurrentState.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

class SensorStateTest {

    UpdateCurrentStateCommandHandler<?> commandHandler = new UpdateCurrentStateCommandHandler<>();

    private final UpdateCurrentState BELLOW_THRESHOLD = new UpdateCurrentState("uuid", 1001);
    private final UpdateCurrentState ABOVE_THRESHOLD = new UpdateCurrentState("uuid", 2001);

    @Test
    void nonActivatedSensorMeasurementBellowThreshold() {

        Definitions.Event newEvent = commandHandler.handle(BELLOW_THRESHOLD, null);
        assertThat(newEvent)
                .isInstanceOfSatisfying(SensorRecovered.class,
                        (recovered) -> assertThat(recovered.uuid()).isEqualTo("uuid"));
    }


    @Test
    void nonActivatedSensorMeasurementAboveThreshold() {
        Definitions.Event newEvent = commandHandler.handle(ABOVE_THRESHOLD, null);
        assertThat(newEvent)
                .isInstanceOfSatisfying(SensorBecameWarning.class,
                        (warning) -> assertThat(warning.uuid()).isEqualTo("uuid"));
    }
//

    @Test
    void healthySensorMeasurementBelowThreshold() {
        CurrentState currentlyHealthy = new CurrentState("uuid", OK, 0);

        Definitions.Event newEvent = commandHandler.handle(BELLOW_THRESHOLD, currentlyHealthy);

        assertThat(newEvent).isNull();
    }

    @Test
    void healthySensorMeasurementAboveThreshold() {
        CurrentState currentlyHealthy = new CurrentState("uuid", OK, 0);

        Definitions.Event newEvent = commandHandler.handle(ABOVE_THRESHOLD, currentlyHealthy);

        assertThat(newEvent).isInstanceOfSatisfying(SensorBecameWarning.class,
                (warning) -> assertThat(warning.uuid()).isEqualTo("uuid"));
    }

    //
    @Test
    void warningSensorMeasureBelowThreshold() {
        CurrentState currentlyWarning = new CurrentState("uuid", WARN, 0);

        StateEvent newEvent = commandHandler.handle(BELLOW_THRESHOLD, currentlyWarning);

        assertThat(newEvent).isInstanceOfSatisfying(SensorRecovered.class,
                (sensorRecovered -> assertThat(sensorRecovered.uuid()).isEqualTo("uuid")));
    }

    @Test
    void warningSensorMeasureAboveThreshold() {
        CurrentState currentlyWarning = new CurrentState("uuid", WARN, 0);

        StateEvent newEvent = commandHandler.handle(ABOVE_THRESHOLD, currentlyWarning);

        assertThat(newEvent).isInstanceOfSatisfying(SensorWarningEscalated.class,
                (escalated -> assertThat(escalated.uuid()).isEqualTo("uuid")));
    }


    //
    @Test
    void escalatedSensorMeasureBelowThreshold() {
        CurrentState currentlyWarning = new CurrentState("uuid", ESCALATED, 0);

        StateEvent newEvent = commandHandler.handle(BELLOW_THRESHOLD, currentlyWarning);

        assertThat(newEvent).isInstanceOfSatisfying(SensorRecovered.class,
                (sensorRecovered -> assertThat(sensorRecovered.uuid()).isEqualTo("uuid")));
    }

    @Test
    void escalatedSensorMeasureAboveThreshold() {
        CurrentState currentlyWarning = new CurrentState("uuid", ESCALATED, 0);

        StateEvent newEvent = commandHandler.handle(ABOVE_THRESHOLD, currentlyWarning);

        assertThat(newEvent).isInstanceOfSatisfying(SensorBecameAlerting.class,
                (alerting -> assertThat(alerting.uuid()).isEqualTo("uuid")));
    }

    //
    @Test
    void alertSensorMeasureAboveThresholdWithCountDownFull() {
        CurrentState currentlyWarning = new CurrentState("uuid", ALERT, REQ_CONSECUTIVE_HEALTH_TO_RECOVER);

        StateEvent newEvent = commandHandler.handle(ABOVE_THRESHOLD, currentlyWarning);

        assertThat(newEvent).isNull(); // no change
    }

    @Test
    void alertSensorMeasureConsecutiveBellowThreshold() {
        CurrentState currentlyWarning = new CurrentState("uuid", ALERT, REQ_CONSECUTIVE_HEALTH_TO_RECOVER - 2);

        StateEvent newEvent = commandHandler.handle(BELLOW_THRESHOLD, currentlyWarning);

        assertThat(newEvent).isInstanceOfSatisfying(SensorRecovered.class,
                (recovered -> assertThat(recovered.uuid()).isEqualTo("uuid")));
    }

    @Test
    void alertSensorMeasureBellowThresholdWithCountDownInitiated() {
        CurrentState currentlyWarning = new CurrentState("uuid", ALERT, REQ_CONSECUTIVE_HEALTH_TO_RECOVER);

        StateEvent newEvent = commandHandler.handle(BELLOW_THRESHOLD, currentlyWarning);

        assertThat(newEvent).isInstanceOfSatisfying(SensorStartedRecovering.class,
                (recovering -> {
                    assertThat(recovering.uuid()).isEqualTo("uuid");
                    assertThat(recovering.countDownToHealth()).isEqualTo(REQ_CONSECUTIVE_HEALTH_TO_RECOVER - 1);
                }));
    }

    @Test
    void alertSensorMeasureAboveThresholdWithCountDownInitiated() {
        CurrentState currentlyWarning = new CurrentState("uuid", ALERT, REQ_CONSECUTIVE_HEALTH_TO_RECOVER - 2);

        StateEvent newEvent = commandHandler.handle(ABOVE_THRESHOLD, currentlyWarning);

        assertThat(newEvent).isInstanceOfSatisfying(SensorFailedRecovering.class,
                (failed -> assertThat(failed.uuid()).isEqualTo("uuid")));
    }


    // State
    StateEventHandler eventHandler = new StateEventHandler<>();

    @Test
    void unactivatedSensorHealthyEvent() {

        CurrentState newState = eventHandler.on(new SensorRecovered("uuid"), null);

        assertThat(newState)
                .isNotNull();

        assertThat(newState.uuid())
                .isEqualTo("uuid");

        assertThat(newState.status())
                .isEqualTo(OK);
    }

    @Test
    void unactivatedSensorWarningEvent() {

        CurrentState newState = eventHandler.on(new SensorBecameWarning("uuid"), null);

        assertThat(newState)
                .isNotNull();

        assertThat(newState.uuid())
                .isEqualTo("uuid");

        assertThat(newState.status())
                .isEqualTo(WARN);
    }

    @Test
    void healthySensorWarningEvent() {

        CurrentState newState = eventHandler.on(new SensorBecameWarning("uuid"), new CurrentState("uuid", OK, 0));

        assertThat(newState)
                .isNotNull();

        assertThat(newState.uuid())
                .isEqualTo("uuid");

        assertThat(newState.status())
                .isEqualTo(WARN);
    }

    @Test
    void warningSensorRecoveredEvent() {

        CurrentState newState = eventHandler.on(new SensorRecovered("uuid"), new CurrentState("uuid", WARN, 0));

        assertThat(newState)
                .isNotNull();

        assertThat(newState.uuid())
                .isEqualTo("uuid");

        assertThat(newState.status())
                .isEqualTo(OK);
    }

    @Test
    void warningSensorEscalatedEvent() {

        CurrentState newState = eventHandler.on(new SensorWarningEscalated("uuid"), new CurrentState("uuid", WARN, 0));

        assertThat(newState)
                .isNotNull();

        assertThat(newState.uuid())
                .isEqualTo("uuid");

        assertThat(newState.status())
                .isEqualTo(ESCALATED);
    }


    @Test
    void escalatedSensorRecoveredEvent() {

        CurrentState newState = eventHandler.on(new SensorRecovered("uuid"), new CurrentState("uuid", ESCALATED, 0));

        assertThat(newState)
                .isNotNull();

        assertThat(newState.uuid())
                .isEqualTo("uuid");

        assertThat(newState.status())
                .isEqualTo(OK);
    }

    @Test
    void escalatedSensorAlertEvent() {

        CurrentState newState = eventHandler.on(new SensorBecameAlerting("uuid"), new CurrentState("uuid", ESCALATED, 0));

        assertThat(newState)
                .isNotNull();

        assertThat(newState.uuid())
                .isEqualTo("uuid");

        assertThat(newState.status())
                .isEqualTo(ALERT);

        assertThat(newState.neededMeasurementsToRecover())
                .isEqualTo(REQ_CONSECUTIVE_HEALTH_TO_RECOVER);
    }

    @Test
    void alertSensorStartedRecoveredEvent() {

        CurrentState newState = eventHandler.on(new SensorStartedRecovering("uuid", REQ_CONSECUTIVE_HEALTH_TO_RECOVER - 1),
                new CurrentState("uuid", ALERT, REQ_CONSECUTIVE_HEALTH_TO_RECOVER));

        assertThat(newState)
                .isNotNull();

        assertThat(newState.uuid())
                .isEqualTo("uuid");

        assertThat(newState.status())
                .isEqualTo(ALERT);

        assertThat(newState.neededMeasurementsToRecover())
                .isEqualTo(REQ_CONSECUTIVE_HEALTH_TO_RECOVER - 1);
    }

    @Test
    void alertSensorFailedToRecoveredEvent() {

        CurrentState newState = eventHandler.on(new SensorFailedRecovering("uuid"),
                new CurrentState("uuid", ALERT, REQ_CONSECUTIVE_HEALTH_TO_RECOVER - 1));

        assertThat(newState)
                .isNotNull();

        assertThat(newState.uuid())
                .isEqualTo("uuid");

        assertThat(newState.status())
                .isEqualTo(ALERT);

        assertThat(newState.neededMeasurementsToRecover())
                .isEqualTo(REQ_CONSECUTIVE_HEALTH_TO_RECOVER);
    }

    @Test
    void alertSensorRecoveredEvent() {

        CurrentState newState = eventHandler.on(new SensorRecovered("uuid"),
                new CurrentState("uuid", ALERT, REQ_CONSECUTIVE_HEALTH_TO_RECOVER - 2));

        assertThat(newState)
                .isNotNull();

        assertThat(newState.uuid())
                .isEqualTo("uuid");

        assertThat(newState.status())
                .isEqualTo(OK);

        assertThat(newState.neededMeasurementsToRecover())
                .isEqualTo(0);
    }
}