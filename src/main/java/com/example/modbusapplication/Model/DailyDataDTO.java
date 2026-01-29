package com.example.modbusapplication.Model;


import java.time.LocalDate;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Getter
@Setter
@Component
public class DailyDataDTO {
    private int deviceId;
    private int dailyTotalweight;
    private LocalDate recordDate;

}
