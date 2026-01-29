package com.example.modbusapplication.Model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserDetailsDTO {

    private String userId;
    private String password;
    private String companyName;
    private int userKey;
    private List<DeviceDetailsDTO> devices; // list of devices
    private List<WhatsAppDetailsDTO> whatsappDetails; // list of whatsapp numbers

}

