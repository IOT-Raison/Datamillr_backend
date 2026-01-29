package com.example.modbusapplication.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.modbusapplication.Model.ModbusDataRequestDTO;
import com.example.modbusapplication.Model.PackingEntityDao;
import com.example.modbusapplication.Repository.FlowRepository;
import com.example.modbusapplication.Repository.PackingRepository;

@Service
public class PackingReportService {


    @Autowired
    FlowRepository modbusRecordRepository;
    @Autowired
    PackingRepository packingRepository;

    public List<PackingEntityDao> packingReport(ModbusDataRequestDTO requestDAO) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Short deviceId = requestDAO.getDeviceId();
        String startDateStr = requestDAO.getStartDate();
        String endDateStr = requestDAO.getEndDate();
        // boolean packingMachine = modbusRecordRepository.isPackingMachine(deviceId);
        LocalDateTime start = null, end = null;
        try{
           
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
            return packingRepository.getAllPackingDataByDeviceId(deviceId);
           
        }      
        List<PackingEntityDao> rows = packingRepository.getPackingDataByDeviceIdAndDateRange(deviceId, start, end);
        double previousAccumulated = 0;
        for (int i = 0; i < rows.size(); i++) {
            PackingEntityDao current = rows.get(i);
            double currentAccumulated = current.getAccumulatedWeight();
            double bagWeight = currentAccumulated - previousAccumulated;

            // first row has no previous accumulated value
            if (i == 0) {
                bagWeight = currentAccumulated;
            }
        current.setWeightOfBag(bagWeight);  // <-- new field
        previousAccumulated = currentAccumulated;
    }
    return rows;

    } catch (Exception e) {
        throw new IllegalArgumentException("Invalid input: " + e.getMessage(), e);
    }
    }

    }