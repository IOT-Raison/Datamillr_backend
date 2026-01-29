package com.example.modbusapplication.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.modbusapplication.Model.BatchReportDTO;
import com.example.modbusapplication.Model.DailyDataDTO;
import com.example.modbusapplication.Model.ModbusEntityDao;
import com.example.modbusapplication.Model.ModbusRecord;
import com.example.modbusapplication.Model.PackingEntityDao;
import com.example.modbusapplication.Model.RawRecordDTO;
import com.example.modbusapplication.Repository.FlowRepository;
import com.example.modbusapplication.Repository.PackingRepository;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class ModbusRecordService {

    @Autowired
    FlowRepository modbusRecordRepository;
    @Autowired
    PackingRepository packingRepository;
    @Autowired
    DailyDataDTO dailyDataDTO;
    @Autowired
    ModbusNotificationService modbusNotificationService;

//  public int handleModbusData(List<RawRecordDTO> rawRecordDTOList) {

//     int successCount = 0;
//     List<BatchReportDTO> batchReports = new ArrayList<>();
//     boolean lastRowFlag = false;
//     ModbusEntityDao modbusEntityDao = null;
//     String lastRowBatchName = null;

//     try {
//         int i = 0;
//         BatchReportDTO batchReportDTO = new BatchReportDTO();
//         for (RawRecordDTO dto : rawRecordDTOList) {
//             i++;
//             modbusEntityDao = decodeAndStore(dto.getEncByteString(), dailyDataDTO);

//             if (modbusEntityDao == null || !modbusEntityDao.isSuccess()) {
//                 continue;
//             }
//             successCount++;
//             if (i == 1) {
//                 batchReportDTO = modbusRecordRepository.getLastRowData(
//                         modbusEntityDao.getBatchName(), modbusEntityDao.getDeviceId());
//                 if (batchReportDTO == null || batchReportDTO.getBatchName() == null) {
//                     BatchReportDTO newBatch = new BatchReportDTO();
//                     newBatch.setStartDate(modbusEntityDao.getTimestamp());
//                     newBatch.setEndDate(modbusEntityDao.getTimestamp());
//                     newBatch.setTotalWeight(modbusEntityDao.getTotalWeight());
//                     newBatch.setBatchName(modbusEntityDao.getBatchName());
//                     newBatch.setEndOfBatch(false);
//                     batchReports.add(newBatch);
//                     batchReportDTO = newBatch;
//                 } else {
//                     lastRowFlag = batchReportDTO.isEndOfBatch();
//                     lastRowBatchName = batchReportDTO.getBatchName();

//                     if (!lastRowFlag) {
//                         if (modbusEntityDao.getTotalWeight() == 0) {
//                             batchReportDTO.setEndOfBatch(true);
//                             batchReportDTO.setEndDate(modbusEntityDao.getTimestamp());
//                         }
//                         batchReports.add(batchReportDTO);
//                     }
//                 }
//             }
//             handleWeighingBatchData(modbusEntityDao, batchReports, batchReportDTO);
//             if (batchReports.size() == 0) {
//                 return successCount;
//             }
//             batchReportDTO = batchReports.get(batchReports.size() - 1);
//         }
//         if (modbusEntityDao == null) return successCount;
//         Short deviceId = modbusEntityDao.getDeviceId();
//         for (BatchReportDTO br : batchReports) {
//             if (br.isEndOfBatch()) {
//                 DailyDataDTO lastDaily = modbusRecordRepository.getLastDailyData(deviceId);
//                 LocalDate today = modbusEntityDao.getTimestamp().toLocalDate();
//                 LocalDate lastDate = lastDaily != null ? lastDaily.getRecordDate() : null;
//                 boolean newDate = lastDaily == null || !today.equals(lastDate);
//                 dailyDataDTO.setRecordDate(today);
//                 dailyDataDTO.setDeviceId(deviceId);
//                 if (newDate) {
//                     dailyDataDTO.setDailyTotalweight(br.getTotalWeight());
//                 } else {
//                     dailyDataDTO.setDailyTotalweight(dailyDataDTO.getDailyTotalweight() + br.getTotalWeight());
//                 } 

//                 modbusRecordRepository.saveDailyData(dailyDataDTO);
//                 modbusNotificationService.sendBtchReport(br);
//             }
//         }

//         if (!batchReportDTO.getBatchName().equals(modbusEntityDao.getBatchName())) {
//             batchReportDTO = new BatchReportDTO();
//             batchReportDTO.setBatchName(modbusEntityDao.getBatchName());
//             batchReportDTO.setDeviceId(modbusEntityDao.getDeviceId());
//             batchReportDTO.setStartDate(modbusEntityDao.getTimestamp());
//             batchReportDTO.setEndDate(modbusEntityDao.getTimestamp());
//             batchReportDTO.setTotalWeight(modbusEntityDao.getTotalWeight());
//             batchReportDTO.setEndOfBatch(modbusEntityDao.getTotalWeight() == 0);
//             batchReports.add(batchReportDTO);
//         }
//         if (batchReports.isEmpty()) {
//             return successCount;
//         }
//         if (batchReports.get(0).getBatchName().equals(lastRowBatchName) && !lastRowFlag) {
//             modbusRecordRepository.updateBatchData(batchReports.get(0), deviceId);
//             batchReports.remove(0);
//         }
//         if (!batchReports.isEmpty()) {
//             modbusRecordRepository.insertBatchData(batchReports, deviceId);
//         }
//     } catch (Exception e) {
//         System.out.println("ERROR in handleModbusData: " + e.getMessage());
//         e.printStackTrace();
//     }
//     return successCount;
// }

// public void handleWeighingBatchData(ModbusEntityDao modbusEntityDao,
//                                     List<BatchReportDTO> batchReports,
//                                     BatchReportDTO batchReportDTO) throws SQLException {
//     String batchName = modbusEntityDao.getBatchName();
//     int weight = modbusEntityDao.getTotalWeight();
//     if (weight == 0 && batchReportDTO.isEndOfBatch()) {
//         return;
//     }

//     boolean batchnameChanged = !batchName.equals(batchReportDTO.getBatchName());
//     boolean samebatchname = batchReportDTO.isEndOfBatch();
//     boolean shouldEnd = batchnameChanged || weight == 0;
//     if(shouldEnd){
//         BatchReportDTO newBatch = new BatchReportDTO();
//         newBatch.setEndOfBatch(false);
//         newBatch.setStartDate(modbusEntityDao.getTimestamp());
//         newBatch.setEndDate(modbusEntityDao.getTimestamp());
//         newBatch.setTotalWeight(weight);
//         newBatch.setBatchName(batchName);
//         batchReports.add(newBatch);
//     }
//     else if (weight != 0 && samebatchname) {
//         BatchReportDTO sameBatchName = new BatchReportDTO();
//         sameBatchName.setEndOfBatch(false);
//         sameBatchName.setStartDate(modbusEntityDao.getTimestamp());
//         sameBatchName.setEndDate(modbusEntityDao.getTimestamp());
//         sameBatchName.setTotalWeight(weight);
//         sameBatchName.setBatchName(batchName);
//         batchReports.add(sameBatchName);
//     } 
//     else {
//         batchReportDTO.setEndDate(modbusEntityDao.getTimestamp());
//         batchReportDTO.setTotalWeight(weight);
//     }
// }

public int handleModbusData(List<RawRecordDTO> rawRecordDTOList) {

    System.out.println("---- handleModbusData START ----");
    int successCount = 0;

    try {
        for (RawRecordDTO dto : rawRecordDTOList) {

            ModbusEntityDao dao = decodeAndStore(dto.getEncByteString(), dailyDataDTO);

            if (dao == null || !dao.isSuccess()) {
                continue;
            }

            successCount++;

            Short deviceId = dao.getDeviceId();
            //boolean startBatch = dao.isStartOfBatch();
            boolean endBatch = dao.isEndOfBatch();

            System.out.println(
                "Device=" + deviceId +
                " Weight=" + dao.getTotalWeight() +
              // " Start=" + startBatch +
                " End=" + endBatch
            );

            // 🔹 Get last batch row (by time, not by batch name)
            BatchReportDTO lastRow =
                    modbusRecordRepository.getLastRowData(deviceId);

/* ================= FIRST READ / AFTER RESTART ================= */
if (lastRow == null) {

    if (dao.getTotalWeight() > 0) {
        System.out.println("FIRST READ → weight > 0 → ASSUME RUNNING BATCH");

        BatchReportDTO newBatch = createNewBatch(dao, false);
        modbusRecordRepository.insertBatchData(
                List.of(newBatch), deviceId
        );
    } else {
        System.out.println("FIRST READ → weight = 0 → waiting for batch start");
    }

    continue;
}


            /* ================= BATCH END ================= */
            if (endBatch && !lastRow.isEndOfBatch()) {

                System.out.println("Batch END detected");

                lastRow.setEndDate(dao.getTimestamp());
               // lastRow.setBatchName(dao.getBatchName());
                lastRow.setEndOfBatch(true);

                modbusRecordRepository.updateBatchData(lastRow, deviceId);

                // ✅ add daily total ONLY once
                updateDailyTotal(lastRow, dao);

                modbusNotificationService.sendBtchReport(lastRow);
                continue;
            }

            

            /* ================= BATCH START ================= */
            if (lastRow.isEndOfBatch() && dao.getTotalWeight() > 0) {

                System.out.println("New Batch START (DB boundary detected)");

                BatchReportDTO newBatch = createNewBatch(dao, false);
                modbusRecordRepository.insertBatchData(List.of(newBatch), deviceId);
                continue;
            }

            /* ================= RUNNING BATCH ================= */
            if (!lastRow.isEndOfBatch()) {

                System.out.println("Updating running batch");

                lastRow.setEndDate(dao.getTimestamp());
                //lastRow.setBatchName(dao.getBatchName());
                lastRow.setTotalWeight(dao.getTotalWeight());

                modbusRecordRepository.updateBatchData(lastRow, deviceId);
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    System.out.println("---- handleModbusData END ----");
    return successCount;
}

private BatchReportDTO createNewBatch(ModbusEntityDao dao, boolean end) {
    BatchReportDTO br = new BatchReportDTO();
    br.setBatchName(dao.getBatchName());
    br.setStartDate(dao.getTimestamp());
    br.setEndDate(dao.getTimestamp());
    br.setTotalWeight(dao.getTotalWeight());
    br.setEndOfBatch(end);
    return br;
}
private void updateDailyTotal(BatchReportDTO batch, ModbusEntityDao dao) {

    Short deviceId = dao.getDeviceId();
    LocalDate today = dao.getTimestamp().toLocalDate();

    // 🔹 Fetch last daily record
    DailyDataDTO lastDaily = modbusRecordRepository.getLastDailyData(deviceId);

    DailyDataDTO dailyData = new DailyDataDTO();
    dailyData.setDeviceId(deviceId);
    dailyData.setRecordDate(today);

    if (lastDaily == null || !today.equals(lastDaily.getRecordDate())) {
        // ✅ First batch of the day
        dailyData.setDailyTotalweight(batch.getTotalWeight());
    } else {
        // ✅ Add to existing daily total
        dailyData.setDailyTotalweight(
            lastDaily.getDailyTotalweight() + batch.getTotalWeight()
        );
    }

    modbusRecordRepository.saveDailyData(dailyData);

    System.out.println(
        "Daily total updated → Date=" + today +
        " Weight=" + dailyData.getDailyTotalweight()
    );
}



//  public int handleModbusData(List<RawRecordDTO> rawRecordDTOList) {

//     System.out.println("---- handleModbusData START ----");
//     System.out.println("Raw DTO Count: " + rawRecordDTOList.size());

//     int successCount = 0;
//     List<BatchReportDTO> batchReports = new ArrayList<>();
//     boolean lastRowFlag = false;
//     ModbusEntityDao modbusEntityDao = null;
//     String lastRowBatchName = null;

//     try {
//         int i = 0;
//         BatchReportDTO batchReportDTO = new BatchReportDTO();

//         for (RawRecordDTO dto : rawRecordDTOList) {
//             i++;
//             System.out.println("\n--- LOOP " + i + " ---");
//             System.out.println("Raw Encoded String: " + dto.getEncByteString());

//             modbusEntityDao = decodeAndStore(dto.getEncByteString(), dailyDataDTO);
//             System.out.println("Decoded DAO: " + modbusEntityDao);

//             if (modbusEntityDao == null || !modbusEntityDao.isSuccess()) {
//                 System.out.println("Skipping - decode failed or DAO success=false");
//                 continue;
//             }

//             successCount++;
//             System.out.println("Success Count: " + successCount);

//             // FIRST ROW LOGIC
//             if (i == 1) {
//                 String batchName = modbusEntityDao.getBatchName();
//                 int weight = modbusEntityDao.getTotalWeight();
//                 System.out.println("Fetching LastRowData for batch=" + modbusEntityDao.getBatchName()
//                         + ", device=" + modbusEntityDao.getDeviceId());

//                 batchReportDTO = modbusRecordRepository.getLastRowData(
//                         modbusEntityDao.getBatchName(), modbusEntityDao.getDeviceId());

//                 if (batchReportDTO == null || batchReportDTO.getBatchName() == null) {
//                     System.out.println("No last record found -> Creating new batch row");

//                     batchReportDTO = new BatchReportDTO();
//                     batchReportDTO.setStartDate(modbusEntityDao.getTimestamp());
//                     batchReportDTO.setEndDate(modbusEntityDao.getTimestamp());
//                     batchReportDTO.setTotalWeight(modbusEntityDao.getTotalWeight());
//                     batchReportDTO.setBatchName(batchName);
//                     batchReportDTO.setEndOfBatch(modbusEntityDao.isEndOfBatch());

//                     batchReports.add(batchReportDTO);
//                 }
    
//                     lastRowFlag = batchReportDTO.isEndOfBatch();
//                     lastRowBatchName = batchReportDTO.getBatchName();

//                     System.out.println("Found Existing Batch: " + lastRowBatchName + " EndOfBatch=" + lastRowFlag);
//                 if (!modbusEntityDao.isEndOfBatch()) {
//                     System.out.println("Updating existing batch row");
//                     batchReportDTO.setEndDate(modbusEntityDao.getTimestamp());
//                     batchReportDTO.setTotalWeight(weight);
//                 }

//                 //boolean batchNameChanged = !batchName.equals(batchReportDTO.getBatchName());
//                 //boolean previousEnded = batchReportDTO.isEndOfBatch();
//                 boolean endofbatch = modbusEntityDao.isEndOfBatch();

//                 if (endofbatch) {
//                     System.out.println("Creating NEW batch row");

//                     BatchReportDTO newBatch = new BatchReportDTO();
//                     newBatch.setStartDate(modbusEntityDao.getTimestamp());
//                     newBatch.setEndDate(modbusEntityDao.getTimestamp());
//                     newBatch.setTotalWeight(modbusEntityDao.getTotalWeight());
//                     newBatch.setEndOfBatch(modbusEntityDao.isEndOfBatch());
//                     newBatch.setBatchName(modbusEntityDao.getBatchName());

//                     batchReports.add(newBatch);
//                     batchReportDTO = newBatch;
//                 }

//                 if (!lastRowFlag) {
//                     if (modbusEntityDao.isEndOfBatch()) {
//                         System.out.println("Closing existing batch");
//                         batchReportDTO.setEndOfBatch(true);
//                         batchReportDTO.setEndDate(modbusEntityDao.getTimestamp());
//                         batchReportDTO.setTotalWeight(modbusEntityDao.getTotalWeight());
//                     }
//                     batchReports.add(batchReportDTO);
//                 }

//                 Short deviceId = modbusEntityDao.getDeviceId();
//                 System.out.println("Saving final Daily Totals for device " + deviceId);

//                 for (BatchReportDTO br : batchReports) {
//                 System.out.println("Checking Batch: " + br.getBatchName() + " EOB=" + br.isEndOfBatch());
//                     if (br.isEndOfBatch()) {
//                         DailyDataDTO lastDaily = modbusRecordRepository.getLastDailyData(deviceId);
//                         LocalDate today = modbusEntityDao.getTimestamp().toLocalDate();
//                         LocalDate lastDate = lastDaily != null ? lastDaily.getRecordDate() : null;
//                         boolean newDate = lastDaily == null || !today.equals(lastDate);
//                         dailyDataDTO.setRecordDate(today);
//                         dailyDataDTO.setDeviceId(deviceId);
//                     if (newDate) {
//                         System.out.println("New date: " + br.getTotalWeight());
//                         dailyDataDTO.setDailyTotalweight(br.getTotalWeight());
//                     } else {
//                         System.out.println("Old date: " + dailyDataDTO.getDailyTotalweight() + " + " + br.getTotalWeight());
//                         dailyDataDTO.setDailyTotalweight(dailyDataDTO.getDailyTotalweight() + br.getTotalWeight());
//                     }

//                     modbusRecordRepository.saveDailyData(dailyDataDTO);
//                     modbusNotificationService.sendBtchReport(br);
//                     }
//                 }

//             }
               
//         }

//             if (batchReports.size() == 0) return successCount;
//             batchReportDTO = batchReports.get(batchReports.size() - 1);
        

//         if (modbusEntityDao == null) return successCount;
  

//        Short deviceId = modbusEntityDao.getDeviceId();
//         if (batchReports.get(0).getBatchName().equals(lastRowBatchName) && !lastRowFlag) {
//             System.out.println("Updating previous row");
//             modbusRecordRepository.updateBatchData(batchReports.get(0), deviceId);
//             batchReports.remove(0);
//         }

//         if (!batchReports.isEmpty()) {
//             System.out.println("Inserting batchReports size: " + batchReports.size());
//             modbusRecordRepository.insertBatchData(batchReports, deviceId);
//         }

//     } catch (Exception e) {
//         System.out.println("ERROR: " + e.getMessage());
//         e.printStackTrace();
//     }

//     System.out.println("---- handleModbusData END ----");
//     return successCount;
// }

public ModbusEntityDao decodeAndStore(String base64Data, DailyDataDTO dailyDataDTO) {

    try {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data.trim());

        try (ByteArrayInputStream bais = new ByteArrayInputStream(decodedBytes);
                ObjectInputStream ois = new ObjectInputStream(bais)) {

            Object obj = ois.readObject();
            if (!(obj instanceof List<?>)) {
                System.err.println("Decoded object is not a list.");
                ModbusEntityDao modbusEntityDao = new ModbusEntityDao();
                modbusEntityDao.setSuccess(false);
                return modbusEntityDao;
            }

            @SuppressWarnings("unchecked")
            List<ModbusRecord> records = (List<ModbusRecord>) obj;
            System.out.println("📦 Decoded ModbusRecords:");
            records.forEach(r -> System.out.println("📝 " + r));
            // Extract fields
            String batchName = null;
            int status = 0;
            int flowrate = 0;
            int setWeight = 0;
            int totalWeight = 0;
            LocalDateTime timestamp = null;
            Short deviceId = null;
            boolean endOfBatch = false;
         //   boolean startOfBatch = false;


            for (ModbusRecord record : records) {
                switch (record.getName()) {
                    case "status":
                        status = Integer.parseInt(record.getRegisters());
                        break;
                    case "flowrate":
                        flowrate = Integer.parseInt(record.getRegisters());
                        break;
                    case "batchName":
                        batchName = record.getRegisters();
                        break;
                    case "setWeight":
                        setWeight = Integer.parseInt(record.getRegisters());
                        break;
                    case "totalWeight":
                        totalWeight = Integer.parseInt(record.getRegisters());
                        break;
                    case "datetime":
                        timestamp = LocalDateTime.parse(
                                record.getRegisters().substring(0, 19),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        break;
                    case "deviceId":
                        String rawDeviceId = record.getRegisters();
                    if (rawDeviceId != null && !rawDeviceId.trim().isEmpty()) {
                        try {
                            deviceId = Short.parseShort(rawDeviceId.trim());
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid deviceId value: " + rawDeviceId);
                        }
                    } else {
                        System.err.println("Empty or null deviceId value");
                    }
                    break;
                    case "endOfBatch":
                        endOfBatch = Boolean.parseBoolean(record.getRegisters());
                    break;
                }
            }
            // // Skip fake reads
            // if (status == 0 && totalWeight == 0) {
            //     System.out.println("Skipping fake read: status=0 & totalWeight=0 for deviceId: " + deviceId);
            //     ModbusEntityDao dao = new ModbusEntityDao();
            //     dao.setSuccess(false);
            //     return dao;
            // }
            
            // // Skip if batchName empty
            // if (batchName == null || batchName.trim().isEmpty()) {
            //     System.out.println("Skipping because batchName is empty");
            //     ModbusEntityDao dao = new ModbusEntityDao();
            //     dao.setSuccess(false);
            //     return dao;
            // }

            // // // Validate required fields
            // if (deviceId == null) {
            //     System.err.println("Missing required field: deviceId");
            //     ModbusEntityDao dao = new ModbusEntityDao();
            //     dao.setSuccess(false);
            //     return dao;
            // }

            // if (timestamp == null) {
            //     System.err.println("Missing required field: timestamp");
            //     ModbusEntityDao dao = new ModbusEntityDao();
            //     dao.setSuccess(false);
            //     return dao;
            // }

            ModbusEntityDao modbusEntityDao = new ModbusEntityDao(timestamp, status,
                    flowrate, batchName, setWeight, totalWeight, deviceId);
                    modbusEntityDao.setEndOfBatch(endOfBatch); 

//             // Skip if batchName empty
// if (endOfBatch) {
//     System.out.println("🔴 Batch END detected for batch: " + batchName);
//     modbusEntityDao.setBatchName(batchName + "(end)");
// }

            try {
                modbusRecordRepository.insertDataEntity(modbusEntityDao);
                System.out.println("Record saved to database: " + modbusEntityDao);
                modbusEntityDao.setSuccess(true);
                return modbusEntityDao;
            } catch (Exception e) {
                System.err.println("Exception on insertDataEntity :: DeviceID ::" + deviceId
                            + " :: " + e.getMessage());
                // ModbusEntityDao modbusEntityDao = new ModbusEntityDao();
                modbusEntityDao.setSuccess(false);
                return modbusEntityDao;
            }

        }

    } catch (Exception e) {
        System.err.println("Error decoding/storing record: " + e.getMessage());
        ModbusEntityDao modbusEntityDao = new ModbusEntityDao();
        modbusEntityDao.setSuccess(false);
        return modbusEntityDao;
    }
}
   
public boolean decodeAndStorePacking(String base64Data) {
    try {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data.trim());
        try (ByteArrayInputStream bais = new ByteArrayInputStream(decodedBytes);
                ObjectInputStream ois = new ObjectInputStream(bais)) {

            Object obj = ois.readObject();
            if (!(obj instanceof List<?>)) {
                System.err.println("Decoded object is not a list.");
                return false;
            }

            @SuppressWarnings("unchecked")
            List<ModbusRecord> records = (List<ModbusRecord>) obj;
            System.out.println("Decoded Packing ModbusRecords:");
            records.forEach(r -> System.out.println(" " + r));
            // Extract fields
            LocalDateTime timestamp = null;
            String batchId = null;
            int bagWeightSet = 0;
            int actualWeight = 0;
            String scaleUsed = null;
            int bagCount = 0;
            int accumulatedWeight = 0;
            String machineStatus = null;
            Short deviceId = null;
            boolean batchEnd = false;
            for (ModbusRecord record : records) {
                switch (record.getName()) {
                    case "timestamp":
                        timestamp = LocalDateTime.parse(record.getRegisters().substring(0, 19),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        break;
                    case "batchId":
                        batchId = record.getRegisters();
                        break;
                    case "bagWeightSet":
                        bagWeightSet = Integer.parseInt(record.getRegisters());
                        break;
                    case "actualWeight":
                        actualWeight = Integer.parseInt(record.getRegisters());
                        break;
                    case "scaleUsed":
                        scaleUsed = record.getRegisters();
                        break;
                    case "bagCount":
                        bagCount = Integer.parseInt(record.getRegisters());
                        break;
                    case "accumulatedWeight":
                        accumulatedWeight = Integer.parseInt(record.getRegisters());
                        break;
                    case "machineStatus":
                        machineStatus = record.getRegisters();
                        break;
                    case "batchEnd":
                        batchEnd = Boolean.parseBoolean(record.getRegisters());
                        break;
                    case "deviceId":
                        String rawDeviceId = record.getRegisters();
                    if (rawDeviceId != null && !rawDeviceId.trim().isEmpty()) {
                        try {
                            deviceId = Short.parseShort(rawDeviceId.trim());
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid deviceId value: " + rawDeviceId);
                        }
                    } else {
                        System.err.println("Empty or null deviceId value");
                    }
                    break;
                }
            }

            if (deviceId == null || batchId == null) {
                System.err.println("Missing required fields (deviceId or batchId)");
                return false;
            }

            PackingEntityDao modbusEntityDao = new PackingEntityDao(timestamp, batchId, bagWeightSet, actualWeight, scaleUsed,
                    bagCount, accumulatedWeight, machineStatus, deviceId);

            packingRepository.insertPackingData(modbusEntityDao);
            System.out.println("Packing record saved: " + modbusEntityDao);
           if (batchEnd) {

    Map<Integer, Integer> bagCounts =
            packingRepository.getBagCountByWeight(deviceId, batchId);

    LocalDateTime batchStartTime =
            packingRepository.getBatchStartTime(deviceId, batchId);

    BatchReportDTO report = new BatchReportDTO();
    report.setDeviceId(deviceId);
    report.setBatchName(batchId);
    report.setStartDate(batchStartTime);
    report.setEndDate(timestamp);
    report.setTotalWeight(accumulatedWeight);

    modbusNotificationService.sendPackingBatchCompleted(
            report,
            bagCounts
    );
}

            return true;

        }

    } catch (Exception e) {
        System.err.println("Error decoding/storing packing record: " + e.getMessage());
        return false;
    }
}

}
