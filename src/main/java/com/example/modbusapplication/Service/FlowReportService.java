package com.example.modbusapplication.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.example.modbusapplication.Model.BatchReportDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.modbusapplication.Model.ModbusDataRequestDTO;
import com.example.modbusapplication.Model.ModbusEntityDao;
import com.example.modbusapplication.Repository.FlowRepository;

@Service
public class FlowReportService {

   @Autowired
   FlowRepository modbusRecordRepository;

  public List<BatchReportDTO> flowReport(ModbusDataRequestDTO requestDAO) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    Short deviceId = requestDAO.getDeviceId();
    String startDateStr = requestDAO.getStartDate();
    String endDateStr = requestDAO.getEndDate();
    LocalDateTime start = null, end = null;

    try {
        if (startDateStr != null && endDateStr != null) {
            LocalDate startDate = LocalDate.parse(startDateStr, formatter);
            LocalDate endDate = LocalDate.parse(endDateStr, formatter);
            start = startDate.atStartOfDay();
            end = endDate.atTime(23, 59, 59);
        } else if (startDateStr != null) {
            LocalDate date = LocalDate.parse(startDateStr, formatter);
            start = date.atStartOfDay();
            end = date.atTime(23, 59, 59);
        }

        List<BatchReportDTO> rows =
                modbusRecordRepository.getDataForBatch(deviceId, start, end);

        return rows;

    } catch (Exception e) {
        throw new IllegalArgumentException("Invalid input: " + e.getMessage(), e);
    }
}

public List<ModbusEntityDao> getFlowData(ModbusDataRequestDTO request) {
    short deviceId = request.getDeviceId();
    LocalDateTime start = LocalDateTime.parse(request.getStartDate());
    LocalDateTime end = LocalDateTime.parse(request.getEndDate());
    String batchName = request.getBatchname();

    return modbusRecordRepository.getDataByDeviceIdAndDateRangeAndBatchName(deviceId, start, end, batchName);
}




}
