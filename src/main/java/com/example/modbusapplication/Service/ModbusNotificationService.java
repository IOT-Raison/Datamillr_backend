package com.example.modbusapplication.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.modbusapplication.Entity.DeviceMapping;
import com.example.modbusapplication.Entity.UserInformation;
//import com.example.modbusapplication.Entity.UserInformation;
import com.example.modbusapplication.Entity.UserWhatsAppNumber;
import com.example.modbusapplication.Entity.WhatsAppDeviceMapping;
import com.example.modbusapplication.Model.BatchReportDTO;
import com.example.modbusapplication.Repository.DeviceMappingRepository;
import com.example.modbusapplication.Repository.UserInformationRepository;
import com.example.modbusapplication.Repository.UserWhatsAppNumberRepository;
import com.example.modbusapplication.Repository.WhatsAppDeviceMappingRepository;


@Service
public class ModbusNotificationService {

        @Autowired
        private UserInformationRepository userInformationRepository;

    private final WhatsAppService whatsAppService;
    private final DeviceMappingRepository deviceMappingRepository;
   // private final UserInformationRepository userInformationRepository;
    private final UserWhatsAppNumberRepository userWhatsAppNumberRepository;
    private final WhatsAppDeviceMappingRepository whatsAppDeviceMappingRepository;

    public ModbusNotificationService(
            WhatsAppService whatsAppService,
            DeviceMappingRepository deviceMappingRepository,
            UserInformationRepository userInformationRepository,
            UserWhatsAppNumberRepository userWhatsAppNumberRepository,
            WhatsAppDeviceMappingRepository whatsAppDeviceMappingRepository) {

        this.whatsAppService = whatsAppService;
        this.deviceMappingRepository = deviceMappingRepository;
       // this.userInformationRepository = userInformationRepository;
        this.userWhatsAppNumberRepository = userWhatsAppNumberRepository;
        this.whatsAppDeviceMappingRepository = whatsAppDeviceMappingRepository;
    }
    public void sendBtchReport(BatchReportDTO batchReportDTO) {
    try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy | hh:mm:ss a");

        String formattedStartTime = batchReportDTO.getStartDate() != null
                ? batchReportDTO.getStartDate().format(formatter)
                : "N/A";
        String formattedEndTime = batchReportDTO.getEndDate() != null
                ? batchReportDTO.getEndDate().format(formatter)
                : "N/A";

        short deviceId = batchReportDTO.getDeviceId();

        // 1️Get device info
        DeviceMapping mapping = deviceMappingRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found: " + deviceId));

        // 2️Find all WhatsApp mappings for this device
        List<WhatsAppDeviceMapping> deviceMappings =
                whatsAppDeviceMappingRepository.findByDeviceId(deviceId);

        if (deviceMappings.isEmpty()) {
            System.out.println("No WhatsApp numbers linked to device " + deviceId);
            return;
        }

        // 3️For each mapping, find the actual WhatsApp number
        for (WhatsAppDeviceMapping link : deviceMappings) {
            Optional<UserWhatsAppNumber> optionalNumber =
                    userWhatsAppNumberRepository.findById(link.getWhatsappNumberId());

            if (optionalNumber.isEmpty()) continue;

            UserWhatsAppNumber number = optionalNumber.get();

            // Skip disabled numbers
            if (!number.isWhatsappEnabled()) continue;

            // 4️Prepare WhatsApp variables
            Map<String, Object> vars = Map.of(
                    "1", mapping.getDeviceName(),
                    "2", batchReportDTO.getBatchName(),
                    "3", formattedStartTime,
                    "4", formattedEndTime,
                    "5", batchReportDTO.getTotalWeight()
            );

            String templateSid = "HX30d2bf96a19a27db2b5dc131eae24601";

            whatsAppService.sendTemplateMessage(number.getWhatsappNumber(), templateSid, vars);

            System.out.println("WhatsApp sent to " + number.getWhatsappNumber()
                    + " for device " + mapping.getDeviceName());
        }

    } catch (Exception e) {
        System.err.println("Error sending WhatsApp message: " + e.getMessage());
        e.printStackTrace();
    }
}


public void sendSchedulerTemplate(BatchReportDTO batchReportDTO) {
    try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a");

        String formattedStartTime = batchReportDTO.getStartDate() != null
                ? batchReportDTO.getStartDate().format(formatter)
                : "N/A";
        String formattedEndTime = batchReportDTO.getEndDate() != null
                ? batchReportDTO.getEndDate().format(formatter)
                : "N/A";

        short deviceId = batchReportDTO.getDeviceId();

        // 1️Get device info
        DeviceMapping mapping = deviceMappingRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found: " + deviceId));

        // 2️Fetch company name from user_information table
        String companyName = userInformationRepository.findByUserKey(mapping.getUserKey())
                .map(UserInformation::getCompanyName)
                .orElse("User");

        // 3️Get WhatsApp number mappings for this device
        List<WhatsAppDeviceMapping> deviceMappings =
                whatsAppDeviceMappingRepository.findByDeviceId(deviceId);

        if (deviceMappings.isEmpty()) {
            System.out.println("No WhatsApp numbers linked to device " + mapping.getDeviceName());
            return;
        }

        // 4️Loop through each linked WhatsApp number
        for (WhatsAppDeviceMapping link : deviceMappings) {
            Optional<UserWhatsAppNumber> optionalNumber =
                    userWhatsAppNumberRepository.findById(link.getWhatsappNumberId());

            if (optionalNumber.isEmpty()) continue;

            UserWhatsAppNumber number = optionalNumber.get();
            if (!number.isWhatsappEnabled()) continue;

            // 5️Prepare variables for the Twilio template
            // Hello {{1}}!
            // Here is your scheduler batch update for device {{2}}
            // Batch Name: {{3}}
            // Start Time: {{4}}
            // Last Update Time: {{5}}
            // Last Total Weight: {{6}}

            Map<String, Object> vars = Map.of(
                    "1", companyName, // ← dynamic from user_information
                    "2", mapping.getDeviceName(),
                    "3", batchReportDTO.getBatchName() != null ? batchReportDTO.getBatchName() : "N/A",
                    "4", formattedStartTime,
                    "5", formattedEndTime,
                    "6", batchReportDTO.getTotalWeight()
            );

            // Use your approved Twilio scheduler template SID
            String templateSid = "HX2c52ad82046f915364deb9704246dd50";

            whatsAppService.sendTemplateMessage(number.getWhatsappNumber(), templateSid, vars);

            System.out.println("Scheduler message sent to " + number.getWhatsappNumber()
                    + " for device " + mapping.getDeviceName());
        }

    } catch (Exception e) {
        System.err.println("Error sending scheduler WhatsApp message: " + e.getMessage());
        e.printStackTrace();
    }
}

public void sendPackingBatchCompleted(BatchReportDTO batchReportDTO, Map<Integer, Integer> bagWeightCountMap) {
    try {
       
        DateTimeFormatter dateTimeFormatter =
                DateTimeFormatter.ofPattern("dd-MM-yyyy | hh:mm:ss a");

        short deviceId = batchReportDTO.getDeviceId();

        /* ---------------- Device info ---------------- */
        DeviceMapping mapping = deviceMappingRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found: " + deviceId));

        /* ---------------- WhatsApp mappings ---------------- */
        List<WhatsAppDeviceMapping> deviceMappings =
                whatsAppDeviceMappingRepository.findByDeviceId(deviceId);

        if (deviceMappings.isEmpty()) {
            System.out.println("No WhatsApp numbers linked to device " + deviceId);
            return;
        }

        /* ---------------- Calculate totals ---------------- */
        int totalBags = 0;
        int totalSetWeight = 0;

        StringBuilder bagSummary = new StringBuilder();

        for (Map.Entry<Integer, Integer> entry : bagWeightCountMap.entrySet()) {
            int bagWeight = entry.getKey();
            int bagCount = entry.getValue();

            totalBags += bagCount;
            totalSetWeight += bagWeight * bagCount;

            bagSummary.append(bagWeight)
                      .append(" KG Bags : ")
                      .append(bagCount)
                      .append("\n");
        }

        String startTime = batchReportDTO.getStartDate() != null
                ? batchReportDTO.getStartDate().format(dateTimeFormatter)
                : "N/A";

        String endTime = batchReportDTO.getEndDate() != null
                ? batchReportDTO.getEndDate().format(dateTimeFormatter)
                : "N/A";

        /* ---------------- Send WhatsApp ---------------- */
        for (WhatsAppDeviceMapping link : deviceMappings) {

            Optional<UserWhatsAppNumber> optionalNumber =
                    userWhatsAppNumberRepository.findById(link.getWhatsappNumberId());

            if (optionalNumber.isEmpty()) continue;

            UserWhatsAppNumber number = optionalNumber.get();
            if (!number.isWhatsappEnabled()) continue;

            /*
             * WhatsApp Template Variables
             *
             * {{1}} Device Name
             * {{2}} Batch ID
             * {{3}} Bag Summary
             * {{4}} Total Bags
             * {{5}} Total Set Weight
             * {{6}} Total Weight 
             * {{7}} Start Time
             * {{8}} End Time
             */
            Map<String, Object> vars = Map.of(
                    "1", mapping.getDeviceName(),
                    "2", batchReportDTO.getBatchName(),
                    "3", bagSummary.toString(),
                    "4", totalBags,
                    "5", totalSetWeight,
                    "6", batchReportDTO.getTotalWeight(),
                    "7", startTime,
                    "8", endTime
            );

            // 🔴 Replace with your approved WhatsApp template SID
            String templateSid = "HX7cdacea75d88e9c3e7694c2389a35a26";

            whatsAppService.sendTemplateMessage(
                    number.getWhatsappNumber(),
                    templateSid,
                    vars
            );

            System.out.println("Packing batch WhatsApp sent to "
                    + number.getWhatsappNumber()
                    + " for device " + mapping.getDeviceName());
        }

    } catch (Exception e) {
        System.err.println("Error sending packing batch WhatsApp: " + e.getMessage());
        e.printStackTrace();
    }
}

}
