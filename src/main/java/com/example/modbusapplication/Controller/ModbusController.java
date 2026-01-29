package com.example.modbusapplication.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.modbusapplication.Model.RawRecordDTO;
import com.example.modbusapplication.Service.ModbusRecordService;
import java.util.List;

@RestController
@RequestMapping("/modbus")
public class ModbusController {

    @Autowired
    private ModbusRecordService modbusRecordService;

    /** ---------------- NORMAL MODBUS DATA ---------------- */
    @PostMapping("/upload-bytes")
    public ResponseEntity<String> modbusRecords(@RequestBody List<RawRecordDTO> rawRecordDTOList) {
        System.out.println("Received encoded DTO records: " + rawRecordDTOList.size());

        int successCount = modbusRecordService.handleModbusData(rawRecordDTOList);
        
        if (successCount == rawRecordDTOList.size()) {
            return ResponseEntity.ok("All records decoded and stored successfully.");
        } else if (successCount > 0) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body("Some records were stored successfully. " + successCount + "/" + rawRecordDTOList.size());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to store any records.");
        }
    }



    /** ---------------- PACKING MACHINE DATA ---------------- */
    @PostMapping("/upload-packing-bytes")
    public ResponseEntity<String> packingRecords(@RequestBody List<RawRecordDTO> rawRecordDTOList) {
        System.out.println("Received encoded packing DTO records: " + rawRecordDTOList.size());

        int successCount = 0;
        for (RawRecordDTO dto : rawRecordDTOList) {
            boolean success = modbusRecordService.decodeAndStorePacking(dto.getEncByteString());
            if (success) {
                successCount++;
            } else {
                System.err.println("Failed to store decoded packing data for one record.");
            }
        }

        if (successCount == rawRecordDTOList.size()) {
            return ResponseEntity.ok("All packing records decoded and stored successfully.");
        } else if (successCount > 0) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body("Some packing records were stored successfully. " + successCount + "/" + rawRecordDTOList.size());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to store any packing records.");
        }
    }

}
