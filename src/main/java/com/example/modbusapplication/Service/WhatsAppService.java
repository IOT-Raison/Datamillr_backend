package com.example.modbusapplication.Service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.example.modbusapplication.Entity.UserInformation;
import com.example.modbusapplication.Entity.UserWhatsAppNumber;
import com.example.modbusapplication.Entity.WhatsAppDeviceMapping;
import com.example.modbusapplication.Entity.WhatsAppScheduler;
import com.example.modbusapplication.Model.UpdateWhatsAppDAO;
import com.example.modbusapplication.Repository.DeviceMappingRepository;
import com.example.modbusapplication.Repository.SchedulerRepository;
import com.example.modbusapplication.Repository.UserInformationRepository;
import com.example.modbusapplication.Repository.UserWhatsAppNumberRepository;
import com.example.modbusapplication.Repository.WhatsAppDeviceMappingRepository;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppService {

    @Value("${twilio.accountSid}")
    private String accountSid;

    @Value("${twilio.authToken}")
    private String authToken;

   @Value("${twilio.whatsapp.from}")
    private String fromWhatsAppNumber;

    @Autowired
    private UserInformationRepository userRepository;
    @Autowired
    DeviceMappingRepository deviceMappingRepository;
    @Autowired
    private UserWhatsAppNumberRepository userWhatsAppNumberRepository;
    @Autowired
    private WhatsAppDeviceMappingRepository whatsAppDeviceMappingRepository;
    @Autowired
    private SchedulerRepository schedulerRepository;


    public void sendWhatsAppMessage(String toNumber, String messageBody) {
        Twilio.init(accountSid, authToken);

        Message message = Message.creator(
                new com.twilio.type.PhoneNumber("whatsapp:" + toNumber),
                new com.twilio.type.PhoneNumber(fromWhatsAppNumber),
                messageBody
        ).create();

        System.out.println("WhatsApp message sent: " + message.getSid());
    }

public String sendTemplateMessage(String toNumber, String templateSid, Map<String, Object> variables) {
    Twilio.init(accountSid, authToken);

    // Normalize number format (handles +91, 91, 0, or missing)
    String raw = toNumber.trim();
    String phoneNumber;
    if (raw.startsWith("+91")) {
        phoneNumber = raw;
    } else if (raw.startsWith("91")) {
        phoneNumber = "+" + raw;
    } else if (raw.startsWith("0")) {
        phoneNumber = "+91" + raw.substring(1);
    } else {
        phoneNumber = "+91" + raw;
    }

    try {
        Message message = Message.creator(
                new PhoneNumber("whatsapp:" + phoneNumber),
                new PhoneNumber(fromWhatsAppNumber),
                "" // body handled via template SID
        )
        .setContentSid(templateSid)
        .setContentVariables(
                new Gson().toJson(
                        variables.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())))
                )
        )
        .create();

        System.out.println("WhatsApp sent to " + phoneNumber);
       // System.out.println(" Twilio Message SID: " + message.getSid());
        System.out.println("Status: " + message.getStatus());

        return "Success: " + message.getStatus();

    } catch (com.twilio.exception.ApiException e) {
        // Twilio API error (e.g. invalid number, unapproved template)
        System.err.println("Twilio API Error: " + e.getMessage());
        return "Error: " + e.getMessage();
    } catch (Exception e) {
        // Any unexpected error
        System.err.println("Unexpected Error: " + e.getMessage());
        return "Error: " + e.getMessage();
    }
}

 public void sendotpTemplateMessage(String rawNumber, String templateSid, int otp) {
        Twilio.init(accountSid, authToken);

        String toNumber = formatToE164(rawNumber);

        Map<String, Object> variables = new HashMap<>();
        variables.put("1", String.valueOf(otp));

        try {
            Message message = Message.creator(
                    new PhoneNumber("whatsapp:" + toNumber),
                    new PhoneNumber(fromWhatsAppNumber),
                    ""
            )
            .setContentSid(templateSid)
            .setContentVariables(new Gson().toJson(variables))
            .create();

            System.out.println("WhatsApp template message sent successfully! SID: " + message.getSid());

        } catch (ApiException e) {
            System.err.println("Failed to send WhatsApp message:");
            System.err.println("Status Code: " + e.getStatusCode());
            System.err.println("Error Code: " + e.getCode());
            System.err.println("Error Message: " + e.getMessage());
            throw new RuntimeException("Twilio Error: " + e.getMessage(), e);
        }
    }

    // Helper to ensure E.164 format
    private String formatToE164(String rawNumber) {
        rawNumber = rawNumber.replaceAll("\\s+", "").replaceAll("-", "");
        if (rawNumber.startsWith("+91")) return rawNumber;
        if (rawNumber.startsWith("91")) return "+" + rawNumber;
        if (rawNumber.startsWith("0")) return "+91" + rawNumber.substring(1);
        return "+91" + rawNumber;
    }

public ResponseEntity<?> updateWhatsAppSettings(UpdateWhatsAppDAO request) {
    short userKey = Short.parseShort(request.getUserKey());
    Optional<UserInformation> userOpt = userRepository.findByUserKey(userKey);

    if (userOpt.isEmpty()) {
        return ResponseEntity.status(404).body(Map.of("error", "User not found"));
    }

    // Step 1: Check if this number already exists for this user
    List<UserWhatsAppNumber> existingNumbers = userWhatsAppNumberRepository.findByUserKey(userKey);
    UserWhatsAppNumber number = existingNumbers.stream()
            .filter(n -> n.getWhatsappNumber().equals(request.getWhatsappNumber()))
            .findFirst()
            .orElse(null);

    // Step 2: If not exists, create new
    if (number == null) {
        number = new UserWhatsAppNumber();
        number.setUserKey(userKey);
        number.setWhatsappNumber(request.getWhatsappNumber());
    }

    number.setWhatsappEnabled(request.isWhatsappEnabled());
    number = userWhatsAppNumberRepository.save(number);

    // Step 3: Remove any old device mappings for this number (to refresh list)
    List<WhatsAppDeviceMapping> oldMappings = whatsAppDeviceMappingRepository.findByWhatsappNumberId(number.getId());
    whatsAppDeviceMappingRepository.deleteAll(oldMappings);

    // Step 4: Add new mappings
    if (request.getDevices() != null && !request.getDevices().isEmpty()) {
        for (Short deviceId : request.getDevices()) {
            WhatsAppDeviceMapping mapping = new WhatsAppDeviceMapping();
            mapping.setWhatsappNumberId(number.getId());
            mapping.setDeviceId(deviceId);
            whatsAppDeviceMappingRepository.save(mapping);
        }
    }

    return ResponseEntity.ok(Map.of(
            "message", "WhatsApp number and device mapping updated successfully",
            "whatsappNumber", number.getWhatsappNumber(),
            "enabled", number.isWhatsappEnabled()
    ));
}
public ResponseEntity<?> getUserWhatsAppDetails(short userKey) {
    try {
        // 1. Check if user exists
        Optional<UserInformation> userOpt = userRepository.findByUserKey(userKey);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        // 2. Get all WhatsApp numbers for this user
        List<UserWhatsAppNumber> numbers = userWhatsAppNumberRepository.findByUserKey(userKey);
        if (numbers.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "userKey", userKey,
                "numbers", List.of()
            ));
        }

        List<Map<String, Object>> responseList = new ArrayList<>();

        // 3. For each WhatsApp number, find mapped device names
        for (UserWhatsAppNumber number : numbers) {
            List<WhatsAppDeviceMapping> mappings =
                    whatsAppDeviceMappingRepository.findByWhatsappNumberId(number.getId());

            List<String> deviceNames = new ArrayList<>();
            for (WhatsAppDeviceMapping mapping : mappings) {
                deviceMappingRepository.findByDeviceId(mapping.getDeviceId()).ifPresent(device -> {
                    deviceNames.add(device.getDeviceName());
                });
            }

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("whatsappNumber", number.getWhatsappNumber());
            entry.put("enabled", number.isWhatsappEnabled());
            entry.put("devices", deviceNames);

            responseList.add(entry);
        }

         // 3. Fetch scheduler times for this user
        Optional<WhatsAppScheduler> schedulerOpt = schedulerRepository.findByUserKey(userKey);

        Map<String, Object> schedulerDetails = new LinkedHashMap<>();
        if (schedulerOpt.isPresent()) {
            WhatsAppScheduler s = schedulerOpt.get();
            schedulerDetails.put("time1", s.getTime1());
            schedulerDetails.put("time2", s.getTime2());
            schedulerDetails.put("time3", s.getTime3());
            schedulerDetails.put("time4", s.getTime4());
        } else {
            schedulerDetails.put("time1", null);
            schedulerDetails.put("time2", null);
            schedulerDetails.put("time3", null);
            schedulerDetails.put("time4", null);
        }

        // 4. Return final structure
        return ResponseEntity.ok(Map.of(
            "userKey", userKey,
            "numbers", responseList,
            "scheduler", schedulerDetails
        ));

    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(Map.of(
            "error", "Failed to fetch user WhatsApp details",
            "details", e.getMessage()
        ));
    }
}

public ResponseEntity<?> deleteWhatsAppNumber(short userKey, String whatsappNumber) {
    try {
        // 1. Check if number exists
        UserWhatsAppNumber number = userWhatsAppNumberRepository
                .findByUserKeyAndWhatsappNumber(userKey, whatsappNumber);

        if (number == null) {
            return ResponseEntity.status(404).body(Map.of(
                "message", "WhatsApp number not found for this user"
            ));
        }

        // 2. Delete device mappings first
        whatsAppDeviceMappingRepository.deleteAll(
                whatsAppDeviceMappingRepository.findByWhatsappNumberId(number.getId())
        );

        // 3. Delete the number
        userWhatsAppNumberRepository.delete(number);

        return ResponseEntity.ok(Map.of(
            "message", "WhatsApp number deleted successfully",
            "whatsappNumber", whatsappNumber
        ));

    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(Map.of(
            "message", "Failed to delete number",
            "error", e.getMessage()
        ));
    }
}
}
