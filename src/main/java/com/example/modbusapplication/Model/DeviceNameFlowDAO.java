package com.example.modbusapplication.Model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class DeviceNameFlowDAO {
    private String deviceName;
    private Short deviceIds;
    private String machineName;
    private LocalDateTime time;
    private Double flowRate;
    private Double totalWeight;
    private Double setWeight;
    private String batchName;
    
}
