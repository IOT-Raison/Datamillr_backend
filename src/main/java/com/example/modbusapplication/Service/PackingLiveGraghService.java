package com.example.modbusapplication.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.modbusapplication.Model.ExportRequestDTO;
import com.example.modbusapplication.Model.ModbusDataRequestDTO;
import com.example.modbusapplication.Model.ModbusEntityDao;
import com.example.modbusapplication.Model.ModbusGroupedBatchResponse;
import com.example.modbusapplication.Model.ModbusSummaryResponse;
import com.example.modbusapplication.Model.PackingEntityDao;
import com.example.modbusapplication.Repository.FlowRepository;
import com.example.modbusapplication.Repository.PackingRepository;

@Service
public class PackingLiveGraghService {

    @Autowired
    FlowRepository modbusRecordRepository;
    @Autowired
    PackingRepository packingRepository;

    public Object fetchModbusDataFlexible(ModbusDataRequestDTO requestDAO) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    Short deviceId = requestDAO.getDeviceId();
    String startDateStr = requestDAO.getStartDate();
    String endDateStr = requestDAO.getEndDate();
    Integer month = requestDAO.getMonth();
    Integer year = requestDAO.getYear();
    Boolean allYearHistory = requestDAO.getAllYearHistory();

    if (deviceId == null) {
        throw new IllegalArgumentException("Device ID must be provided");
    }

    try {

        if (Boolean.TRUE.equals(allYearHistory)) {
            List<ModbusEntityDao> rows = modbusRecordRepository.getAllDataByDeviceId(deviceId);
            return createAllYearWeightSummary(rows);
        }

        // boolean packingMachine = modbusRecordRepository.isPackingMachine(deviceId);
        LocalDateTime start = null, end = null;

        if (month != null && year != null) {
            // Monthly summary
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            start = startDate.atStartOfDay();
            end = endDate.atTime(23, 59, 59);

            List<ModbusEntityDao> rows = modbusRecordRepository.getDataByDeviceIdAndDateRange(deviceId, start, end);
            return createMonthlySummary(rows);
        } else if (year != null) {
            // ⬅ Yearly summary
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);
            start = startDate.atStartOfDay();
            end = endDate.atTime(23, 59, 59);

            List<ModbusEntityDao> rows = modbusRecordRepository.getDataByDeviceIdAndDateRange(deviceId, start, end);
            return createYearlySummary(rows);
        }

        // ... (keep the rest of your daily/date-range/all logic)
        if (startDateStr != null && endDateStr != null) {
            LocalDate startDate = LocalDate.parse(startDateStr, formatter);
            LocalDate endDate = LocalDate.parse(endDateStr, formatter);
            start = startDate.atStartOfDay();
            end = endDate.atTime(23, 59, 59);
        } else if (startDateStr != null) {
            LocalDate date = LocalDate.parse(startDateStr, formatter);
            start = date.atStartOfDay();
            end = date.atTime(23, 59, 59);
        } else {
            
                packingRepository.getAllPackingDataByDeviceId(deviceId);
        }

       {     
            List<PackingEntityDao> rows =
                    packingRepository.getPackingDataByDeviceIdAndDateRange(deviceId, start, end);
            return rows;
        }

    } catch (Exception e) {
        throw new IllegalArgumentException("Invalid input: " + e.getMessage(), e);
    }
}




public ModbusGroupedBatchResponse getGroupedData(ExportRequestDTO requestDTO) {
    ModbusDataRequestDTO dao = new ModbusDataRequestDTO();
    
    // Set required fields (all are strings)
    dao.setDeviceId(requestDTO.getDeviceId());
    dao.setStartDate(requestDTO.getStartDate());  // String like "2025-07-25"
    dao.setEndDate(requestDTO.getEndDate());

    Object result = fetchModbusDataFlexible(dao);

    if (result instanceof ModbusGroupedBatchResponse) {
        return (ModbusGroupedBatchResponse) result;
    } else {
        throw new IllegalArgumentException("Expected grouped batch response but got raw data.");
    }
}

private ModbusSummaryResponse createYearlySummary(List<ModbusEntityDao> rows) {
    Map<String, Double> monthMap = new TreeMap<>();

    DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM");

    for (ModbusEntityDao row : rows) {
        String month = row.getTimestamp().format(monthFormatter); // "July", "August", etc.
        monthMap.put(month, monthMap.getOrDefault(month, 0.0) + row.getTotalWeight());
    }

    List<ModbusSummaryResponse.SummaryItem> items = new ArrayList<>();
    double grandTotal = 0;

    for (Map.Entry<String, Double> entry : monthMap.entrySet()) {
        ModbusSummaryResponse.SummaryItem item = new ModbusSummaryResponse.SummaryItem();
        item.setLabel(entry.getKey()); // month name
        item.setTotalWeight(entry.getValue());
        items.add(item);
        grandTotal += entry.getValue();
    }

    ModbusSummaryResponse summary = new ModbusSummaryResponse();
    summary.setType("yearly");
    summary.setSummary(items);
    summary.setGrandTotal(grandTotal);

    return summary;
}



private ModbusSummaryResponse createMonthlySummary(List<ModbusEntityDao> rows) {
    Map<String, Double> dailyMap = new TreeMap<>();

    for (ModbusEntityDao row : rows) {
        String date = row.getTimestamp().toLocalDate().toString(); // "yyyy-MM-dd"
        dailyMap.put(date, dailyMap.getOrDefault(date, 0.0) + row.getTotalWeight());
    }

    List<ModbusSummaryResponse.SummaryItem> items = new ArrayList<>();
    double grandTotal = 0;

    for (Map.Entry<String, Double> entry : dailyMap.entrySet()) {
        ModbusSummaryResponse.SummaryItem item = new ModbusSummaryResponse.SummaryItem();
        item.setLabel(entry.getKey()); // date
        item.setTotalWeight(entry.getValue());
        items.add(item);
        grandTotal += entry.getValue();
    }

    ModbusSummaryResponse summary = new ModbusSummaryResponse();
    summary.setType("monthly");
    summary.setSummary(items);
    summary.setGrandTotal(grandTotal);

    return summary;
}


private ModbusSummaryResponse createAllYearWeightSummary(List<ModbusEntityDao> rows) {
    Map<Integer, Double> yearlyMap = new TreeMap<>();

    for (ModbusEntityDao row : rows) {
        if (row.getTimestamp() != null) {
            int year = row.getTimestamp().getYear(); // Extract year from timestamp
            yearlyMap.put(year, yearlyMap.getOrDefault(year, 0.0) + row.getTotalWeight());
        }
    }

    List<ModbusSummaryResponse.SummaryItem> items = new ArrayList<>();
    double grandTotal = 0;

    for (Map.Entry<Integer, Double> entry : yearlyMap.entrySet()) {
        ModbusSummaryResponse.SummaryItem item = new ModbusSummaryResponse.SummaryItem();
        item.setLabel(String.valueOf(entry.getKey())); // year as label
        item.setTotalWeight(entry.getValue());
        items.add(item);
        grandTotal += entry.getValue();
    }

    ModbusSummaryResponse summary = new ModbusSummaryResponse();
    summary.setType("allYearHistory");
    summary.setSummary(items);
    summary.setGrandTotal(grandTotal);

    return summary;
}
}
