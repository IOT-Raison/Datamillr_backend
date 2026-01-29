// package com.example.modbusapplication.Service;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
// import java.util.TreeMap;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import com.example.modbusapplication.Model.ModbusSummaryResponse;
// import com.example.modbusapplication.Model.ModbusDataRequestDTO;
// import com.example.modbusapplication.Model.ModbusEntityDao;
// import com.example.modbusapplication.Repository.FlowRepository;

// @Service
// public class FlowLiveGraghService {
    
//    @Autowired
//    FlowRepository modbusRecordRepository;

//    public Object fetchModbusDataFlexible(ModbusDataRequestDTO requestDAO) {

//     Short deviceId = requestDAO.getDeviceId();
//     Integer month = requestDAO.getMonth();
//     Integer year = requestDAO.getYear();
//     Boolean allYearHistory = requestDAO.getAllYearHistory();

//     if (deviceId == null) {
//         throw new IllegalArgumentException("Device ID must be provided");
//     }

//     try {
//         if (Boolean.TRUE.equals(allYearHistory)) {
//             List<ModbusEntityDao> rows = modbusRecordRepository.getAllDataByDeviceId(deviceId);
//             return createAllYearWeightSummary(rows);
//         }

//         LocalDateTime start = null, end = null;

//         if (month != null && year != null) {
//             // Monthly summary
//             LocalDate startDate = LocalDate.of(year, month, 1);
//             LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
//             start = startDate.atStartOfDay();
//             end = endDate.atTime(23, 59, 59);

//             List<ModbusEntityDao> rows = modbusRecordRepository.getDataByDeviceIdAndDateRange(deviceId, start, end);
//             return createMonthlySummary(rows);
//         } else if (year != null) {
//             // ⬅ Yearly summary
//             LocalDate startDate = LocalDate.of(year, 1, 1);
//             LocalDate endDate = LocalDate.of(year, 12, 31);
//             start = startDate.atStartOfDay();
//             end = endDate.atTime(23, 59, 59);

//             List<ModbusEntityDao> rows = modbusRecordRepository.getDataByDeviceIdAndDateRange(deviceId, start, end);
//             return createYearlySummary(rows);
//         }

//         List<ModbusEntityDao> rows = modbusRecordRepository.getAllDataByDeviceId(deviceId);
//         rows = calculateWeightDifferences(rows);
//         return rows;
        
//     } catch (Exception e) {
//         throw new IllegalArgumentException("Invalid input: " + e.getMessage(), e);
//     }
// }

// private List<ModbusEntityDao> calculateWeightDifferences(List<ModbusEntityDao> rows) {
//     if (rows == null || rows.isEmpty()) {
//         return rows;
//     }

//     int previous = rows.get(0).getTotalWeight();
//     rows.get(0).setTotalWeight(0); // first entry → no diff

//     for (int i = 1; i < rows.size(); i++) {
//         int current = rows.get(i).getTotalWeight();
//         int diff = current - previous;
//         rows.get(i).setTotalWeight(diff);
//         previous = current;
//     }
//     return rows;
// }

//    private ModbusSummaryResponse createYearlySummary(List<ModbusEntityDao> rows) {
//     Map<String, Double> monthMap = new TreeMap<>();

//     DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM");

//     for (ModbusEntityDao row : rows) {
//         String month = row.getTimestamp().format(monthFormatter); // "July", "August", etc.
//         monthMap.put(month, monthMap.getOrDefault(month, 0.0) + row.getTotalWeight());
//     }

//     List<ModbusSummaryResponse.SummaryItem> items = new ArrayList<>();
//     double grandTotal = 0;

//     for (Map.Entry<String, Double> entry : monthMap.entrySet()) {
//         ModbusSummaryResponse.SummaryItem item = new ModbusSummaryResponse.SummaryItem();
//         item.setLabel(entry.getKey()); // month name
//         item.setTotalWeight(entry.getValue());
//         items.add(item);
//         grandTotal += entry.getValue();
//     }

//     ModbusSummaryResponse summary = new ModbusSummaryResponse();
//     summary.setType("yearly");
//     summary.setSummary(items);
//     summary.setGrandTotal(grandTotal);

//     return summary;
// }



// private ModbusSummaryResponse createMonthlySummary(List<ModbusEntityDao> rows) {
//     Map<String, Double> dailyMap = new TreeMap<>();

//     for (ModbusEntityDao row : rows) {
//         String date = row.getTimestamp().toLocalDate().toString(); // "yyyy-MM-dd"
//         dailyMap.put(date, dailyMap.getOrDefault(date, 0.0) + row.getTotalWeight());
//     }

//     List<ModbusSummaryResponse.SummaryItem> items = new ArrayList<>();
//     double grandTotal = 0;

//     for (Map.Entry<String, Double> entry : dailyMap.entrySet()) {
//         ModbusSummaryResponse.SummaryItem item = new ModbusSummaryResponse.SummaryItem();
//         item.setLabel(entry.getKey()); // date
//         item.setTotalWeight(entry.getValue());
//         items.add(item);
//         grandTotal += entry.getValue();
//     }

//     ModbusSummaryResponse summary = new ModbusSummaryResponse();
//     summary.setType("monthly");
//     summary.setSummary(items);
//     summary.setGrandTotal(grandTotal);

//     return summary;
// }


// private ModbusSummaryResponse createAllYearWeightSummary(List<ModbusEntityDao> rows) {
//     Map<Integer, Double> yearlyMap = new TreeMap<>();

//     for (ModbusEntityDao row : rows) {
//         if (row.getTimestamp() != null) {
//             int year = row.getTimestamp().getYear(); // Extract year from timestamp
//             yearlyMap.put(year, yearlyMap.getOrDefault(year, 0.0) + row.getTotalWeight());
//         }
//     }

//     List<ModbusSummaryResponse.SummaryItem> items = new ArrayList<>();
//     double grandTotal = 0;

//     for (Map.Entry<Integer, Double> entry : yearlyMap.entrySet()) {
//         ModbusSummaryResponse.SummaryItem item = new ModbusSummaryResponse.SummaryItem();
//         item.setLabel(String.valueOf(entry.getKey())); // year as label
//         item.setTotalWeight(entry.getValue());
//         items.add(item);
//         grandTotal += entry.getValue();
//     }

//     ModbusSummaryResponse summary = new ModbusSummaryResponse();
//     summary.setType("allYearHistory");
//     summary.setSummary(items);
//     summary.setGrandTotal(grandTotal);

//     return summary;
// }

// }


package com.example.modbusapplication.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.modbusapplication.Model.ModbusDataRequestDTO;
import com.example.modbusapplication.Model.ModbusEntityDao;
import com.example.modbusapplication.Model.ModbusSummaryResponse;
import com.example.modbusapplication.Repository.FlowRepository;

@Service
public class FlowLiveGraghService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
   FlowRepository modbusRecordRepository;

    /* --------------------------------------------------------------------
       MAIN CONTROLLER METHOD
    -------------------------------------------------------------------- */
    public Object fetchModbusDataFlexible(ModbusDataRequestDTO requestDAO) {

        Short deviceId = requestDAO.getDeviceId();
        Integer month = requestDAO.getMonth();
        Integer year = requestDAO.getYear();
        Boolean allYearHistory = requestDAO.getAllYearHistory();

        if (deviceId == null)
            throw new IllegalArgumentException("Device ID must be provided");

        if (Boolean.TRUE.equals(allYearHistory)) {
            List<Map<String, Object>> rows = fetchAll(deviceId);
            return createAllYearSummary(rows);
        }

        if (month != null && year != null) {
            List<Map<String, Object>> rows = fetchMonthlyRows(deviceId, year, month);
            return createMonthlySummary(rows);
        }

        if (year != null) {
            List<Map<String, Object>> rows = fetchYearlyRows(deviceId, year);
            return createYearlySummary(rows);
        }

        List<ModbusEntityDao> rows = modbusRecordRepository.getAllDataByDeviceId(deviceId);
        rows = calculateWeightDifferences(rows);
        return rows;
    }

    /* --------------------------------------------------------------------
       FETCH DAILY TABLE DATA
    -------------------------------------------------------------------- */

    // Fetch whole daily table
    private List<Map<String, Object>> fetchAll(short deviceId) {
        String sql = "SELECT record_date, total_weight FROM modbus_daily_data_" + deviceId +
                " ORDER BY record_date ASC";

        return jdbcTemplate.queryForList(sql);
    }

    private List<ModbusEntityDao> calculateWeightDifferences(List<ModbusEntityDao> rows) {
    if (rows == null || rows.isEmpty()) {
        return rows;
    }

    int previous = rows.get(0).getTotalWeight();
    rows.get(0).setTotalWeight(0); // first entry → no diff

    for (int i = 1; i < rows.size(); i++) {
        int current = rows.get(i).getTotalWeight();
        int diff = current - previous;
        rows.get(i).setTotalWeight(diff);
        previous = current;
    }
    return rows;
}

    // Fetch monthly rows
    private List<Map<String, Object>> fetchMonthlyRows(short deviceId, int year, int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        String sql = "SELECT record_date, total_weight FROM modbus_daily_data_" + deviceId +
                " WHERE record_date BETWEEN ? AND ? ORDER BY record_date ASC";

        return jdbcTemplate.queryForList(sql, start, end);
    }

    // Fetch yearly rows
    private List<Map<String, Object>> fetchYearlyRows(short deviceId, int year) {

        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        String sql = "SELECT record_date, total_weight FROM modbus_daily_data_" + deviceId +
                " WHERE record_date BETWEEN ? AND ? ORDER BY record_date ASC";

        return jdbcTemplate.queryForList(sql, start, end);
    }

    /* --------------------------------------------------------------------
       MONTHLY SUMMARY (Group by day)
    -------------------------------------------------------------------- */
    private ModbusSummaryResponse createMonthlySummary(List<Map<String, Object>> rows) {

        Map<String, Double> map = new TreeMap<>();

        for (Map<String, Object> row : rows) {
            String date = row.get("record_date").toString();
            double weight = ((Number) row.get("total_weight")).doubleValue();

            map.put(date, map.getOrDefault(date, 0.0) + weight);
        }

        return buildSummary("monthly", map);
    }

    /* --------------------------------------------------------------------
       YEARLY SUMMARY (Group by month)
    -------------------------------------------------------------------- */
    private ModbusSummaryResponse createYearlySummary(List<Map<String, Object>> rows) {

        Map<String, Double> map = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM");

        for (Map<String, Object> row : rows) {
            LocalDate date = ((java.sql.Date) row.get("record_date")).toLocalDate();
            String monthName = date.format(formatter);
            double weight = ((Number) row.get("total_weight")).doubleValue();

            map.put(monthName, map.getOrDefault(monthName, 0.0) + weight);
        }

        return buildSummary("yearly", map);
    }

    /* --------------------------------------------------------------------
       ALL-YEAR SUMMARY (Group by year)
    -------------------------------------------------------------------- */
    private ModbusSummaryResponse createAllYearSummary(List<Map<String, Object>> rows) {

        Map<String, Double> map = new TreeMap<>();

        for (Map<String, Object> row : rows) {
            LocalDate date = ((java.sql.Date) row.get("record_date")).toLocalDate();
            int year = date.getYear();
            double weight = ((Number) row.get("total_weight")).doubleValue();

            map.put(String.valueOf(year), map.getOrDefault(String.valueOf(year), 0.0) + weight);
        }

        return buildSummary("allYearHistory", map);
    }

    /* --------------------------------------------------------------------
       COMMON SUMMARY BUILDER
    -------------------------------------------------------------------- */
    private ModbusSummaryResponse buildSummary(String type, Map<String, Double> dataMap) {

        List<ModbusSummaryResponse.SummaryItem> items = new ArrayList<>();
        double grandTotal = 0;

        for (Map.Entry<String, Double> entry : dataMap.entrySet()) {
            ModbusSummaryResponse.SummaryItem item = new ModbusSummaryResponse.SummaryItem();
            item.setLabel(entry.getKey());
            item.setTotalWeight(entry.getValue());
            items.add(item);
            grandTotal += entry.getValue();
        }

        ModbusSummaryResponse response = new ModbusSummaryResponse();
        response.setType(type);
        response.setSummary(items);
        response.setGrandTotal(grandTotal);

        return response;
    }
}
