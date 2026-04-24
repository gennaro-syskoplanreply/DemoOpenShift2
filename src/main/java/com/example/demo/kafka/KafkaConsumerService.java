package com.example.demo.kafka; 

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * KafkaConsumerService — Servizio per la ricezione di messaggi da Kafka.
 * Ascolta il topic configurato e processa i messaggi in arrivo.
 */
@Service
public class KafkaConsumerService {

    /**
     * Ascolta i messaggi dal topic Kafka e li stampa nel log.
     *
     * @param message il messaggio ricevuto dal topic
     */
    @KafkaListener(
        topics = "${KAFKA_TOPIC}",
        groupId = "${KAFKA_GROUP_ID}"
    )
    public void listen(String message) {
        System.out.println("Messaggio ricevuto da Kafka: " + message);
        // Aggiungi qui la logica di business da eseguire alla ricezione
    }
}