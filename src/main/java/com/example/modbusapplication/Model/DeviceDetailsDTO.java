package com.example.modbusapplication.Model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceDetailsDTO {
    private Short deviceId;
    private String deviceName;
    private String deviceType;

        public DeviceDetailsDTO(Short deviceId, String deviceName, String deviceType) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
    }

    public DeviceDetailsDTO() {} 
}

