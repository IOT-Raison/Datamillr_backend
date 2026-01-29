package com.example.modbusapplication.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.modbusapplication.Model.DeviceOutDetailsDTO;
import com.example.modbusapplication.Model.RegDeviceDAO;
import com.example.modbusapplication.Model.SearchCompanyDao;
import com.example.modbusapplication.Service.AdminLogicService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    AdminLogicService adminLogicService;

    @PostMapping("/create-table")
    public ResponseEntity<?> createTable(
        @RequestParam String deviceId,
        @RequestParam String machineType) {
    System.out.println("deviceId ==" + deviceId + " machineType==" + machineType);
    return adminLogicService.createTable(deviceId, machineType);
}

    @PostMapping("/register-device")
    public ResponseEntity<?> registerDevice(@RequestBody RegDeviceDAO regDeviceDAO) {
        System.out.println("");
        return adminLogicService.registerdevice(regDeviceDAO);

    }
    @GetMapping("/search-company-name")
    public ResponseEntity<?> getMethodName(@RequestParam String companyName) {
        List<SearchCompanyDao> searchedCompanyName = adminLogicService.searchCompanyName(companyName);
        return ResponseEntity.status(HttpStatus.OK).body(searchedCompanyName);

    }

    @GetMapping("/user-details")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(adminLogicService.getAllUsersForAdmin());
    } 

    @DeleteMapping("/delete-user/{userKey}")
    public ResponseEntity<?> deleteUser(@PathVariable short userKey) {
        try {
            String message = adminLogicService.deleteUser(userKey);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // CREATE ENTRY
@PostMapping("/add")
public ResponseEntity<?> addDevice(@RequestBody DeviceOutDetailsDTO dto) {
    try {
        return ResponseEntity.ok(adminLogicService.addDeviceOut(dto));
    } catch (RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }
}


    // LIST ALL
    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(adminLogicService.getAll());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable short deviceId) {
        return ResponseEntity.ok(adminLogicService.getByDeviceId(deviceId));
    }

    // UPDATE
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDevice(@PathVariable short deviceId,
                                          @RequestBody DeviceOutDetailsDTO dto) {
        return ResponseEntity.ok(adminLogicService.updateDeviceOut(deviceId, dto));
    }

    // DELETE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable short deviceId) {
        adminLogicService.deleteDeviceOut(deviceId);
        return ResponseEntity.ok(Map.of("message", "Device Out entry deleted successfully"));
    }

    

}
