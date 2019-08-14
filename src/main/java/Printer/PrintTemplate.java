package Printer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jssc.SerialPortException;
import properties.Props;
import java.util.ArrayList;
import java.util.logging.Level;

public class PrintTemplate {

    public static boolean printToPrinter(String argument) {

        Gson gson = new GsonBuilder().create();
        JSONNewTalon requst = gson.fromJson(argument, JSONNewTalon.class);

        ArrayList<String> commandLines = getListFromStringWithSeparator(requst.getTemplateTalon(), ";");

        try {
            PrinterService printerService = PrinterService.getServicePrint();

            printerService.openService();
            printerService.initPrinter();

            for (String line : commandLines) {
                printCommand(line.trim(), requst.getRequest());
            }

            printerService.closeService();

            return true;
        } catch (SerialPortException e) {
            Props.getLogger().log(Level.WARNING, "Ошибака работы с принтером: ", e);
        }

        return false;
    }

    public static void printCommand(String command, String argument) throws SerialPortException {
        int indexS = command.indexOf("(");
        int indexE = command.indexOf(")");
        if (indexS > 0 & indexE > 0 & indexE > indexS) {
            String commandName = command.substring(0, indexS);
            String parametersCommand = command.substring(indexS + 1, indexE);

            PrinterService printerService = PrinterService.getServicePrint();

            if (commandName.equals("setAlignTextToLine"))
                setAlignTextToLine(printerService, parametersCommand);
            if (commandName.equals("setKegel"))
                setKegel(printerService, parametersCommand);
            if (commandName.equals("printString"))
                printString(printerService, parametersCommand);
            if (commandName.equals("printParameterFromWebService"))
                printParameterFromWebService(printerService,
                        parametersCommand,
                        getListFromStringWithSeparator(argument,"||"));
            if (commandName.equals("printAndNewLine"))
                printAndNewLine(printerService);
            if (commandName.equals("cutPaper"))
                cutPaper(printerService);
            if (commandName.equals("printAndCutPaperByNLines"))
                printAndCutPaperByNLines(printerService, getListFromStringWithSeparator(parametersCommand, ","));


        }else
            Props.getLogger().log(Level.WARNING, "Не удалось обработать команду для принтера: \"" + command + "\"");
    }

    public static ArrayList<String> getListFromStringWithSeparator(String str, String separator) {
        ArrayList<String> list = new ArrayList<>();
        String string = str;
        int index = str.indexOf(separator);

        while (index > -1) {
            list.add(string.substring(0, index));
            string = string.substring(index + separator.length());
            index = string.indexOf(separator);
        }

        list.add(string);

        return list;
    }

    private static void setAlignTextToLine(PrinterService printerService, String argumet) throws SerialPortException {
        printerService.setAlignTextToLine(argumet);
    }

    private static void setKegel(PrinterService printerService, String argumet) throws SerialPortException {
        printerService.setKegel((Integer.parseInt(argumet)));
    }

    private static void printString(PrinterService printerService, String argumet) throws SerialPortException {
        printerService.printString(argumet);
    }

    private static void printParameterFromWebService(PrinterService printerService, String argumet, ArrayList<String> listArguments) throws SerialPortException {
        printerService.printString(listArguments.get(Integer.parseInt(argumet)));
    }

    private static void printAndNewLine(PrinterService printerService) throws SerialPortException {
        printerService.printAndNewLine();
    }

    private static void cutPaper(PrinterService printerService) throws SerialPortException {
        printerService.cutPaper();
    }

    private static void printAndCutPaperByNLines(PrinterService printerService, ArrayList<String> listArguments) throws SerialPortException {
        printerService.printAndCutPaperByNLines(Integer.parseInt(listArguments.get(0)),
                Boolean.parseBoolean(listArguments.get(1)));
    }

    public class JSONNewTalon {
        public String Request;
        public String TemplateTalon;

        public String getRequest() {
            return Request;
        }

        public String getTemplateTalon() {
            return TemplateTalon;
        }

        public void setRequest(String Request) {
            Request = Request;
        }

        public void setTemplateTalon(String templateTalon) {
            TemplateTalon = templateTalon;
        }
    }
}
