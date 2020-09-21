package com.acme.sensors.infrastructure;

import com.acme.sensors.domain.SensorMetrics.SensorMetric;
import com.acme.sensors.infrastructure.config.KafkaConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;

import static com.acme.sensors.domain.SensorMetrics.SensorMetricRepository;

@Component
public class KafkaSensorMetricRepository implements SensorMetricRepository {

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    private final KafkaConfig config;
    private final RetryTemplate retryTemplate;

    public KafkaSensorMetricRepository(final StreamsBuilderFactoryBean streamsBuilderFactoryBean,
                                       final KafkaConfig config) {

        this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
        this.config = config;

        this.retryTemplate = new RetryTemplate();
        this.retryTemplate.setBackOffPolicy(new FixedBackOffPolicy());
        this.retryTemplate.setRetryPolicy(new SimpleRetryPolicy()); // default of 3 attempts
    }

    @Override
    public Mono<SensorMetric> forTheLast30Days(final String anUuid) { // the from and until could be passed as parameter
        KafkaStreams streams = streamsBuilderFactoryBean.getKafkaStreams();

        ReadOnlyWindowStore<String, SensorMetric> store = retryTemplate.execute(context -> streams.store(
                StoreQueryParameters.fromNameAndType(
                        config.getSensorMetricsStateStoreName(),
                        QueryableStoreTypes.windowStore())));

        Instant now = Instant.now();
        Instant from = now.minus(Duration.ofDays(30));

        try (WindowStoreIterator<SensorMetric> metrics = store.fetch(anUuid, from, now)) {

            var aggregatedMetrics =
                    SensorMetric.fromRange(
                            new WindowStoreKeyValueIteratorToValueIteratorAdapter<>(metrics));

            return Mono.justOrEmpty(aggregatedMetrics);
        }

    }


    /***
     * Simple adapter to avoid kafka KeyValue iterator to leaky inside the domain code.
     * @param <V>
     */
    private static class WindowStoreKeyValueIteratorToValueIteratorAdapter<V> implements Iterator<V> {

        private WindowStoreIterator<V> metrics;

        public WindowStoreKeyValueIteratorToValueIteratorAdapter(final WindowStoreIterator<V> metrics) {
            this.metrics = metrics;
        }

        @Override
        public boolean hasNext() {
            return metrics.hasNext();
        }

        @Override
        public V next() {
            return metrics.next().value;
        }
    }
}
