package com.acme.sensors.domain;

import com.acme.sensors.domain.SensorState.StateEvent.*;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class SensorState {

    public static record CurrentState(String uuid, Status status, Integer neededMeasurementsToRecover)
            implements Definitions.State {

        public enum Status {
            OK, WARN, ESCALATED, ALERT;
        }

        public static final int REQ_CONSECUTIVE_HEALTH_TO_RECOVER = 3;

        Integer countDownToRecover() {
            return neededMeasurementsToRecover - 1;
        }

        boolean hasStartedToRecover(){
            return status == Status.ALERT && neededMeasurementsToRecover != REQ_CONSECUTIVE_HEALTH_TO_RECOVER;
        }

        boolean isReadyToRecover(){
            return neededMeasurementsToRecover == 1;
        }

    }

    public static record UpdateCurrentState(
            String uuid,
            Integer co2)
            implements Definitions.Command {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    //todo move away from here
    public interface StateEvent extends Definitions.Event {

        record SensorRecovered(String uuid) implements StateEvent {
        }

        record SensorBecameWarning(String uuid) implements StateEvent {
        }

        record SensorWarningEscalated(String uuid) implements StateEvent {
        }

        record SensorBecameAlerting(String uuid) implements StateEvent {
        }

        record SensorStartedRecovering(String uuid, int countDownToHealth) implements StateEvent {
        }

        record SensorFailedRecovering(String uuid) implements StateEvent {
        }

    }

    public static class UpdateCurrentStateCommandHandler<E extends StateEvent>
            implements Definitions.StatefulCommandHandler<UpdateCurrentState, CurrentState, E> {

        @Override
        public E handle(UpdateCurrentState updateCurrentState, CurrentState currentState) {

            // replace this messy logic by some sort of clever enum, but still stateless, maybe emulating trait/object from scala

            StateEvent sideEffectEvent = null;

            // non activated sensor
            if (currentState == null) {
                sideEffectEvent = isBelowThreshold(updateCurrentState.co2)
                        ? new SensorRecovered(updateCurrentState.uuid)
                        : new SensorBecameWarning(updateCurrentState.uuid);

            } else if (CurrentState.Status.OK == currentState.status()) {
                if (isAboveThreshold(updateCurrentState.co2))
                    sideEffectEvent = new SensorBecameWarning(updateCurrentState.uuid);

            } else if (CurrentState.Status.WARN == currentState.status()) {
                sideEffectEvent = isBelowThreshold(updateCurrentState.co2)
                        ? new SensorRecovered(updateCurrentState.uuid)
                        : new SensorWarningEscalated(updateCurrentState.uuid);

            } else if (CurrentState.Status.ESCALATED == currentState.status()) {
                sideEffectEvent = isBelowThreshold(updateCurrentState.co2)
                        ? new SensorRecovered(updateCurrentState.uuid)
                        : new SensorBecameAlerting(updateCurrentState.uuid);

            } else if (CurrentState.Status.ALERT == currentState.status()) {
                if (isBelowThreshold(updateCurrentState.co2)) {
                    if (currentState.isReadyToRecover()) {
                        sideEffectEvent = new SensorRecovered(updateCurrentState.uuid);
                    } else {
                        sideEffectEvent = new SensorStartedRecovering(updateCurrentState.uuid, currentState.countDownToRecover());
                    }
                } else if (currentState.hasStartedToRecover()) {
                    sideEffectEvent = new SensorFailedRecovering(currentState.uuid);
                }
            }

            return (E) sideEffectEvent;
        }


        private boolean isBelowThreshold(int measurement) {
            return !isAboveThreshold(measurement);
        }

        private boolean isAboveThreshold(int measurement) {
            return measurement > 2_000;
        }

    }

    public static class StateEventHandler<E extends StateEvent>
            implements Definitions.StatefulEventHandler<E, CurrentState> {

        @Override
        public CurrentState on(E e, CurrentState currentState) {

            //Using JEP 305: Pattern Matching for instanceof .
            // Once there is a finally a decent switch expression in java, with the jep-305, this code should look better
            if (e instanceof SensorRecovered recovered) {
                return new CurrentState(recovered.uuid, CurrentState.Status.OK, 0);
            } else if (e instanceof SensorBecameWarning warning) {
                return new CurrentState(warning.uuid, CurrentState.Status.WARN, 0);
            } else if (e instanceof SensorWarningEscalated escalated){
                return new CurrentState(currentState.uuid, CurrentState.Status.ESCALATED, 0);
            }  else if ( e instanceof SensorBecameAlerting alerting) {
                return new CurrentState(currentState.uuid, CurrentState.Status.ALERT, CurrentState.REQ_CONSECUTIVE_HEALTH_TO_RECOVER);
            } else if ( e instanceof SensorFailedRecovering failed) {
                return new CurrentState(currentState.uuid, CurrentState.Status.ALERT, CurrentState.REQ_CONSECUTIVE_HEALTH_TO_RECOVER);
            } else if ( e instanceof SensorStartedRecovering recovering) {
                return new CurrentState(currentState.uuid, CurrentState.Status.ALERT, recovering.countDownToHealth);
            }


            return null;
        }
    }
}
