package com.example.modbusapplication.Model;

import java.time.LocalDateTime;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ModbusEntityDao {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private Long id;
    private LocalDateTime timestamp;
    private int status;
    private String batchName;
    private int flowrate;
    private int setWeight;
    private int totalWeight;
    private Short deviceId;
    private boolean success;
    private String machinetype = "Flow Control";
    private boolean isEndOfBatch;



    public ModbusEntityDao() {
    }

   
    public ModbusEntityDao(LocalDateTime timestamp, int status, int flowrate, String batchName, int setWeight, int totalWeight, Short deviceId) {
        this.timestamp = timestamp;
        this.status = status;
        this.flowrate = flowrate;
        this.batchName = batchName;
        this.setWeight = setWeight;
        this.totalWeight = totalWeight;
        this.deviceId = deviceId;
     
    }


}

  