package com.example.demo.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.demo.dto.LogRequest;
import com.example.demo.model.Log;
import com.example.demo.service.LogService;

import java.sql.Date;

/**
 * KafkaConsumerService — Servizio per la ricezione di messaggi da Kafka.
 *
 * Gestisce due listener:
 * - listen(): ascolta il topic principale e processa i messaggi
 * - listenDlt(): ascolta il topic DLQ per i messaggi falliti
 *
 * In caso di errore nel metodo listen():
 * 1. Spring Kafka riprova automaticamente (3 volte, ogni 2 secondi)
 * 2. Se tutti i retry falliscono, il messaggio viene inviato al DLT
 * 3. listenDlt() riceve il messaggio dal DLT e lo gestisce
 */
@Service
public class KafkaConsumerService {

    private final LogService logService;

    public KafkaConsumerService(LogService logService) {
        this.logService = logService;
    }
    
    /**
     * Ascolta i messaggi dal topic principale.
     * Se lancia un'eccezione, Spring Kafka attiva il meccanismo di retry.
     *
     * @param message il messaggio ricevuto dal topic
     */
    @KafkaListener(
        topics = "${KAFKA_TOPIC}",
        groupId = "${KAFKA_GROUP_ID}"
    )
    public void listen(String message) {
        System.out.println("Messaggio ricevuto: " + message);

        // Simulazione di un errore per testare retry e DLQ
        // Rimuovi questo blocco in produzione
        if (message.contains("ERRORE")) {
            throw new RuntimeException("Errore simulato per testare il retry!");
        }

        // Logica di business normale
        LogRequest request = new LogRequest();
        request.setTimeStamp(new Date(System.currentTimeMillis()));   
        request.setMessage(message);
        Log created = logService.create(request);
    }

    /**
     * Ascolta i messaggi dal Dead Letter Topic.
     * Viene chiamato quando un messaggio ha fallito tutti i retry.
     * Qui puoi loggare, salvare nel DB, inviare alert, ecc.
     *
     * @param message il messaggio fallito
     */
    @KafkaListener(
        topics = "${KAFKA_TOPIC}.DLT",
        groupId = "${KAFKA_GROUP_ID}-dlt"
    )
    public void listenDlt(String message) {
        System.err.println("⚠️ Messaggio finito in DLQ: " + message);

        LogRequest request = new LogRequest();
        request.setTimeStamp(new Date(System.currentTimeMillis()));   
        request.setMessage("Message in DLQ: " + message);
        Log created = logService.create(request);
    }
}