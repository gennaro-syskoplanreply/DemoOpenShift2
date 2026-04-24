package com.example.demo.kafka; 

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.demo.dto.LogRequest;
import com.example.demo.model.Log;
import com.example.demo.service.LogService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.Date; 

/**
 * KafkaConsumerService — Servizio per la ricezione di messaggi da Kafka.
 * Ascolta il topic configurato e processa i messaggi in arrivo.
 */
@Service
public class KafkaConsumerService {

    private final LogService logService;

    public KafkaConsumerService(LogService logService) {
        this.logService = logService;
    }
    /**
     * Ascolta i messaggi dal topic Kafka e li stampa nel log.
     *
     * @param message il messaggio ricevuto dal topic
     */
    @KafkaListener(
        topics = "${KAFKA_TOPIC}",
        groupId = "${KAFKA_GROUP_ID_DEMOAPP2}"
    )
    public void listen(String message) {
        System.out.println("Messaggio ricevuto da Kafka: " + message);
        LogRequest request = new LogRequest();
        request.setTimeStamp(new Date(System.currentTimeMillis()));   
        request.setMessage(message);
        Log created = logService.create(request);
    }
}