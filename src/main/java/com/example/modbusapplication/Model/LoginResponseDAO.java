package com.example.modbusapplication.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponseDAO {
    private String userId;
    private int userKey;
    private String companyName;
    private boolean isLoginSucess;
    private boolean isNewUser;
    private List<DeviceNameFlowDAO> flowdevices;
    private List<DeviceNamePackingDAO> packingdevices;
}