package com.acme.sensors.domain;

import com.acme.sensors.domain.SensorState.StateEvent.*;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.BiFunction;

public class SensorState {

    public interface SensorStateRepository {
        Mono<CurrentState> currentStateFor(String anUuid);
    }

    public static record CurrentState(String uuid, Status status, Integer neededMeasurementsToRecover)
            implements Definitions.State {

        public static final int REQ_CONSECUTIVE_HEALTH_TO_RECOVER = 3;

        Integer countDownToRecover() {
            return neededMeasurementsToRecover - 1;
        }

        boolean hasStartedToRecover() {
            return status == Status.ALERT && neededMeasurementsToRecover != REQ_CONSECUTIVE_HEALTH_TO_RECOVER;
        }

        boolean isReadyToRecover() {
            return neededMeasurementsToRecover == 1;
        }

    }

    public enum Status {
        OK(
                (final UpdateCurrentState command, final CurrentState currentState) ->
                        switch (command.measurementType()) {
                            case HIGH -> Optional.of(new SensorBecameWarning(command.uuid()));
                            case LOW -> Optional.empty();
                        }),

        WARN(
                (UpdateCurrentState command, CurrentState currentState) ->
                        switch (command.measurementType()) {
                            case HIGH -> Optional.of(new SensorWarningEscalated(command.uuid()));
                            case LOW -> Optional.of(new SensorRecovered(command.uuid()));
                        }),

        ESCALATED(
                (UpdateCurrentState command, CurrentState currentState) ->
                        switch (command.measurementType()) {
                            case HIGH -> Optional.of(new SensorBecameAlerting(command.uuid()));
                            case LOW -> Optional.of(new SensorRecovered(command.uuid()));
                        }),

        ALERT(
                (UpdateCurrentState command, CurrentState currentState) ->
                        switch (command.measurementType()) {
                            case HIGH -> currentState.hasStartedToRecover()
                                    ? Optional.of(new SensorFailedRecovering(currentState.uuid))
                                    : Optional.empty();
                            case LOW -> currentState.isReadyToRecover()
                                    ? Optional.of(new SensorRecovered(command.uuid()))
                                    : Optional.of(new SensorStartedRecovering(command.uuid(), currentState.countDownToRecover()));
                        }),

        UNKNOWN(
                (final UpdateCurrentState command, final CurrentState currentState) ->
                        switch (command.measurementType()) {
                            case HIGH -> Optional.of(new SensorBecameWarning(command.uuid()));
                            case LOW -> Optional.of(new SensorRecovered(command.uuid));
                        });

        private BiFunction<UpdateCurrentState, CurrentState, Optional<StateEvent>> transitionFunction;

        Status(BiFunction<UpdateCurrentState, CurrentState, Optional<StateEvent>> function) {
            this.transitionFunction = function;
        }

        Optional<StateEvent> apply(UpdateCurrentState command, CurrentState currentState) {
            return transitionFunction.apply(command, currentState);
        }
    }

    public static record UpdateCurrentState(
            String uuid,
            Integer co2)
            implements Definitions.Command {

        enum MeasurementType {
            HIGH, LOW
        }

        MeasurementType measurementType() {
            return co2 > 2_000 ? MeasurementType.HIGH : MeasurementType.LOW;
        }
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
        public Optional<E> handle(UpdateCurrentState command, CurrentState currentState) {

            // non activated sensor
            if (currentState == null) {
                return (Optional<E>) Status.UNKNOWN.apply(command, currentState);
            } else {
                return (Optional<E>) currentState.status.apply(command, currentState);
            }
        }
    }

    public static class StateEventHandler<E extends StateEvent>
            implements Definitions.StatefulEventHandler<E, CurrentState> {

        @Override
        public CurrentState on(E e, CurrentState currentState) {

            //Using JEP 305: Pattern Matching for instanceof .
            // Once there is a finally a decent switch expression in java, with the jep-305, this code should look better
            if (e instanceof SensorRecovered recovered) {
                return new CurrentState(recovered.uuid, Status.OK, 0);
            } else if (e instanceof SensorBecameWarning warning) {
                return new CurrentState(warning.uuid, Status.WARN, 0);
            } else if (e instanceof SensorWarningEscalated escalated) {
                return new CurrentState(currentState.uuid, Status.ESCALATED, 0);
            } else if (e instanceof SensorBecameAlerting alerting) {
                return new CurrentState(currentState.uuid, Status.ALERT, CurrentState.REQ_CONSECUTIVE_HEALTH_TO_RECOVER);
            } else if (e instanceof SensorFailedRecovering failed) {
                return new CurrentState(currentState.uuid, Status.ALERT, CurrentState.REQ_CONSECUTIVE_HEALTH_TO_RECOVER);
            } else if (e instanceof SensorStartedRecovering recovering) {
                return new CurrentState(currentState.uuid, Status.ALERT, recovering.countDownToHealth);
            }

            return null;
        }
    }

}
