package me.nullx.comport;

import jssc.*;

public class Comunications {
    public static String[] getPorts() {
        return SerialPortList.getPortNames();
    }

    public static SerialPort connect(String portName, int baudrate, int databits, int stopbits, int parity,
            int flowcontrol) throws SerialPortException {
        SerialPort serialPort = new SerialPort(portName);
        serialPort.openPort();// Open serial port
        serialPort.purgePort(SerialPort.PURGE_TXCLEAR | SerialPort.PURGE_RXCLEAR);
        serialPort.setParams(baudrate, databits, stopbits, parity);

        return serialPort;
    }

    public static void disconnect(SerialPort port) throws SerialPortException {
        port.closePort();
    }
}
