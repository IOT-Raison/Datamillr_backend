package com.example.modbusapplication.Model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegDeviceDAO {
    private Short deviceId;
    private String deviceName;
    private String companyName;
    private String userId;
    private String password;
    private short userKey;
    private boolean isNewUser;
    
}
