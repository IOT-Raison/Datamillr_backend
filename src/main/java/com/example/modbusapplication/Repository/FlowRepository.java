package com.example.modbusapplication.Repository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.sql.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.modbusapplication.Model.BatchReportDTO;
import com.example.modbusapplication.Model.DailyDataDTO;
import com.example.modbusapplication.Model.ModbusEntityDao;


@Repository
public class FlowRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Creating a table
    public void createFlowControlTable(String deviceId) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS modbus_data_" + deviceId.trim() + "(" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," + 
                "timestamp DATETIME NOT NULL," +
                "status  INT," +
                "flowrate INT," +
                "batch_name  VARCHAR(15)," +
                "set_weight  INT," +
                "total_weight INT)";
        System.out.println("sql === " + sql);
        jdbcTemplate.execute(sql);
        createDailyDataTable(deviceId);
        createBatchDataTable(deviceId);
    }

    //Creating the dailyData Table to store daily total weight
    public void createDailyDataTable(String deviceId) throws SQLException{
        String sql ="CREATE TABLE IF NOT EXISTS modbus_daily_data_"+deviceId.trim()+"("+
        "record_date DATE PRIMARY KEY,"+
        "total_weight INT)";
        System.out.println("sql === "+sql);
        jdbcTemplate.execute(sql);

    }

    //creating the batchdata to store batch wise data
    public void createBatchDataTable(String deviceId) throws SQLException{
        String sql = "CREATE TABLE IF NOT EXISTS modbus_batch_data_"+deviceId.trim()+"("+
        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
        "start_date DATETIME ,"+
        "end_date DATETIME ,"+
        "batch_name VARCHAR(15),"+
        "total_weight INT,"+
        "is_end_of_batch boolean)";
        System.out.println("sql === "+sql);
        jdbcTemplate.execute(sql);
    }


    public void insertDataEntity(ModbusEntityDao modbusEntityDao) {
        String sql = "INSERT INTO modbus_data_" + modbusEntityDao.getDeviceId()
            + " (`timestamp`, status, flowrate, batch_name, set_weight, total_weight) VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
            modbusEntityDao.getTimestamp(),
            modbusEntityDao.getStatus(),
            modbusEntityDao.getFlowrate(),
            modbusEntityDao.getBatchName(),
            modbusEntityDao.getSetWeight(),
            modbusEntityDao.getTotalWeight());
    }


    public void insertDailyData(DailyDataDTO dailyDataDTO){
        String sql = "INSERT INTO modbus_daily_data_"+dailyDataDTO.getDeviceId()+
        "(record_date,total_weight) VALUES (CURDATE(),?)"+
        "ON DUPLICATE KEY UPDATE total_weight= ?";
        int dailyTotalweight = dailyDataDTO.getDailyTotalweight();
        jdbcTemplate.update(sql,
            dailyTotalweight,
            dailyTotalweight);

    }
    public DailyDataDTO getLastDailyData(short deviceId) {
    String sql = "SELECT record_date, total_weight FROM modbus_daily_data_" + deviceId +
            " ORDER BY record_date DESC LIMIT 1";

    try {
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            DailyDataDTO dto = new DailyDataDTO();
            dto.setRecordDate(rs.getDate("record_date").toLocalDate());
            dto.setDailyTotalweight(rs.getInt("total_weight"));
            dto.setDeviceId(deviceId);
            return dto;
        });
    } catch (Exception e) {
        return null; // no rows found
    }
}




   public List<ModbusEntityDao> getDataByDeviceIdAndDateRange(short deviceId, LocalDateTime start, LocalDateTime end) {
        String tableName = "modbus_data_" + deviceId;
        String sql = "SELECT timestamp, status, flowrate, batch_name, set_weight, total_weight FROM " + tableName +
                 " WHERE timestamp BETWEEN ? AND ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
        ModbusEntityDao entity = new ModbusEntityDao();
            entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            entity.setStatus(rs.getInt("status"));
            entity.setFlowrate(rs.getInt("flowrate"));
            entity.setBatchName(rs.getString("batch_name"));
            entity.setSetWeight(rs.getInt("set_weight")); 
            // entity.setPresentWeight(rs.getInt("present_weight"));
            entity.setTotalWeight(rs.getInt("total_weight"));
            entity.setDeviceId(deviceId);
            return entity;
        }, start, end); 
    }
    public List<ModbusEntityDao> getDataByDeviceIdAndDateRangeAndBatchName(
        short deviceId, LocalDateTime start, LocalDateTime end, String batchName) {

    String tableName = "modbus_data_" + deviceId;

    String sql = "SELECT timestamp, status, flowrate, batch_name, set_weight, total_weight " +
                 "FROM " + tableName +
                 " WHERE timestamp BETWEEN ? AND ? AND batch_name = ?";

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
        ModbusEntityDao entity = new ModbusEntityDao();
        entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        entity.setStatus(rs.getInt("status"));
        entity.setFlowrate(rs.getInt("flowrate"));
        entity.setBatchName(rs.getString("batch_name"));
        entity.setSetWeight(rs.getInt("set_weight"));
        entity.setTotalWeight(rs.getInt("total_weight"));
        entity.setDeviceId(deviceId);
        return entity;
    }, start, end, batchName);
}


public List<BatchReportDTO> getDataForBatch(short deviceId, LocalDateTime start, LocalDateTime end) {
    String tableName = "modbus_batch_data_" + deviceId;

    String sql = "SELECT start_date, end_date, batch_name, total_weight, is_end_of_batch " +
                 "FROM " + tableName +
                 " WHERE start_date BETWEEN ? AND ?";

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
        BatchReportDTO entity = new BatchReportDTO();
        entity.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
        entity.setEndDate(rs.getTimestamp("end_date").toLocalDateTime());
        entity.setBatchName(rs.getString("batch_name"));
        entity.setTotalWeight(rs.getInt("total_weight"));
        entity.setEndOfBatch(rs.getBoolean("is_end_of_batch"));
        entity.setDeviceId(deviceId);
        return entity;
    }, start, end);
}


   
        public List<ModbusEntityDao> getLastDataByDeviceId(short deviceId) {
        String tableName = "modbus_data_" + deviceId;
        String sql = "SELECT timestamp, status, flowrate, batch_name, set_weight, total_weight FROM " 
               + tableName + " ORDER BY timestamp DESC Limit 1";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
        ModbusEntityDao entity = new ModbusEntityDao();
            entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            entity.setStatus(rs.getInt("status"));
            entity.setBatchName(rs.getString("batch_name"));
            entity.setSetWeight(rs.getInt("set_weight"));
            entity.setTotalWeight(rs.getInt("total_weight"));
            entity.setDeviceId(deviceId);
        return entity;
        });
    }

public List<ModbusEntityDao> getAllDataByDeviceId(short deviceId) {
    String tableName = "modbus_data_" + deviceId;
    String sql = "SELECT timestamp, status, flowrate, batch_name, set_weight, total_weight " +
                 "FROM " + tableName + " WHERE timestamp >= ? ORDER BY timestamp ASC";

    LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

    return jdbcTemplate.query(
        sql,
        (rs, rowNum) -> {
            ModbusEntityDao entity = new ModbusEntityDao();
            entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            entity.setStatus(rs.getInt("status"));
            entity.setFlowrate(rs.getInt("flowrate"));
            entity.setBatchName(rs.getString("batch_name"));
            entity.setSetWeight(rs.getInt("set_weight"));
            // entity.setPresentWeight(rs.getInt("present_weight"));
            entity.setTotalWeight(rs.getInt("total_weight"));
            entity.setDeviceId(deviceId);
            return entity;
        },
        Timestamp.valueOf(startOfToday) // passing args as varargs
    );
}

    public List<ModbusEntityDao> getDataByDeviceIdForLogin(short deviceId) {
        String tableName = "modbus_data_" + deviceId;
        String sql = "SELECT `timestamp`, status, flowrate, batch_name, set_weight, total_weight " 
        + "FROM " + tableName + " ORDER BY `timestamp` DESC LIMIT 1";

        return jdbcTemplate.query(sql,(rs, rowNum) -> {
            ModbusEntityDao entity = new ModbusEntityDao();
            entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            entity.setStatus(rs.getInt("status"));
            entity.setFlowrate(rs.getInt("flowrate"));
            entity.setBatchName(rs.getString("batch_name"));
            entity.setSetWeight(rs.getInt("set_weight"));
            entity.setTotalWeight(rs.getInt("total_weight"));
            entity.setDeviceId(deviceId);
            return entity;
        });
    }

   

    public List<ModbusEntityDao> fetchDataBtwnDates(String startDate,String endDate,short deviceId){
        String tableName = "modbus_data_" + deviceId;
        String sql = "SELECT timestamp, status, flowrate, batch_name, set_weight, total_weight FROM " 
               + tableName + " WHERE timestamp BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
        ModbusEntityDao entity = new ModbusEntityDao();
        entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        entity.setStatus(rs.getInt("status"));
        entity.setFlowrate(rs.getInt("flowrate"));
        entity.setBatchName(rs.getString("batch_name"));
        entity.setSetWeight(rs.getInt("set_weight"));
        // entity.setPresentWeight(rs.getInt("present_weight"));
        entity.setTotalWeight(rs.getInt("total_weight"));
        entity.setDeviceId(deviceId);
        return entity;
        });        

    }

   
public BatchReportDTO getLastRowData(Short deviceId) {

    String sql = "SELECT * FROM modbus_batch_data_" + deviceId +
                 " ORDER BY end_date DESC LIMIT 1";

    System.out.println("sql === " + sql);

    try {

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            BatchReportDTO dto = new BatchReportDTO();
            dto.setDeviceId(deviceId);
            dto.setBatchName(rs.getString("batch_name"));
            dto.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
            dto.setEndDate(rs.getTimestamp("end_date").toLocalDateTime());
            dto.setTotalWeight(rs.getInt("total_weight"));
            dto.setEndOfBatch(rs.getBoolean("is_end_of_batch"));
            return dto;
        });

    } catch (EmptyResultDataAccessException e) {
        // ✅ Table empty → FIRST READ case
        System.out.println("No batch rows found (table empty)");
        return null;
    }
}

        public BatchReportDTO getLastRowForSchedular(Short deviceId) throws SQLException {
        String sql = "SELECT * FROM modbus_batch_data_" + deviceId + " " +
                        "ORDER BY end_date DESC LIMIT 1";

        System.out.println("sql === " + sql);
        BatchReportDTO batchReportDTO = null;
        
        try {
           

             batchReportDTO = jdbcTemplate.queryForObject(sql, (rs, num) -> {
                BatchReportDTO batchReportDTO2 = new BatchReportDTO();
                batchReportDTO2.setDeviceId(deviceId);
                batchReportDTO2.setEndOfBatch(rs.getBoolean("is_end_of_batch"));
                batchReportDTO2.setBatchName(rs.getString("batch_name"));
                batchReportDTO2.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
                batchReportDTO2.setTotalWeight(rs.getInt("total_weight"));
                batchReportDTO2.setEndDate(rs.getTimestamp("end_date").toLocalDateTime());
                return batchReportDTO2;
            });
        } catch (Exception e) {
            batchReportDTO = new BatchReportDTO();
             batchReportDTO.setEndOfBatch(true);
             System.out.println("res"+batchReportDTO.isEndOfBatch());
        }
        return batchReportDTO;

    }
    
    public void updateBatchData(BatchReportDTO BatchReportDTO, Short deviceId) {
        String sql = "UPDATE modbus_batch_data_" + deviceId + " " +
                "SET end_date = ? ,"+ 
                "total_weight =  ? ," +
                "is_end_of_batch = ? "+
                "ORDER BY end_date DESC LIMIT 1;";

        jdbcTemplate.update(sql,
                Timestamp.valueOf(BatchReportDTO.getEndDate()), // assuming it's a java.sql.Timestamp
                BatchReportDTO.getTotalWeight(),
                BatchReportDTO.isEndOfBatch());
                //modbusEntityDao.getBatchName());
    }
    public void insertBatchData(List<BatchReportDTO> batchReportDTOs, Short deviceId) {
        String sql = "INSERT INTO  modbus_batch_data_" + deviceId +
                "(start_date,end_date,batch_name,total_weight,is_end_of_batch)" +
                " VALUES (?,?,?,?,?)";

        jdbcTemplate.batchUpdate(sql,
                batchReportDTOs,
                batchReportDTOs.size(),
                (pmst, batchReportDTO) -> {
                    pmst.setTimestamp(1, Timestamp.valueOf(batchReportDTO.getStartDate()));
                    pmst.setTimestamp(2, Timestamp.valueOf((batchReportDTO.getEndDate())));
                    pmst.setString(3, batchReportDTO.getBatchName());
                    pmst.setLong(4, batchReportDTO.getTotalWeight());
                    pmst.setBoolean(5, batchReportDTO.isEndOfBatch());
                });

    }
    
//     public boolean isPackingMachine(Short deviceId) {
//     String tableName = "packing_data_" + deviceId;
    
//     try {
//         // Check if the table exists
//         String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?";
//         Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
//         return count != null && count > 0;
//     } catch (Exception e) {
//         // If query fails, assume it's not a packing machine
//         return false;
//     }
// }     

public boolean isPackingMachine(Short deviceId) {

    String sql =
        "SELECT device_type " +
        "FROM device_mapping " +
        "WHERE device_id = ?";

    try {
        String deviceType = jdbcTemplate.queryForObject(
            sql,
            String.class,
            deviceId
        );

        return deviceType != null &&
               deviceType.equalsIgnoreCase("packing");

    } catch (Exception e) {
        System.err.println("Device mapping not found for deviceId " + deviceId);
        return false;
    }
}


public void saveDailyData(DailyDataDTO dailyDataDTO) {

    String sql = "INSERT INTO modbus_daily_data_" + dailyDataDTO.getDeviceId() +
            " (record_date, total_weight) VALUES (?, ?) " +
            "ON DUPLICATE KEY UPDATE total_weight = VALUES(total_weight)";

    jdbcTemplate.update(sql,
            dailyDataDTO.getRecordDate(),          // date from DTO
            dailyDataDTO.getDailyTotalweight());   // added weight
}

public List<DailyDataDTO> fetchDailyData(short deviceId, LocalDate start, LocalDate end) {
    String tableName = "modbus_daily_data_" + deviceId;

    String sql = "SELECT record_date, total_weight FROM " + tableName +
                 " WHERE record_date BETWEEN ? AND ? ORDER BY record_date ASC";

    return jdbcTemplate.query(sql, (rs, row) -> {
        DailyDataDTO dto = new DailyDataDTO();
        dto.setRecordDate(rs.getDate("record_date").toLocalDate());
        dto.setDailyTotalweight(rs.getInt("total_weight"));
        return dto;
    });
}

}
