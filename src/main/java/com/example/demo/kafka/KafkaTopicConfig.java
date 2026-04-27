package com.example.demo.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * KafkaTopicConfig — Configurazione del topic Kafka.
 * Crea automaticamente il topic all'avvio se non esiste già.
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${KAFKA_TOPIC}")
    private String topicName;

    @Bean
    public NewTopic demoTopic() {
        return TopicBuilder.name(topicName)
                .partitions(3)
                .replicas(1)
                .build();
    }
}