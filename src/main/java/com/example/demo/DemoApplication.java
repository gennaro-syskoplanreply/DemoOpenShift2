package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@SpringBootApplication
public class DemoApplication {

    @RequestMapping("/")
    String home() {
        return "Hello World da Gennaro!";
    }

    @GetMapping("/test")
    String test() {
        LocalDate oggi = LocalDate.now();
        String dataFormattata = oggi.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return "Data di oggi: " + dataFormattata;
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}