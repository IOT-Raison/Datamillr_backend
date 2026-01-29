package com.example.modbusapplication.Model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModbusBatchGroup {

    private ModbusEntityDao batchStartdata;
    private ModbusEntityDao batchEnddata;
    private List<ModbusEntityDao> batchdata;

}
