package com.example.modbusapplication.Repository;

import com.example.modbusapplication.Entity.DeviceMapping;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceMappingRepository extends JpaRepository<DeviceMapping, Short> {
      Optional<DeviceMapping> findByDeviceId(short deviceId);
      List<DeviceMapping> findByUserKey(short userKey);
      void deleteByUserKey(short userKey);


}
