package Start;

import Printer.PrinterService;
import UpdateFromServer.Updater;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import properties.Props;
import java.util.logging.Level;

public class Solution extends Application {

    public static void main (String[] args) {  /* Точка входа в программу*/

        Props.getLogger().log(Level.INFO, "Запущена программа.");

        Updater.registrationKiosk(false);

        PrinterService servicePrint = PrinterService.getServicePrint();

        if (servicePrint != null) {
            Props.getLogger().log(Level.INFO, "Выбран принтер для печати: " + servicePrint.getName());

            if (!servicePrint.isPaper())
                Props.getLogger().log(Level.INFO, "В принтере нет бумаги. Вставьте бумагу");
        }
        launch(args);

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        String fxmlFile = "/fxml/sample.fxml";
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResourceAsStream(fxmlFile));
        primaryStage.setTitle("Киоск");

        //// ФУЛСКРИН  {
        primaryStage.setFullScreen(true);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            public void handle(WindowEvent event) {

                event.consume();
            }
        });
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        //// ФУЛСКРИН  }

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}