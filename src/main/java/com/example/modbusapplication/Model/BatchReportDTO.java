package com.example.modbusapplication.Model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BatchReportDTO {
    private short deviceId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String batchName;
    private int totalWeight;
    private boolean isEndOfBatch;
    
}
