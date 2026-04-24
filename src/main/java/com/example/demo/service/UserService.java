package com.example.demo.service;

import com.example.demo.dto.UserRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import com.example.demo.kafka.KafkaProducerService;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;

    public UserService(UserRepository userRepository, KafkaProducerService kafkaProducerService) {
        this.userRepository = userRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    public User create(UserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        // Invia messaggio Kafka dopo il salvataggio
        kafkaProducerService.sendMessage(
            "Ok va tutto bene - Utente creato: " + savedUser.getName() + " " + savedUser.getSurname()
        );

        return savedUser;
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User update(UUID id, UserRequest request) {
        User user = getById(id);
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setRole(request.getRole());
        return userRepository.save(user);
    }

    public void delete(UUID id) {
        userRepository.delete(getById(id));
    }
}
