package com.example.modbusapplication.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class DeviceOutDetailsDTO {

    // private Long id;
    private short deviceId;
    private String modelNumber;
    private String serialNumber;

    private String customerName;
    private String clientLocation;
    private String contactNumber;

    private LocalDate installedDate;
    private LocalDate warrantyFrom;
    private LocalDate warrantyTo;

    private String status;
    private String remarks;

    private String createdBy;
    private LocalDateTime createdOn;


}

