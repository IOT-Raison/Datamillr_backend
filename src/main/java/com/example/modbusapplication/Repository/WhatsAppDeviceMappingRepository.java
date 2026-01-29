package com.example.modbusapplication.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.modbusapplication.Entity.WhatsAppDeviceMapping;
import java.util.List;

public interface WhatsAppDeviceMappingRepository extends JpaRepository<WhatsAppDeviceMapping, Long> {
 List<WhatsAppDeviceMapping> findByDeviceId(short deviceId);
    List<WhatsAppDeviceMapping> findByWhatsappNumberId(Long whatsappNumberId);
    void deleteByWhatsappNumberId(long whatsappNumberId);

}