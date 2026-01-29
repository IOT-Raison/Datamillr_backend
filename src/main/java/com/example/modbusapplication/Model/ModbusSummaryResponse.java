package com.example.modbusapplication.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModbusSummaryResponse {
    private String type; // "monthly" or "yearly"
    private List<SummaryItem> summary;
    private double grandTotal;

    @Getter
    @Setter
    public static class SummaryItem {
        private String label; // Day (e.g., "2025-07-01") or Month (e.g., "July")
        private double totalWeight;

    }
}


