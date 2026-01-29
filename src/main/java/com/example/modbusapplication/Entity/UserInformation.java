package com.example.modbusapplication.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
@Entity
@Getter
@Setter
public class UserInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private short userKey;
    private String userId;
    private String loginPassword; 
    private String companyName;
    private boolean isNewUser;


    public UserInformation() {
    }

    public UserInformation(short userKey, String userId, String loginPassword, String companyName, boolean isNewUser) {
    this.userKey = userKey;
    this.userId = userId;
    this.loginPassword = loginPassword;
    this.companyName = companyName;
    this.isNewUser = isNewUser;

}

    public Long getId() {
        return id;
    }

     public void setId(Long id) {
         this.id = id;
     }

}