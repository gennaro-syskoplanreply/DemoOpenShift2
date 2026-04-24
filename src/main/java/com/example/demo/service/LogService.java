package com.example.demo.service;

import com.example.demo.dto.LogRequest;
import com.example.demo.model.Log;
import com.example.demo.repository.LogRepository;
import com.example.demo.repository.LogRepository;

import com.example.demo.kafka.KafkaProducerService;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LogService {
    private final LogRepository logRepository;
    private final KafkaProducerService kafkaProducerService;

    public LogService(LogRepository logRepository, KafkaProducerService kafkaProducerService) {
        this.logRepository = logRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    public Log create(LogRequest request) {
        Log log = new Log();
        log.setTimeStamp(request.getTimeStamp());
        log.setMessage(request.getMessage());

        Log savedLog = logRepository.save(log);

        return savedLog;
    }

    public List<Log> getAll() {
        return logRepository.findAll();
    }

    public Log getById(UUID id) {
        return logRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("log not found with id: " + id));
    }

    public Log update(UUID id, LogRequest request) {
        Log log = getById(id);
        log.setTimeStamp(request.getTimeStamp());
        log.setMessage(request.getMessage());
        return logRepository.save(log);
    }

    public void delete(UUID id) {
        logRepository.delete(getById(id));
    }
}
