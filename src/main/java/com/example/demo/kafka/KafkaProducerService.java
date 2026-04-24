package com.example.demo.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * KafkaProducerService — Servizio per l'invio di messaggi a Kafka.
 * Viene utilizzato per notificare eventi di business al topic configurato.
 */
@Service
public class KafkaProducerService {

    @Value("${KAFKA_TOPIC}")
    private String topic;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Invia un messaggio al topic Kafka configurato.
     *
     * @param message il messaggio da inviare
     */
    public void sendMessage(String message) {
        kafkaTemplate.send(topic, message)
            .addCallback(
                result  -> System.out.println("Messaggio inviato al topic '" + topic + "': " + message),
                failure -> System.err.println("Errore invio messaggio: " + failure.getMessage())
            );
    }
}