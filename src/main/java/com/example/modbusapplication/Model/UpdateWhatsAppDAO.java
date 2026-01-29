package com.example.modbusapplication.Model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateWhatsAppDAO {
    private String userKey;        
    private String whatsappNumber;  
    private boolean whatsappEnabled; 
    private List<Short> devices; 
}