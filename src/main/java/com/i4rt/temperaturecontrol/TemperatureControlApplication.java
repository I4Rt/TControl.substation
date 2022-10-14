package com.i4rt.temperaturecontrol;

import com.i4rt.temperaturecontrol.deviceControlThreads.DefaultTemperatureCheck;
import com.intelligt.modbus.jlibmodbus.Modbus;
import com.intelligt.modbus.jlibmodbus.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters;
import com.intelligt.modbus.jlibmodbus.serial.SerialPort;
import jssc.SerialPortList;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TemperatureControlApplication {



    public static void main(String[] args) {
        SpringApplication.run(TemperatureControlApplication.class, args);
    }

}
