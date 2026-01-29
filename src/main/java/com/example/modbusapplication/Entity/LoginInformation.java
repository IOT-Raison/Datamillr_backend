package com.example.modbusapplication.Entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
@Entity
public class LoginInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true) 
    private String ipAddress;
    private String randomNumber;
    private LocalDateTime hitTime;
    private int userKey;
    private boolean status;


     public int getUserKey() {
        return userKey;
    }
    public void setUserKey(int userKey) {
        this.userKey = userKey;
    }
    public boolean isStatus() {
        return status;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public String getRandomNumber() {
        return randomNumber;
    }
    public LocalDateTime getHitTime() {
        return hitTime;
    }
    // public void setId(Long id) {
    //     this.id = id;
    // }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public void setRandomNumber(String randomNumber) {
        this.randomNumber = randomNumber;
    }
    public void setHitTime(LocalDateTime hitTime) {
        this.hitTime = hitTime;
    }
    
}
