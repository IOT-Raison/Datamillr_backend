package com.example.modbusapplication.Model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WhatsAppDetailsDTO {
    private String whatsappNumber;
    private boolean whatsappEnabled;
    private List<Short> mappedDeviceIds; 
    
        public WhatsAppDetailsDTO(String whatsappNumber, boolean whatsappEnabled, List<Short> mappedDeviceIds) {
        this.whatsappNumber = whatsappNumber;
        this.whatsappEnabled = whatsappEnabled;
        this.mappedDeviceIds = mappedDeviceIds;
    }

    public WhatsAppDetailsDTO() {}
}

