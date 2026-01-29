package com.example.modbusapplication.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "device_mapping")
@Getter
@Setter
public class DeviceMapping {

    public DeviceMapping(short deviceId) {
        this.deviceId =deviceId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private short userKey;

    private short deviceId;

    private String deviceName;

    private String deviceType;

    public DeviceMapping(short deviceId, String deviceName, short userKey, String deviceType) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.userKey = userKey;
        this.deviceType = deviceType;
    }
public DeviceMapping() {
}


}