package com.example.modbusapplication.Controller;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.modbusapplication.Model.BatchReportDTO;
import com.example.modbusapplication.Model.ModbusDataRequestDTO;
import com.example.modbusapplication.Model.ModbusEntityDao;
import com.example.modbusapplication.Model.PackingEntityDao;
import com.example.modbusapplication.Repository.FlowRepository;
import com.example.modbusapplication.Service.ExcelExportService;
import com.example.modbusapplication.Service.FlowReportService;
import com.example.modbusapplication.Service.PackingReportService;

@RestController
@RequestMapping("/report")
public class ReportController {


    @Autowired
    private FlowReportService flowService;
    @Autowired
    private PackingReportService packingService;
    @Autowired
    FlowRepository flowRepository;
    @Autowired
    ExcelExportService excelExportService;

  @PostMapping("/fetch")
    public ResponseEntity<?> fetchReport(@RequestBody ModbusDataRequestDTO requestDAO) {
        try {
            Short deviceId = requestDAO.getDeviceId();
            if (deviceId == null) {
                return ResponseEntity.badRequest().body("Device ID must be provided");
            }

            boolean packingMachine = flowRepository.isPackingMachine(deviceId);

            Object response;
            if (packingMachine) {
                response = packingService.packingReport(requestDAO);
            } else {
                response = flowService.flowReport(requestDAO);
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }

    @PostMapping("/get-view-data")
public ResponseEntity<List<ModbusEntityDao>> getFlowData(@RequestBody ModbusDataRequestDTO request) {
    List<ModbusEntityDao> result = flowService.getFlowData(request);
    return ResponseEntity.ok(result);
}


    @GetMapping("/fetch-batch-details")
    public String fetchBatchDetails(@RequestBody ModbusDataRequestDTO requestDTO) {
        return new String();
    }

@PostMapping("/export-excel")
public ResponseEntity<InputStreamResource> exportBatchExcel(@RequestBody ModbusDataRequestDTO requestDTO) throws Exception {

    String deviceName = requestDTO.getDeviceName();
    String startDate = requestDTO.getStartDate();
    String endDate = requestDTO.getEndDate();
    String companyName = requestDTO.getCompanyName();

    boolean isPackingMachine = flowRepository.isPackingMachine(requestDTO.getDeviceId());

    List<BatchReportDTO> flowrecord;
    List<PackingEntityDao> packingrecord;

    InputStream excelStream;

    if (isPackingMachine) {
        System.out.println("Packing Machine Batch Report Export");
        packingrecord = packingService.packingReport(requestDTO);
        excelStream = excelExportService.exportPackingToExcel(packingrecord, companyName, deviceName, startDate, endDate); 
    } else {
        System.out.println("Flow Control Batch Report Export");
        flowrecord = flowService.flowReport(requestDTO); 
        excelStream = excelExportService.exportFlowToExcel(flowrecord, companyName, deviceName, startDate, endDate); 
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate startDatetime = LocalDate.parse(startDate, formatter);
    LocalDate endDatetime   = LocalDate.parse(endDate, formatter);

    String formattedStart = startDatetime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    String formattedEnd   = endDatetime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    String fileName = deviceName + "_" + formattedStart + "_to_" + formattedEnd + ".xlsx";

    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

    return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(new InputStreamResource(excelStream));
}


}
