package com.example.modbusapplication.Repository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.example.modbusapplication.Model.PackingEntityDao;

@Repository
public class PackingRepository {

        @Autowired
    private JdbcTemplate jdbcTemplate;

      public void packing_createTable(String deviceId) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS packing_data_" + deviceId.trim() + " (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "timestamp DATETIME NOT NULL," +
            "batch_id VARCHAR(50)," +
            "bag_weight_set INT," +
            "actual_weight INT," +
            "scale_used VARCHAR(50)," +
            "bag_count INT," +
            "accumulated_weight DOUBLE," +
            "machine_status VARCHAR(10)" +
            ")";
        System.out.println("SQL === " + sql);
        jdbcTemplate.execute(sql);
    }

     public void insertPackingData(PackingEntityDao modbusEntityDao) {
        String sql = "INSERT INTO packing_data_" + modbusEntityDao.getDeviceId()
            + " (`timestamp`, batch_id, bag_weight_set, actual_weight, scale_used, bag_count, accumulated_weight, machine_status) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
            modbusEntityDao.getTimestamp(),
            modbusEntityDao.getBatchId(),
            modbusEntityDao.getBagWeightSet(),
            modbusEntityDao.getActualWeight(),
            modbusEntityDao.getScaleUsed(),
            modbusEntityDao.getBagCount(),
            modbusEntityDao.getAccumulatedWeight(),
            modbusEntityDao.getMachineStatus()
            );
    }

     public List<PackingEntityDao> getPackingDataByDeviceIdAndDateRange(short deviceId, LocalDateTime start, LocalDateTime end) {
        String tableName = "packing_data_" + deviceId;
        String sql = "SELECT timestamp, batch_id, bag_weight_set, actual_weight, scale_used, bag_count, accumulated_weight, machine_status "
               + "FROM " + tableName + " WHERE timestamp BETWEEN ? AND ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
        PackingEntityDao entity = new PackingEntityDao();
            entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            entity.setBatchId(rs.getString("batch_id"));
            entity.setBagWeightSet(rs.getInt("bag_weight_set"));
            entity.setActualWeight(rs.getInt("actual_weight"));
            entity.setScaleUsed(rs.getString("scale_used"));
            entity.setBagCount(rs.getInt("bag_count"));
            entity.setAccumulatedWeight(rs.getDouble("accumulated_weight"));
            entity.setMachineStatus(rs.getString("machine_status"));
            entity.setDeviceId(deviceId);
        return entity;
        }, start, end);
    }
    public List<PackingEntityDao> getPackingDataByDeviceIdForLogin(short deviceId) {
    String tableName = "packing_data_" + deviceId;
    String sql = "SELECT `timestamp`, batch_id, bag_weight_set, actual_weight, scale_used, " +
                 "bag_count, accumulated_weight, machine_status " +
                 "FROM " + tableName + " ORDER BY `timestamp` DESC LIMIT 1";

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
        PackingEntityDao dao = new PackingEntityDao();
        dao.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        dao.setBatchId(rs.getString("batch_id"));
        dao.setBagWeightSet(rs.getInt("bag_weight_set"));
        dao.setActualWeight(rs.getInt("actual_weight"));
        dao.setScaleUsed(rs.getString("scale_used"));
        dao.setBagCount(rs.getInt("bag_count"));
        dao.setAccumulatedWeight(rs.getDouble("accumulated_weight"));
        dao.setMachineStatus(rs.getString("machine_status"));
        return dao;
    });
}


     public List<PackingEntityDao> getAllPackingDataByDeviceId(short deviceId) {
        String tableName = "packing_data_" + deviceId;
        String sql = "SELECT timestamp, batch_id, bag_weight_set, actual_weight, scale_used, bag_count, accumulated_weight, machine_status " 
               + "FROM " + tableName + " ORDER BY timestamp DESC LIMIT 1";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
        PackingEntityDao entity = new PackingEntityDao();
            entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            entity.setBatchId(rs.getString("batch_id"));
            entity.setBagWeightSet(rs.getInt("bag_weight_set"));
            entity.setActualWeight(rs.getInt("actual_weight"));
            entity.setScaleUsed(rs.getString("scale_used"));
            entity.setBagCount(rs.getInt("bag_count"));
            entity.setAccumulatedWeight(rs.getDouble("accumulated_weight"));
            entity.setMachineStatus(rs.getString("machine_status"));
            entity.setDeviceId(deviceId);
        return entity;
        });
    }

     public List<PackingEntityDao> fetchPackingDataBtwnDates(String startDate, String endDate, Short deviceId) {
    String tableName = "packing_data_" + deviceId;
    String sql = "SELECT timestamp, batch_id, bag_weight_set, actual_weight, scale_used, bag_count, accumulated_weight, machine_status "
               + "FROM " + tableName + " WHERE timestamp BETWEEN ? AND ?";

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
        PackingEntityDao entity = new PackingEntityDao();
        entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        entity.setBatchId(rs.getString("batch_id"));
        entity.setBagWeightSet(rs.getInt("bag_weight_set"));
        entity.setActualWeight(rs.getInt("actual_weight"));
        entity.setScaleUsed(rs.getString("scale_used"));
        entity.setBagCount(rs.getInt("bag_count"));
        entity.setAccumulatedWeight(rs.getDouble("accumulated_weight"));
        entity.setMachineStatus(rs.getString("machine_status"));
        entity.setDeviceId(deviceId);
        return entity;
    }, startDate, endDate);
}
    public boolean isPackingMachine(Short deviceId) {
    String tableName = "packing_data_" + deviceId;
    
    try {
        // Check if the table exists
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
        return count != null && count > 0;
    } catch (Exception e) {
        // If query fails, assume it's not a packing machine
        return false;
    }
}  
    
public Map<Integer, Integer> getBagCountByWeight(Short deviceId, String batchId) {

    String tableName = "packing_data_" + deviceId;

    String sql =
        "SELECT bag_weight_set, COUNT(*) AS bag_count " +
        "FROM " + tableName + " " +
        "WHERE batch_id = ? " +
        "GROUP BY bag_weight_set " +
        "ORDER BY bag_weight_set";

    return jdbcTemplate.query(sql, rs -> {
        Map<Integer, Integer> result = new LinkedHashMap<>();
        while (rs.next()) {
            result.put(
                rs.getInt("bag_weight_set"),
                rs.getInt("bag_count")
            );
        }
        return result;
    }, batchId);
}

public LocalDateTime getBatchStartTime(Short deviceId, String batchId) {

    String tableName = "packing_data_" + deviceId;

    String sql =
        "SELECT MIN(timestamp) FROM " + tableName + " WHERE batch_id = ?";

    return jdbcTemplate.queryForObject(sql, LocalDateTime.class, batchId);
}

}
