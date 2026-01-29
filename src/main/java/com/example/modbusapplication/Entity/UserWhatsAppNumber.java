package com.example.modbusapplication.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_whatsapp_numbers",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "whatsapp_number"}))
public class UserWhatsAppNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_Key", nullable = false)
    private Short userKey;

    @Column(name = "whatsapp_number", nullable = false, length = 20)
    private String whatsappNumber;

    @Column(name = "whatsapp_enabled", nullable = false)
    private boolean whatsappEnabled = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Short getUserKey() { return userKey; }
    public void setUserKey(Short userKey) { this.userKey = userKey; }

    public String getWhatsappNumber() { return whatsappNumber; }
    public void setWhatsappNumber(String whatsappNumber) { this.whatsappNumber = whatsappNumber; }

    public boolean isWhatsappEnabled() { return whatsappEnabled; }
    public void setWhatsappEnabled(boolean whatsappEnabled) { this.whatsappEnabled = whatsappEnabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

