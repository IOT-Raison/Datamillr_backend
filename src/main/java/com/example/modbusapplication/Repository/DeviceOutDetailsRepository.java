package com.example.modbusapplication.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.modbusapplication.Entity.DeviceOutDetails;

public interface DeviceOutDetailsRepository extends JpaRepository<DeviceOutDetails, Short> {

    Optional<DeviceOutDetails> findByDeviceId(short deviceId);
    
    void deleteByDeviceId(short deviceId);

    boolean existsByDeviceId(short deviceId);

}
