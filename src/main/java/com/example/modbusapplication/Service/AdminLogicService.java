package com.example.modbusapplication.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.modbusapplication.Entity.DeviceMapping;
import com.example.modbusapplication.Entity.DeviceOutDetails;
import com.example.modbusapplication.Entity.UserInformation;
import com.example.modbusapplication.Entity.UserWhatsAppNumber;
import com.example.modbusapplication.Entity.WhatsAppDeviceMapping;
import com.example.modbusapplication.Model.AdminUserDetailsDTO;
import com.example.modbusapplication.Model.DeviceDetailsDTO;
import com.example.modbusapplication.Model.DeviceOutDetailsDTO;
import com.example.modbusapplication.Model.RegDeviceDAO;
import com.example.modbusapplication.Model.SearchCompanyDao;
import com.example.modbusapplication.Model.WhatsAppDetailsDTO;
import com.example.modbusapplication.Repository.DeviceMappingRepository;
import com.example.modbusapplication.Repository.DeviceOutDetailsRepository;
import com.example.modbusapplication.Repository.FlowRepository;
import com.example.modbusapplication.Repository.PackingRepository;
import com.example.modbusapplication.Repository.UserInformationRepository;
import com.example.modbusapplication.Repository.UserWhatsAppNumberRepository;
import com.example.modbusapplication.Repository.WhatsAppDeviceMappingRepository;

@Service
public class AdminLogicService {

    @Autowired
    FlowRepository modbusRecordRepository;
    @Autowired
    PackingRepository packingRepository;
    @Autowired
    UserInformationRepository userRepository;
    @Autowired
    DeviceMappingRepository deviceMappingRepository;
    @Autowired
    private UserWhatsAppNumberRepository userWhatsAppNumberRepository;
    @Autowired
    private WhatsAppDeviceMappingRepository whatsAppDeviceMappingRepository;
    @Autowired
    private DeviceOutDetailsRepository repository;

    public ResponseEntity<?> createTable(String deviceId, String machineType) {
    try {
        try {
            short shDeviceId = Short.parseShort(deviceId);
            DeviceMapping deviceMapping = new DeviceMapping();
            deviceMapping.setDeviceId(shDeviceId);
            deviceMapping.setDeviceType(machineType);
            deviceMappingRepository.save(deviceMapping);

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("The Device ID already present. Try different Device ID :(");
        }

        // Decide table creation based on machine type
        if ("flowcontrol".equalsIgnoreCase(machineType)) {
            modbusRecordRepository.createFlowControlTable(deviceId);
        } else if ("packing".equalsIgnoreCase(machineType)) {
            packingRepository.packing_createTable(deviceId);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid machine type. Allowed: flowcontrol, packing");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Table created successfully :)");

    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Problem in inserting in table.");
    }
}


   public ResponseEntity<?> registerdevice(RegDeviceDAO regDeviceDAO) {
        try {
            if (regDeviceDAO.isNewUser()) {
                short userKey = (short) (100 + new Random().nextInt(30000));
                regDeviceDAO.setUserKey(userKey);

            }

            if (!checkDeviceMapping(regDeviceDAO)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Device ID not registered"));
            }

            if (regDeviceDAO.isNewUser()) {
                if (!createNewUser(regDeviceDAO)) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Error while creating the new user"));
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(regDeviceDAO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }
    
    @CacheEvict(value = "companyNames", allEntries = true)
    public boolean createNewUser(RegDeviceDAO regDeviceDAO) {

        String cleanedCompany = regDeviceDAO.getCompanyName().replaceAll("[^a-zA-Z0-9]", "");
        String userIdPart = cleanedCompany.length() >= 6 ? cleanedCompany.substring(0, 6) : cleanedCompany;
        userIdPart = String.format("%-6s", userIdPart).replace(' ', 'X').toUpperCase();
        int deviceIdNumber = regDeviceDAO.getDeviceId();
        String suffix = String.format("%05d", deviceIdNumber);

        String userId = userIdPart + suffix;
        String password = suffix;

        UserInformation user = new UserInformation(regDeviceDAO.getUserKey(), userId, password,
                regDeviceDAO.getCompanyName(), true);
        try {
            userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace(); 
            System.out.println("createNewUser :: Exception :: " + e);
            return false;
        }
        regDeviceDAO.setUserId(userId);
        regDeviceDAO.setPassword(password);
        regDeviceDAO.setUserKey(regDeviceDAO.getUserKey());
        System.out.println("Returning: " + regDeviceDAO.getUserId() + ", " + regDeviceDAO.getPassword());
        return true;

    }

    public boolean checkDeviceMapping(RegDeviceDAO regDeviceDAO) {
        try {
            short deviceId = regDeviceDAO.getDeviceId();
            Optional<DeviceMapping> existingMapping = deviceMappingRepository.findByDeviceId(deviceId);

            if (existingMapping.isPresent()) {
                DeviceMapping mapping = existingMapping.get();
                mapping.setDeviceName(regDeviceDAO.getDeviceName());
                mapping.setUserKey(regDeviceDAO.getUserKey());
                deviceMappingRepository.save(mapping);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println("Exception" + e);
        }
        return false;
    }

    @Cacheable("companyNames")
    public List<SearchCompanyDao> getCompanyName() {
        return userRepository.findAllCompanyName();
    }


    public List<SearchCompanyDao> searchCompanyName(String companyName) {
        List<SearchCompanyDao> allUsers = getCompanyName();
        SimpleSearchEngine engine = new SimpleSearchEngine(allUsers);
        return engine.search(companyName);
    }

public List<AdminUserDetailsDTO> getAllUsersForAdmin() {

    List<UserInformation> users = userRepository.findAll();
    List<AdminUserDetailsDTO> result = new ArrayList<>();

    for (UserInformation ui : users) {

        List<DeviceMapping> devices = deviceMappingRepository.findByUserKey(ui.getUserKey());
        List<UserWhatsAppNumber> whatsappNumbers = userWhatsAppNumberRepository.findByUserKey(ui.getUserKey());

        AdminUserDetailsDTO dto = new AdminUserDetailsDTO();
        dto.setUserKey(ui.getUserKey());
        dto.setUserId(ui.getUserId());
        dto.setPassword(ui.getLoginPassword());
        dto.setCompanyName(ui.getCompanyName());

        // Devices
        dto.setDevices(devices.stream().map(d ->
                new DeviceDetailsDTO(d.getDeviceId(), d.getDeviceName(), d.getDeviceType())
        ).toList());

        // WhatsApp details
        List<WhatsAppDetailsDTO> wDetails = new ArrayList<>();
        for (UserWhatsAppNumber w : whatsappNumbers) {
            List<Short> mappedDevices = whatsAppDeviceMappingRepository
                    .findByWhatsappNumberId(w.getId())
                    .stream()
                    .map(WhatsAppDeviceMapping::getDeviceId)
                    .toList();

            wDetails.add(new WhatsAppDetailsDTO(
                    w.getWhatsappNumber(),
                    w.isWhatsappEnabled(),
                    mappedDevices
            ));
        }

        dto.setWhatsappDetails(wDetails);

        result.add(dto);
    }

    return result;
}

@Transactional
public String deleteUser(short userKey) {

    // Step 1: Find user else throw error
    UserInformation user = userRepository.findByUserKey(userKey)
            .orElseThrow(() -> new RuntimeException("User not found"));

    // Step 2: Get WhatsApp numbers for this user
    List<UserWhatsAppNumber> whatsAppNumbers = userWhatsAppNumberRepository.findByUserKey(userKey);

    // Step 3: Delete WhatsApp device mappings for each whatsapp number
    for (UserWhatsAppNumber w : whatsAppNumbers) {
        whatsAppDeviceMappingRepository.deleteByWhatsappNumberId(w.getId());
    }

    // Step 4: Delete WhatsApp numbers
    userWhatsAppNumberRepository.deleteByUserKey(userKey);

    // Step 5: Delete devices
    deviceMappingRepository.deleteByUserKey(userKey);

    // Step 6: Delete user
    userRepository.delete(user);

    return "User deleted successfully";
}

 // ADD / CREATE
    public DeviceOutDetails addDeviceOut(DeviceOutDetailsDTO dto) {

        DeviceOutDetails d = new DeviceOutDetails();

        d.setDeviceId(dto.getDeviceId());
        d.setModelNumber(dto.getModelNumber());
        d.setSerialNumber(dto.getSerialNumber());

        d.setCustomerName(dto.getCustomerName());
        d.setClientLocation(dto.getClientLocation());
        d.setContactNumber(dto.getContactNumber());

        d.setInstalledDate(dto.getInstalledDate());
        d.setWarrantyFrom(dto.getWarrantyFrom());
        d.setWarrantyTo(dto.getWarrantyTo());

        d.setStatus(dto.getStatus());
        d.setRemarks(dto.getRemarks());

        d.setCreatedBy(dto.getCreatedBy());

        return repository.save(d);
    }

    // GET ALL
    public List<DeviceOutDetails> getAll() {
        return repository.findAll();
    }

    // GET BY ID
    public DeviceOutDetails getByDeviceId(short deviceId) {
        return repository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device Out Entry not found"));
    }

    // UPDATE
    public DeviceOutDetails updateDeviceOut(short deviceId, DeviceOutDetailsDTO dto) {
        DeviceOutDetails d = getByDeviceId(deviceId);

        d.setDeviceId(dto.getDeviceId());
        d.setModelNumber(dto.getModelNumber());
        d.setSerialNumber(dto.getSerialNumber());

        d.setCustomerName(dto.getCustomerName());
        d.setClientLocation(dto.getClientLocation());
        d.setContactNumber(dto.getContactNumber());

        d.setInstalledDate(dto.getInstalledDate());
        d.setWarrantyFrom(dto.getWarrantyFrom());
        d.setWarrantyTo(dto.getWarrantyTo());

        d.setStatus(dto.getStatus());
        d.setRemarks(dto.getRemarks());

        return repository.save(d);
    }

    // DELETE
    public void deleteDeviceOut(short deviceId) {
        repository.deleteByDeviceId(deviceId);
    }


}