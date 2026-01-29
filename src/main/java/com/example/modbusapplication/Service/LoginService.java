package com.example.modbusapplication.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.example.modbusapplication.Entity.DeviceMapping;
import com.example.modbusapplication.Entity.LoginInformation;
import com.example.modbusapplication.Entity.UserInformation;
import com.example.modbusapplication.Model.DeviceNameFlowDAO;
import com.example.modbusapplication.Model.DeviceNamePackingDAO;
import com.example.modbusapplication.Model.LoginResponseDAO;
import com.example.modbusapplication.Model.ModbusEntityDao;
import com.example.modbusapplication.Model.PackingEntityDao;
import com.example.modbusapplication.Model.UpdateUserDAO;
import com.example.modbusapplication.Repository.DeviceMappingRepository;
import com.example.modbusapplication.Repository.LoginInformationRepository;
import com.example.modbusapplication.Repository.PackingRepository;
import com.example.modbusapplication.Repository.FlowRepository;
import com.example.modbusapplication.Repository.UserInformationRepository;

import jakarta.transaction.Transactional;

@Service
public class LoginService {

    @Autowired
    UserInformationRepository userRepository;
    @Autowired
    DeviceMappingRepository deviceMappingRepository;
    @Autowired
    LoginInformationRepository authSessionRepository;
    @Autowired
    FlowRepository modbusRecordRepository;
    @Autowired
    PackingRepository packingRepository; 

    private static final String SEPARATOR = "124 124 124";

    public String generateRandomNumber() {
        return String.valueOf(new Random().nextInt(10));
    }

    public String storeLoginInfo(String ipAddress) {
        String randomNumber = generateRandomNumber();
        Optional<LoginInformation> existingSession = authSessionRepository.findByIpAddress(ipAddress);

        LoginInformation session = existingSession.orElse(new LoginInformation());
        session.setIpAddress(ipAddress);
        session.setRandomNumber(randomNumber);
        session.setHitTime(LocalDateTime.now());
        authSessionRepository.save(session);

        return randomNumber;
    }

    public ResponseEntity<LoginResponseDAO> loginResponse(String hashedCredential, String ipAddress) {
        // 1) find session
        Optional<LoginInformation> sessionOpt = authSessionRepository.findByIpAddress(ipAddress);
        // System.out.println("ipaddress: " + ipAddress);
        if (sessionOpt.isEmpty())
            return ResponseEntity.status(401).body(null);
        LoginInformation session = sessionOpt.get();

        // 2) decode
        int rand = Integer.parseInt(session.getRandomNumber());
        String[] parts = hashedCredential.split(Pattern.quote(SEPARATOR));

        // System.out.println("HashedCredential received: " + hashedCredential);
        // System.out.println("Split parts count: " + parts.length);
        // for (int i = 0; i < parts.length; i++) {
        // System.out.println("Part " + i + ": " + parts[i]);
        // }

        if (parts.length != 2)
            return ResponseEntity.badRequest().body(null);
        String decodedUserId = decodeAscii(parts[0].trim(), rand);
        String decodedPassword = decodeAscii(parts[1].trim(), rand);

        // System.out.println("✅ Decoded User ID: " + decodedUserId);
        // System.out.println("✅ Decoded Password: " + decodedPassword);

        // 3) authenticate
        Optional<UserInformation> userOpt = userRepository.findByUserId(decodedUserId);
        if (userOpt.isEmpty())
            return ResponseEntity.status(401).body(null);
        UserInformation user = userOpt.get();
        boolean ok = user.getLoginPassword().equals(decodedPassword);     
        session.setUserKey(user.getUserKey());
        session.setStatus(ok);
        authSessionRepository.save(session);

        if (!ok)
            return ResponseEntity.status(401).body(null);

        // 5) build response payload
        List<DeviceMapping> devices = deviceMappingRepository.findByUserKey(user.getUserKey());

        Map<String, List<Short>> deviceMap = new HashMap<>();
        for (DeviceMapping device : devices) {
            deviceMap.computeIfAbsent(device.getDeviceName(), k -> new ArrayList<>()).add(device.getDeviceId());
        }

        List<String> deviceNames = new ArrayList<>();
        List<Short> deviceIds = new ArrayList<>();

        for (DeviceMapping device : devices) {
            deviceNames.add(device.getDeviceName());
            deviceIds.add(device.getDeviceId());
        }

        List<DeviceNameFlowDAO> flowdeviceList = new ArrayList<>();
        List<DeviceNamePackingDAO> packingdeviceList = new ArrayList<>();

        for (DeviceMapping device : devices) {

    String machineName;
    LocalDateTime time = null;
    Double flowRate = null;
    Double totalWeight = null;
    Double setWeight = null;
    String batchName = null;
    int bagWeightSet = 0;
    int bagCount = 0;
    //Double weightOfBag = null;
    double accumulatedWeight = 0;
    String machineStatus = null;

        


    if ("packing".equalsIgnoreCase(device.getDeviceType())) {
        // Packing device
        machineName = "Packing Machine";
        List<PackingEntityDao> latestRows = packingRepository.getPackingDataByDeviceIdForLogin(device.getDeviceId());
        PackingEntityDao latest = latestRows.isEmpty() ? null : latestRows.get(0);

        if (latest != null) {
            time = latest.getTimestamp();
            batchName = latest.getBatchId();         
            bagWeightSet = latest.getBagWeightSet(); 
            bagCount = latest.getBagCount();
            //weightOfBag = (double) latest.getWeightOfBag();
            accumulatedWeight = latest.getAccumulatedWeight();
            machineStatus = latest.getMachineStatus();
        }

    } else {
        // Flow control device
        machineName = "Flow Control";
        List<ModbusEntityDao> latestRows = modbusRecordRepository.getDataByDeviceIdForLogin(device.getDeviceId());
        ModbusEntityDao latest = latestRows.isEmpty() ? null : latestRows.get(0);

        if (latest != null) {
            time = latest.getTimestamp();
            flowRate = (double) latest.getFlowrate();
            totalWeight = (double) latest.getTotalWeight();
            setWeight = (double) latest.getSetWeight();
            batchName = latest.getBatchName();
        }
    }

   if ("packing".equalsIgnoreCase(device.getDeviceType())) {
    packingdeviceList.add(new DeviceNamePackingDAO(
            device.getDeviceName(),
            device.getDeviceId(),
            machineName,
            time,
            batchName,
            bagWeightSet,
            bagCount,
            accumulatedWeight,
            machineStatus
    ));
} else {
    flowdeviceList.add(new DeviceNameFlowDAO(
            device.getDeviceName(),
            device.getDeviceId(),
            machineName,
            time,
            flowRate,
            totalWeight,
            setWeight,
            batchName
    ));
}

}


        LoginResponseDAO response = new LoginResponseDAO(
                user.getUserId(),
                user.getUserKey(),
                user.getCompanyName(),
                true,
                user.isNewUser(),
                flowdeviceList,
                packingdeviceList);

        return ResponseEntity.ok(response);
    }

    public void updateUserKeyAndStatus(String ipAddress, int userKey, boolean status) {
        Optional<LoginInformation> sessionOpt = authSessionRepository.findByIpAddress(ipAddress);
        if (sessionOpt.isPresent()) {
            LoginInformation session = sessionOpt.get();
            session.setUserKey(userKey);
            session.setStatus(status);
            authSessionRepository.save(session);
        }
    }

    public String decodeAscii(String encodedText, int randomNumber) {
        // System.out.println("Received encodedUserId: " + encodedText);

        String[] encodedParts = encodedText.split(" ");
        StringBuilder decoded = new StringBuilder();

        for (int i = 0; i < encodedParts.length; i++) {
            try {
                int asciiValue = Integer.parseInt(encodedParts[i]);
                if (i % 2 == 0) {
                    asciiValue -= randomNumber;
                } else {
                    asciiValue += randomNumber;
                }
                decoded.append((char) asciiValue);
                // System.out.println("Decoded userId: " + randomNumber);
            } catch (NumberFormatException e) {
                System.out.println("Invalid ASCII value at index " + i + ": " + encodedParts[i]);
            }
        }
        String decodedString = decoded.toString();
        // System.out.println("Decoded userId: " + decodedString);
        return decodedString;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000) // every 1 hour
    @Transactional
    public void cleanOldSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(12);
        authSessionRepository.deleteOlderThan(cutoff);
        // System.out.println("Old login sessions cleaned at " + LocalDateTime.now());
    }

    public ResponseEntity<?> updateUserIdPassword(UpdateUserDAO updateUserDAO) {
        Optional<UserInformation> userOpt = userRepository.findByUserId(updateUserDAO.getOldUserId());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User ID not found"));
        }

        UserInformation user = userOpt.get();
        if (!user.getLoginPassword().equals(updateUserDAO.getOldPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Old password is incorrect"));
        }

        boolean updated = false;
        if (updateUserDAO.getNewUserId() != null && !updateUserDAO.getNewUserId().trim().isEmpty()) {
            user.setUserId(updateUserDAO.getNewUserId());
            updated = true;
        }
        if (updateUserDAO.getNewPassword() != null && !updateUserDAO.getNewPassword().trim().isEmpty()) {
            user.setLoginPassword(updateUserDAO.getNewPassword());
            updated = true;
        }
    //     String otp = null;
    //     if (updateUserDAO.getWhatsappNumber() != null && !updateUserDAO.getWhatsappNumber().trim().isEmpty()) {
    //         user.setWhatsappNumber(updateUserDAO.getWhatsappNumber());
    //         user.setWhatsappEnabled(false); // not verified yet
    //         updated = true;
    //     otp = String.format("%04d", (int)(Math.random() * 10000));

    //     whatsAppService.sendWhatsAppMessage(
    //             updateUserDAO.getWhatsappNumber(),
    //             "Your verification code is: " + otp
    //     );
    // }

        if (user.isNewUser()) {
            user.setNewUser(false);
        }

        if (updated) {
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Credentials updated."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "No new credentials provided to update"));
        }
    }

//     public ResponseEntity<?>  updateWhatsAppSettings(UpdateWhatsAppDAO request) {
//         short userKey;
//         userKey = Short.parseShort(request.getUserKey());
//     Optional<UserInformation> userOpt = userRepository.findByUserKey(userKey);

//     if (userOpt.isEmpty()) {
//         return ResponseEntity.status(404).body(Map.of("error", "User not found"));
//     }

//     UserInformation user = userOpt.get();
//     user.setWhatsappNumber(request.getWhatsappNumber());
//     user.setWhatsappEnabled(request.isWhatsappEnabled());

//     userRepository.save(user);

//     return ResponseEntity.ok(Map.of("message", "WhatsApp settings updated successfully"));
// }


}