package com.example.modbusapplication.Model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModbusDataRequestDTO {
    private Short deviceId;
    private String startDate; 
    private String endDate;
    private String companyName;
    private Integer month;
    private Integer year;
    private Boolean allYearHistory;  
    private String batchname;
    private String deviceName; 
}
