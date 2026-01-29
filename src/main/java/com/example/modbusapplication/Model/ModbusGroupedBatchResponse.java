package com.example.modbusapplication.Model;

import java.util.List;

public class ModbusGroupedBatchResponse {

    private List<ModbusBatchGroup> batch;

    public List<ModbusBatchGroup> getBatch() {
        return batch;
    }

    public void setBatch(List<ModbusBatchGroup> batch) {
        this.batch = batch;
    }

}
