package com.example.modbusapplication.Repository;

import com.example.modbusapplication.Entity.WhatsAppScheduler;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SchedulerRepository extends JpaRepository<WhatsAppScheduler, Long> {
    Optional<WhatsAppScheduler> findByUserKey(int userKey);
}
