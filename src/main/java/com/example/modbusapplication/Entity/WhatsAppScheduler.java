package com.example.modbusapplication.Entity;

import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppScheduler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int userKey;

    private LocalTime time1;
    private LocalTime time2;
    private LocalTime time3;
    private LocalTime time4;
    public WhatsAppScheduler(int userKey) {
        this.userKey = userKey;
    }
}
