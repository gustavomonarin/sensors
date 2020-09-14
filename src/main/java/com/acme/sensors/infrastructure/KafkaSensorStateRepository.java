package com.acme.sensors.infrastructure;

import com.acme.sensors.domain.SensorApplicationService;
import com.acme.sensors.domain.SensorState;
import com.acme.sensors.infrastructure.config.KafkaConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class KafkaSensorStateRepository implements SensorApplicationService.SensorStateRepository {

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    private final KafkaConfig config;
    private final RetryTemplate retryTemplate;

    public KafkaSensorStateRepository(
            StreamsBuilderFactoryBean streamsBuilderFactoryBean,
            KafkaConfig config) {

        this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
        this.config = config;

        this.retryTemplate = new RetryTemplate();
        this.retryTemplate.setBackOffPolicy(new FixedBackOffPolicy());
        this.retryTemplate.setRetryPolicy(new SimpleRetryPolicy()); // default of 3 attempts
    }

    @Override
    public Mono<SensorState.CurrentState> currentStateFor(String anUuid) {

        KafkaStreams streams = streamsBuilderFactoryBean.getKafkaStreams();

        ReadOnlyKeyValueStore<String, SensorState.CurrentState> store = retryTemplate.execute(context -> streams.store(
                StoreQueryParameters.fromNameAndType(
                        config.getSensorStateStateStoreName(),
                        QueryableStoreTypes.keyValueStore())));


        SensorState.CurrentState currentState = store.get(anUuid);
        return Mono.justOrEmpty(currentState);
    }
}
