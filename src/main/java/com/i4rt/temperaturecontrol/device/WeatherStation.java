
package com.i4rt.temperaturecontrol.device;

import com.intelligt.modbus.jlibmodbus.Modbus;
import com.intelligt.modbus.jlibmodbus.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters;
import com.intelligt.modbus.jlibmodbus.serial.SerialPort;
import jssc.SerialPortList;
import lombok.*;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;

@Setter
@Getter
@ToString
public class WeatherStation {
    
    private static WeatherStation instance;

    
    private Double temperature;

    
    private Double windForce;

    
    private Double humidity;

    
    private Double atmospherePressure;

    
    private Double rainfall;


    public static WeatherStation getInstance(){
        if(instance == null){
            instance = new WeatherStation();
        }
        return instance;
    }


    public void makeMeasurements() {
        SerialParameters sp = new SerialParameters();
        Modbus.setLogLevel(Modbus.LogLevel.LEVEL_DEBUG);
        try {
            String[] dev_list = SerialPortList.getPortNames();
            for(String s : dev_list){
                System.out.println(s);
            }
            if (dev_list.length > 0) {


                // Выбираем порт
                sp.setDevice("COM9");

                sp.setBaudRate(SerialPort.BaudRate.BAUD_RATE_19200);
                sp.setDataBits(8);
                sp.setParity(SerialPort.Parity.NONE);
                sp.setStopBits(1);
                System.out.println("before modbus");
                ModbusMaster m = ModbusMasterFactory.createModbusMasterRTU(sp);
                m.connect();
                System.out.println("after modbus");

                // Адрес метеостанции
                int slaveId = 0x33;

                // Опрашиваемый регистр, 6 - температура
                int offset = 6;
                int quantity = 1;

                this.temperature = getValues(m, slaveId, 6, quantity)/10.0;
                this.windForce = getValues(m, slaveId, 5, quantity)/10.0;
                this.humidity = getValues(m, slaveId, 7, quantity)/10.0;
                this.atmospherePressure = getValues(m, slaveId, 8, quantity)/10.0;
                this.rainfall = getValues(m, slaveId, 9, quantity)/10.0;

                System.out.println(this.toString());

                try {
                    m.disconnect();
                } catch (ModbusIOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected static int getValues(ModbusMaster m, int slaveId, int offset, int quantity){
        int value = 0;
        try {
            System.out.println("before reading");
            int[] registerValues = m.readHoldingRegisters(slaveId, offset, quantity);
            value = registerValues[0];
            System.out.println("Address: " + offset++ + ", Value: " + value);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }




}
