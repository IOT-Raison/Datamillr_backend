package com.example.modbusapplication.Model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DeviceNamePackingDAO {
    private String deviceName;
    private Short deviceIds;
    private String machineName;
   private LocalDateTime timestamp;
    private String batchName;
    private int bagWeightSet;
    private int bagCount;
    //private double weightOfBag;
    private double accumulatedWeight;
    private String machineStatus;
}