package com.example.modbusapplication.Controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.modbusapplication.Model.ModbusDataRequestDTO;
import com.example.modbusapplication.Repository.FlowRepository;
import com.example.modbusapplication.Service.FlowLiveGraghService;
import com.example.modbusapplication.Service.PackingLiveGraghService;


@RestController
@RequestMapping("/live")
public class LiveController {

    @Autowired
    private FlowLiveGraghService flowService;
    @Autowired
    private PackingLiveGraghService packingService;
    @Autowired
    private FlowRepository flowRepository;

    @PostMapping("/last/data")
    public ResponseEntity<?> getModbusData(@RequestBody ModbusDataRequestDTO requestDAO) {
        try {
            short deviceId = requestDAO.getDeviceId();

            Object response = flowRepository.getLastDataByDeviceId(deviceId);

            if (response == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No data found for deviceId " + deviceId));
            }

            return ResponseEntity.ok(Map.of(
                    "deviceId", deviceId,
                    "data", response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        } 
    }

    @PostMapping("/graph/data")
    public ResponseEntity<?> fetchModbusData(@RequestBody ModbusDataRequestDTO requestDAO) {
        try {
            Short deviceId = requestDAO.getDeviceId();
            if (deviceId == null) {
                return ResponseEntity.badRequest().body("Device ID must be provided");
            }
             boolean packingMachine = flowRepository.isPackingMachine(deviceId);

            Object response;
            if (packingMachine) {
                response = packingService.fetchModbusDataFlexible(requestDAO);
            } else {
                response = flowService.fetchModbusDataFlexible(requestDAO);
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }

}
