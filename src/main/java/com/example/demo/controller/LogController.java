package com.example.demo.controller;

import com.example.demo.dto.LogRequest;
import com.example.demo.model.Log;
import com.example.demo.service.LogService;
import com.example.demo.service.LogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping
    public ResponseEntity<Log> create(@RequestBody LogRequest request) {
        Log created = logService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Log>> getAll() {
        return ResponseEntity.ok(logService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Log> update(@PathVariable UUID id, @RequestBody LogRequest request) {
        return ResponseEntity.ok(logService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        logService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
