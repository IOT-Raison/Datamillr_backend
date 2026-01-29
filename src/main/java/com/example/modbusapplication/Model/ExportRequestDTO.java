package com.example.modbusapplication.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequestDTO {
    private Short deviceId;
    private String startDate;
    private String endDate;
    private String deviceName; // This is used in the Excel sheet
}



