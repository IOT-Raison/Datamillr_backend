package com.example.modbusapplication.Model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDAO {
    private String oldUserId;
    private String oldPassword;
    private String newUserId;
    private String newPassword;

}
