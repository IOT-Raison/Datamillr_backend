package com.example.modbusapplication.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "whatsapp_device_mapping",
       uniqueConstraints = @UniqueConstraint(columnNames = {"whatsapp_number_id", "device_id"}))
public class WhatsAppDeviceMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "whatsapp_number_id", nullable = false)
    private Long whatsappNumberId;

    @Column(name = "device_id", nullable = false)
    private Short deviceId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getWhatsappNumberId() { return whatsappNumberId; }
    public void setWhatsappNumberId(Long whatsappNumberId) { this.whatsappNumberId = whatsappNumberId; }

    public Short getDeviceId() { return deviceId; }
    public void setDeviceId(Short deviceId) { this.deviceId = deviceId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}

