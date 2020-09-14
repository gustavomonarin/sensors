package com.acme.sensors.domain;

import org.springframework.lang.Nullable;

public class Definitions {

    public interface Command {
    }

    public interface Event {
    }

    public interface State {
    }

    /**
     * @param <C> The command to be handled
     * @param <S> The current status in which the command will be applied
     * @param <E> The resulting event
     */
    public interface StatefulCommandHandler<C extends Command, S extends State, E extends Event> {

        E handle(C command, @Nullable S currentState);

    }

    public interface StatefulEventHandler<E extends Event, S extends State> {

        S on(E event, @Nullable S currentState);

    }


}
