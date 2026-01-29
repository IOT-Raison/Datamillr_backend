package com.example.modbusapplication.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity
@Table(name = "device_out_details")
public class DeviceOutDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private short deviceId;
    private String modelNumber;
    private String serialNumber;
    private String customerName;
    private String clientLocation;
    private String contactNumber;
    private LocalDate installedDate;
    private LocalDate warrantyFrom;
    private LocalDate warrantyTo;
    private String status;   // IN_STOCK, INSTALLED, REPAIR, RETURNED
    private String remarks;
    private String createdBy;

    @CreationTimestamp
    private LocalDateTime createdOn;

}
