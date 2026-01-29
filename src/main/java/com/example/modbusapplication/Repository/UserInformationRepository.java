package com.example.modbusapplication.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.modbusapplication.Entity.UserInformation;
import com.example.modbusapplication.Model.SearchCompanyDao;

public interface UserInformationRepository extends JpaRepository<UserInformation, Long> {
    @Query("SELECT MAX(u.userKey) FROM UserInformation u")
    Integer findMaxUserKey();

    Optional<UserInformation> findByUserId(String userId);

    Optional<UserInformation> findByCompanyName(String companyName);

    Optional<UserInformation> findByUserKey(short userKey);

    @Query("SELECT new com.example.modbusapplication.Model.SearchCompanyDao(u.companyName, u.userKey) FROM UserInformation u")
    List<SearchCompanyDao> findAllCompanyName();

}