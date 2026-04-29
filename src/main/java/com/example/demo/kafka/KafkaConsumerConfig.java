package com.example.demo.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * KafkaConsumerConfig — Configurazione del consumer Kafka con retry e DLQ.
 *
 * Implementa:
 * - Retry automatico in caso di errore (3 tentativi con 2 secondi di attesa)
 * - Dead Letter Queue (DLQ) per i messaggi che falliscono tutti i retry
 *
 * Flusso:
 *   Messaggio → Consumer → Errore → Retry 1 → Retry 2 → Retry 3 → DLQ
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${KAFKA_GROUP_ID_DEMOAPP2}")
    private String groupId;

    @Value("${KAFKA_TOPIC}")
    private String topic;

    /**
     * Configura il ProducerFactory per la DLQ.
     * Serve per inviare i messaggi falliti al topic DLQ.
     */
    @Bean
    public ProducerFactory<String, String> dlqProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * KafkaTemplate usato dalla DLQ per pubblicare i messaggi falliti.
     */
    @Bean
    public KafkaTemplate<String, String> dlqKafkaTemplate() {
        return new KafkaTemplate<>(dlqProducerFactory());
    }

    /**
     * Configura il ConsumerFactory con le proprietà di base.
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * Configura il container factory con:
     * - Retry: 3 tentativi con 2 secondi di attesa tra un tentativo e l'altro
     * - DLQ: i messaggi che falliscono tutti i retry vengono inviati al topic "demo-topic.DLT"
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {

        // Configura la DLQ — i messaggi falliti vanno in "demo-topic.DLT"
        DeadLetterPublishingRecoverer recoverer =
            new DeadLetterPublishingRecoverer(dlqKafkaTemplate());

        // Configura il retry:
        // - FixedBackOff(2000, 3) = aspetta 2 secondi, riprova max 3 volte
        DefaultErrorHandler errorHandler =
            new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 3L));

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}