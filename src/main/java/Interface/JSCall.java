package Interface;

import Printer.PrintTemplate;
import properties.Props;

import java.util.logging.Level;

public class JSCall {

    private static JSCall jsCall = null;

    public static JSCall getJsCall() {
        if (jsCall == null)
            jsCall = new JSCall();
        return jsCall;
    }

    public void newTalon (String usluga) {

        String errorMessage = SampleController.testStartInterface();

        if (errorMessage == null) {
            String request = Soap.newTalon(usluga);
            Props.getLogger().log(Level.INFO, "Создание талона по услуге " + usluga);
            PrintTemplate.printToPrinter(request);
        } else {
            Props.getSampleController().displayError(errorMessage);
        }
    }

    public void startTestPrinter() {
        Props.getSampleController().testPrinterBeforeStart();
    }
}
