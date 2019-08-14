package Printer;

import jssc.*;
import properties.Props;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

public class PrinterService {

    private static PrinterService servicePrint = null;

    private SerialPort serialPort;

    private String name;
    private boolean isPaper;
    private boolean working = false;

    public String getName() {
        return name;
    }

    public boolean isPaper() {
        return isPaper;
    }

    public boolean isWorking() {
        return working;
    }

    public PrinterService(String name, SerialPort serialPort) {
        this.name = name;
        this.serialPort = serialPort;
        this.initializePrinter();
    }

    private static void updateCOMPorts() {


            File folder = new File("/dev");

            String[] files = folder.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.contains("ttyUSB");
                }
            });

            for (String fName : files) {

                try {
                    Process process = Runtime.getRuntime().exec("chmod 777 " + fName);
                    Props.getLogger().log(Level.INFO, "Выполнена консольная команда: " + "chmod 777 " + fName);
                } catch (IOException e) {
                    Props.getLogger().log(Level.WARNING, "Не удалось выполнить консольную команду " + "chmod 777 " + fName, e);
                }
            }
    }

    public synchronized static PrinterService getServicePrint() {
        if (System.getProperty("os.name").indexOf("nix") > 1)
            if (Props.isErrorPrinter() || servicePrint == null)
                updateCOMPorts();
        if (servicePrint == null || Props.isErrorPrinter()){

            String[] strArray = SerialPortList.getPortNames();

            if (strArray.length == 0) {
                Props.getLogger().log(Level.WARNING, "Нет доступных COM-ортов!");
                return null;
            }

            for (String str : strArray){
                servicePrint = new PrinterService(str, new SerialPort(str));

                if (servicePrint.working)
                    break;
            }

            return servicePrint;
        } else
            return servicePrint;
    }

    public PrinterService() {
    }

    public void initializePrinter() {
        try {
            this.openService();

            this.initPrinter();

            this.working = this.getStatusPrinter();

            this.closeService();
        } catch (SerialPortException e) {
            this.working = false;
            this.isPaper = false;
            Props.getLogger().log(Level.WARNING, "Ошибка при обращении к COM-порту: ", e);
        }


    }

    public void openService() throws SerialPortException {
        this.serialPort.openPort();
        this.serialPort.setParams(9600, 8, 1, 0);
    }

    public void closeService() throws SerialPortException {
        this.serialPort.closePort();
    }

    public void initPrinter() throws SerialPortException {
        this.serialPort.writeBytes(new byte[]{0x1B, '@'});
    }

    public boolean getStatusPrinter() throws SerialPortException {
        this.serialPort.writeBytes(new byte[]{0x1b, 'v'});

        byte[] data = new byte[0];
        try {
            data = this.serialPort.readBytes(1, 1000);
            if (data[0] == 3)
                this.isPaper = true;
            else
                this.isPaper = false;

            return true;
        } catch (SerialPortTimeoutException e) {
            Props.getLogger().log(Level.WARNING, "Не удалось получить ответ от COM-порта. Ожидание сброшено по таймауту", e);
            return false;
        }
    }

    public void setAlignTextToLine(String alignAttribute) throws SerialPortException {

        int align = 0;

        if (alignAttribute.equals("centre"))
            align = 1;
        else if (alignAttribute.equals("right"))
            align = 2;

        this.serialPort.writeBytes(new byte[]{0x1B, 0x61, (byte) align});
    }

    public void setKegel(int kegel) throws SerialPortException {
        byte[] command = new byte[0];
        switch (kegel) {
            case 0 :
                command = new byte[]{0x1D, 0x21, 0x00};
                break;
            case 1 :
                command = new byte[]{0x1D, 0x21, 0x11};
                break;
            case 2 :
                command = new byte[]{0x1D, 0x21, 0x22};
                break;
            case 3 :
                command = new byte[]{0x1D, 0x21, 0x33};
                break;
            case 4 :
                command = new byte[]{0x1D, 0x21, 0x44};
                break;
            case 5 :
                command = new byte[]{0x1D, 0x21, 0x55};
                break;
            case 6 :
                command = new byte[]{0x1D, 0x21, 0x66};
                break;
            case 7 :
                command = new byte[]{0x1D, 0x21, 0x77};
                break;
        }
        this.serialPort.writeBytes(command);
    }

    public void printString(String str) throws SerialPortException {

        this.serialPort.writeBytes(new byte[]{0x1B, 0x74, 0x3E}); //Установка кодировки на принтере

        try {
            this.serialPort.writeBytes(str.getBytes("ISO-8859-5"));
        } catch (UnsupportedEncodingException e) {
            Props.getLogger().log(Level.WARNING, "Неподдерживаемая кодировка.");
        }
    }

    public void printAndNewLine() throws SerialPortException {
        this.serialPort.writeBytes(new byte[]{0x0A});
    }

    public void cutPaper() throws SerialPortException {
        this.serialPort.writeBytes(new byte[]{0x1B, 0x69});
    }

    public void printAndCutPaperByNLines(int n, boolean cut) throws SerialPortException {
        this.serialPort.writeBytes(new byte[]{0x1B, 0x64, (byte) n});
        if (cut)
            this.cutPaper();
    }

    public void setRecursivePrint(boolean set) throws SerialPortException {
        this.serialPort.writeBytes(new byte[]{0x1D, 0x42, (byte) (set ? 1 : 0)});
    }

    public void setSmoothing(boolean set) throws SerialPortException {
        this.serialPort.writeBytes(new byte[]{0x1D, 0x62, (byte) 100});
        //(byte) (set ? 1 : 0)
    }

    @Override
    public String toString() {
        return "Printer name = " + name +
                ", isPaper = " + isPaper +
                ", working = " + working;
    }
}


