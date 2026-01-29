package com.example.modbusapplication.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.modbusapplication.Entity.LoginInformation;

import jakarta.transaction.Transactional;

public interface LoginInformationRepository extends JpaRepository<LoginInformation, Long> {
    Optional<LoginInformation> findByIpAddress(String ipAddress);

    @Modifying
    @Transactional
    @Query("DELETE FROM LoginInformation l WHERE l.hitTime < :cutoff")
    void deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);

   

}
