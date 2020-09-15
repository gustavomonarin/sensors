package com.acme.sensors.infrastructure;

import com.acme.sensors.domain.Definitions;
import com.acme.sensors.domain.Definitions.StatefulCommandHandler;
import com.acme.sensors.domain.Definitions.StatefulEventHandler;
import com.acme.sensors.domain.SensorMeasurement;
import com.acme.sensors.domain.SensorState;
import com.acme.sensors.infrastructure.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.Optional;

@EnableKafkaStreams
@Configuration(proxyBeanMethods = false)
public class SensorStateTopologyProvider {

    @Bean
    public KStream<String, SensorState.StateEvent> sensorStatusTopology(
            KafkaConfig config,
            StreamsBuilder builder,
            ObjectMapper objectMapper) {

        JsonSerde<SensorMeasurement.MeasurementCollected> measurementSerde = new JsonSerde<>(SensorMeasurement.MeasurementCollected.class, objectMapper);
        JsonSerde<SensorState.CurrentState> stateSerde = new JsonSerde<>(SensorState.CurrentState.class, objectMapper);

        StoreBuilder<KeyValueStore<String, SensorState.CurrentState>> stateStoreBuilder = Stores
                .keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(
                                config.getSensorStateStateStoreName()),
                        Serdes.String(),
                        stateSerde
                );

        KStream<String, SensorState.StateEvent> sensorStateStream = builder
                .addStateStore(stateStoreBuilder)
                .stream(
                        config.getSensorMeasurementsTopic(),
                        Consumed.with(Serdes.String(), measurementSerde)
                )
                .mapValues(event -> new SensorState.UpdateCurrentState(event.uuid(), event.co2()))
                .transform(() -> new StatefulAggregateProcessor<>(
                                config.getSensorStateStateStoreName(),
                                new SensorState.UpdateCurrentStateCommandHandler<>(),
                                new SensorState.StateEventHandler<>()),
                        config.getSensorStateStateStoreName()
                );

        sensorStateStream.to(config.getSensorStateEventsTopic(),
                Produced.with(
                        Serdes.String(),
                        new JsonSerde<>(
                                SensorState.StateEvent.class,
                                objectMapper)));

        return sensorStateStream;
    }

    static class StatefulAggregateProcessor<K,
            C extends Definitions.Command,
            S extends Definitions.State,
            E extends Definitions.Event>
            implements Transformer<K, C, KeyValue<K, E>> {

        private final String stateStoreName;
        private final StatefulCommandHandler<C, S, E> commandHandler;
        private final StatefulEventHandler<E, S> eventHandler;

        private KeyValueStore<K, S> stateStore;

        StatefulAggregateProcessor(String stateStoreName,
                                   StatefulCommandHandler<C, S, E> commandHandler,
                                   StatefulEventHandler<E, S> eventHandler) {
            this.stateStoreName = stateStoreName;
            this.commandHandler = commandHandler;
            this.eventHandler = eventHandler;
        }

        @Override
        public void init(ProcessorContext context) {

            this.stateStore = (KeyValueStore<K, S>) context.getStateStore(stateStoreName);

        }

        @Override
        public KeyValue<K, E> transform(K key, C command) {

            S currentState = stateStore.get(key);

            Optional<E> newEvent = commandHandler.handle(command, currentState);

            if (newEvent.isPresent()) {
                S newState = eventHandler.on(newEvent.get(), currentState);

                this.stateStore.put(key, newState);

                return new KeyValue<>(key, newEvent.get());
            }

            return null; //no new event to propagate side effects
        }

        @Override
        public void close() {

        }

    }


}
