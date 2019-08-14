package Interface;

import Printer.PrinterService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import properties.Props;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class SampleController implements Initializable {
    @FXML
    private WebView browser;

    private WebEngine webEngine;

    public void initialize(URL location, ResourceBundle resources) {

        Props.setSampleController(this);

        webEngine = browser.getEngine();

        webEngine.setJavaScriptEnabled(true);

        webEngine.load(Props.getKioskHtm());

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                testPrinterBeforeStart();
            }
        });

        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    /* The two objects are named using the setMember() method. */
                    window.setMember("invoke", JSCall.getJsCall());
                }
            }
        });

    }

    public void testPrinterBeforeStart() {
        String errorMessage = testStartInterface();

        if (errorMessage == null)
            initPageAfterLoad();
        else
            displayError(errorMessage);
    }

    public void displayError(String error) {
        webEngine.executeScript("displayError('" + error + "')");
    }

    private void initPageAfterLoad(){

        String json = Soap.getMenuJson();
        json = json.replace("\n","");
        webEngine.executeScript("GetMenu('" + json + "')");
    }

    public static String testStartInterface() {
        String errorMessage = null;

        PrinterService printerService = PrinterService.getServicePrint();

        if (!Props.isConnectToServer())
            errorMessage = "Не удалось связаться с сервером.";
        else {
            if (!Props.isRegistration())
                errorMessage = "Киоск не зарегистрирован на сервере.";
            else{
                if (printerService == null) {
                    errorMessage = "Принтер не найден.";
                    Props.setErrorPrinter(true);
                }
                else {
                    printerService.initializePrinter();
                    if (!printerService.isWorking()) {
                        errorMessage = "Принтер не работает.";
                        Props.setErrorPrinter(true);
                    }
                    else {
                        if (!printerService.isPaper())
                            errorMessage = "В принтере нет бумаги.";
                    }
                }
            }
        }

        if (errorMessage != null)
            Props.getLogger().log(Level.WARNING, "Ошибка при работе интерфейса: " + errorMessage);
        else
            if (Props.isErrorPrinter())
                Props.setErrorPrinter(false);
        return errorMessage;
    }
}


