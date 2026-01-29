package com.example.modbusapplication.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.modbusapplication.Model.BatchReportDTO;
import com.example.modbusapplication.Model.ExportRequestDTO;
import com.example.modbusapplication.Model.PackingEntityDao;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import com.example.modbusapplication.Model.ModbusDataRequestDTO;
import com.example.modbusapplication.Repository.FlowRepository;

@Service
public class ExcelExportService {

    @Autowired
    FlowRepository modbusRecordRepository;
    @Autowired
    FlowReportService flowReportService;
    @Autowired
    PackingReportService packingReportService;

    // For Flow Control Report Export
    public List<BatchReportDTO> getFlowReportData(ExportRequestDTO requestDTO) {

        ModbusDataRequestDTO dao = new ModbusDataRequestDTO();
        dao.setDeviceId(requestDTO.getDeviceId());
        dao.setStartDate(requestDTO.getStartDate());
        dao.setEndDate(requestDTO.getEndDate());

        System.out.println("Flow Control Batch Report");
        return flowReportService.flowReport(dao);
    }

    // For Packing Report Export
    public List<PackingEntityDao> getPackingReportData(ExportRequestDTO requestDTO) {

        ModbusDataRequestDTO dao = new ModbusDataRequestDTO();
        dao.setDeviceId(requestDTO.getDeviceId());
        dao.setStartDate(requestDTO.getStartDate());
        dao.setEndDate(requestDTO.getEndDate());

        System.out.println("Packing Machine Batch Report");
        return packingReportService.packingReport(dao);
    }

public InputStream exportFlowToExcel(List<BatchReportDTO> records, String companyName,
                                    String deviceName, String startDate, String endDate) throws Exception {

    if (records == null || records.isEmpty()) {
        throw new Exception("No Flow Control report data available for the selected date.");
    }

    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Batch Report");

    // Title Style
    CellStyle titleStyle = workbook.createCellStyle();
    Font titleFont = workbook.createFont();
    titleFont.setBold(true);
    titleFont.setFontHeightInPoints((short) 14);
    titleStyle.setFont(titleFont);
    titleStyle.setAlignment(HorizontalAlignment.CENTER);

    // Header style
    CellStyle headerStyle = workbook.createCellStyle();
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setColor(IndexedColors.WHITE.getIndex());
    headerStyle.setFont(headerFont);
    headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    headerStyle.setAlignment(HorizontalAlignment.CENTER);
    headerStyle.setBorderBottom(BorderStyle.THIN);
    headerStyle.setBorderTop(BorderStyle.THIN);
    headerStyle.setBorderLeft(BorderStyle.THIN);
    headerStyle.setBorderRight(BorderStyle.THIN);

    // Data cell style
    CellStyle cellStyle = workbook.createCellStyle();
    cellStyle.setAlignment(HorizontalAlignment.CENTER);
    cellStyle.setBorderBottom(BorderStyle.THIN);
    cellStyle.setBorderTop(BorderStyle.THIN);
    cellStyle.setBorderLeft(BorderStyle.THIN);
    cellStyle.setBorderRight(BorderStyle.THIN);

    // Total row style
    CellStyle totalStyle = workbook.createCellStyle();
    Font totalFont = workbook.createFont();
    totalFont.setBold(true);
    totalStyle.setFont(totalFont);
    totalStyle.setAlignment(HorizontalAlignment.CENTER);
    totalStyle.setBorderBottom(BorderStyle.THIN);
    totalStyle.setBorderTop(BorderStyle.THIN);
    totalStyle.setBorderLeft(BorderStyle.THIN);
    totalStyle.setBorderRight(BorderStyle.THIN);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    int rowNum = 0;

    // Company Row
    Row compRow = sheet.createRow(rowNum++);
    Cell compCell = compRow.createCell(3);
    compCell.setCellValue(companyName);
    compCell.setCellStyle(titleStyle);
    sheet.addMergedRegion(new CellRangeAddress(0, 0, 3, 7));

    // Device row
    Row deviceRow = sheet.createRow(rowNum++);
    Cell deviceCell = deviceRow.createCell(3);
    deviceCell.setCellValue("Device Name: " + deviceName);
    deviceCell.setCellStyle(titleStyle);
    sheet.addMergedRegion(new CellRangeAddress(1, 1, 3, 7));

    // Date range row
    Row dateRow = sheet.createRow(rowNum++);
    Cell dateCell = dateRow.createCell(3);
    dateCell.setCellValue("From: " + startDate + "   To: " + endDate);
    dateCell.setCellStyle(titleStyle);
    sheet.addMergedRegion(new CellRangeAddress(2, 2, 3, 7));

    rowNum++;

    // Table header
    Row header = sheet.createRow(rowNum++);
    String[] headers = {"S.No", "Batch Name", "Start Time", "End Time", "Total Weight"};

    for (int i = 0; i < headers.length; i++) {
        Cell cell = header.createCell(3+i);
        cell.setCellValue(headers[i]);
        cell.setCellStyle(headerStyle);
    }

    // Data rows
    int serial = 1;
    double totalWeightSum = 0;
    for (BatchReportDTO record : records) {

        if (!record.isEndOfBatch()) continue;

        Row row = sheet.createRow(rowNum++);

        row.createCell(3).setCellValue(serial++);
        row.createCell(4).setCellValue(record.getBatchName());
        row.createCell(5).setCellValue(record.getStartDate().format(formatter));
        row.createCell(6).setCellValue(record.getEndDate().format(formatter));
        row.createCell(7).setCellValue(record.getTotalWeight());

        totalWeightSum += record.getTotalWeight();

        // Apply style
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.getCell(3+i);
            if (cell == null) cell = row.createCell(3+i);
            cell.setCellStyle(cellStyle);
        }
    }

// ---------- TOTAL ROW ----------
Row totalRow = sheet.createRow(rowNum++);

// Create and style all required cells for the total row
for (int i = 0; i < headers.length; i++) {
    Cell cell = totalRow.getCell(3+i);
    if (cell == null) {
        cell = totalRow.createCell(3+i);
    }
    cell.setCellStyle(totalStyle);
}

Cell totalLabelCell = totalRow.getCell(3);
totalLabelCell.setCellValue("TOTAL");

sheet.addMergedRegion(new CellRangeAddress(totalRow.getRowNum(), totalRow.getRowNum(), 3, 6));

Cell totalValueCell = totalRow.getCell(7);
totalValueCell.setCellValue(totalWeightSum + " KG");


    // Auto-size columns
    for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(3+i);
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    workbook.write(out);
    workbook.close();
    return new ByteArrayInputStream(out.toByteArray());
}



public InputStream exportPackingToExcel(List<PackingEntityDao> packingList, String companyName,
                                        String deviceName, String startDate, String endDate) throws Exception {

    if (packingList == null || packingList.isEmpty()) {
        throw new Exception("No Packing report data available for the selected date.");
    }

    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet("Packing Report");

    // Title style
    CellStyle titleStyle = workbook.createCellStyle();
    Font titleFont = workbook.createFont();
    titleFont.setBold(true);
    titleFont.setFontHeightInPoints((short) 14);
    titleStyle.setFont(titleFont);
    titleStyle.setAlignment(HorizontalAlignment.CENTER);

    // Header style
    CellStyle headerStyle = workbook.createCellStyle();
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setColor(IndexedColors.WHITE.getIndex());
    headerStyle.setFont(headerFont);
    headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    headerStyle.setAlignment(HorizontalAlignment.CENTER);
    headerStyle.setBorderBottom(BorderStyle.THIN);
    headerStyle.setBorderTop(BorderStyle.THIN);
    headerStyle.setBorderLeft(BorderStyle.THIN);
    headerStyle.setBorderRight(BorderStyle.THIN);

    // Cell style
    CellStyle cellStyle = workbook.createCellStyle();
    cellStyle.setAlignment(HorizontalAlignment.CENTER);
    cellStyle.setBorderBottom(BorderStyle.THIN);
    cellStyle.setBorderTop(BorderStyle.THIN);
    cellStyle.setBorderLeft(BorderStyle.THIN);
    cellStyle.setBorderRight(BorderStyle.THIN);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    int rowIdx = 0;

    Row compRow = sheet.createRow(rowIdx++);
    Cell compCell = compRow.createCell(3);
    compCell.setCellValue(companyName);
    compCell.setCellStyle(titleStyle);
    sheet.addMergedRegion(new CellRangeAddress(0, 0, 3, 9));

    Row deviceRow = sheet.createRow(rowIdx++);
    Cell deviceCell = deviceRow.createCell(3);
    deviceCell.setCellValue("Device Name: " + deviceName);
    deviceCell.setCellStyle(titleStyle);
    sheet.addMergedRegion(new CellRangeAddress(1, 1, 3, 9));

    Row dateRow = sheet.createRow(rowIdx++);
    Cell dateCell = dateRow.createCell(3);
    dateCell.setCellValue("From: " + startDate + "   To: " + endDate);
    dateCell.setCellStyle(titleStyle);
    sheet.addMergedRegion(new CellRangeAddress(2, 2, 3, 9));

    rowIdx++;

    Row header = sheet.createRow(rowIdx++);
    String[] headers = {
        "Timestamp", "Batch ID", "Bag Weight Set", "Actual Weight",
        "Bag Count", "Weight of Bag", "Accumulated Weight"};

    for (int i = 0; i < headers.length; i++) {
        Cell hCell = header.createCell(3+i);
        hCell.setCellValue(headers[i]);
        hCell.setCellStyle(headerStyle);
    }

    for (PackingEntityDao record : packingList) {
        Row row = sheet.createRow(rowIdx++);

        int col = 3;
        row.createCell(col++).setCellValue(record.getTimestamp().format(formatter));
        row.createCell(col++).setCellValue(record.getBatchId());
        row.createCell(col++).setCellValue(record.getBagWeightSet());
        row.createCell(col++).setCellValue(record.getActualWeight());
        row.createCell(col++).setCellValue(record.getBagCount());
        row.createCell(col++).setCellValue(record.getWeightOfBag());
        row.createCell(col++).setCellValue(record.getAccumulatedWeight());

        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.getCell(3+i);
            if (cell == null) cell = row.createCell(3+i);
            cell.setCellStyle(cellStyle);
        }
    }

    for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(3+i);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    workbook.write(out);
    workbook.close();
    return new ByteArrayInputStream(out.toByteArray());
}

}
