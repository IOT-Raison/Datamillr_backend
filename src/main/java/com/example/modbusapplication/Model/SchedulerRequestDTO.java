package com.example.modbusapplication.Model;

import lombok.Data;

@Data
public class SchedulerRequestDTO {
    private int userKey;
    private String time;
    private String removetime; // format HH:mm
}