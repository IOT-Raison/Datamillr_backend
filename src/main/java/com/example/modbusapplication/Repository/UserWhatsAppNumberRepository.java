package com.example.modbusapplication.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.modbusapplication.Entity.UserWhatsAppNumber;
import java.util.List;

public interface UserWhatsAppNumberRepository extends JpaRepository<UserWhatsAppNumber, Long> {
    List<UserWhatsAppNumber> findByUserKey(int userKey);
    UserWhatsAppNumber findByUserKeyAndWhatsappNumber(short userKey, String whatsappNumber);
    void deleteByUserKey(short userKey);

}



