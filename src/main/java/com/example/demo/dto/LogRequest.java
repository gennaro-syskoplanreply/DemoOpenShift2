package com.example.demo.dto;

import java.sql.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogRequest {

    private Date timeStamp;
    private String message;
    
}
