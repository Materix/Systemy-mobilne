package pl.edu.agh.sm.magneto.desktop;

import java.awt.AWTException;
import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import pl.edu.agh.sm.magneto.commons.PositionData;
import pl.edu.agh.sm.magneto.desktop.model.PositionHolder;

public class DesktopRunner extends Application {

    private static Logger logger = Logger.getLogger(DesktopRunner.class.getName());

    private static Thread receiveDataThread;

    public static void main(String[] args) throws IOException, AWTException {

        startDataReceiver();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(FXMLLoader.load(DesktopRunner.class.getResource("main.fxml")),
                1024, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(t -> {
			Platform.exit();
			System.exit(0);
		});
    }

    private static void startDataReceiver() {

        receiveDataThread = new Thread(() -> {
            try {
                DataReceiver dataReceiver = new DataReceiver();
                while (true) {
                    PositionData data = dataReceiver.receiveData();
                    logger.log(Level.INFO, data.toString());
                    Platform.runLater(() -> PositionHolder.getInstance().setPosition(data.toString()));

                }
            } catch (Exception ex) {

            }
        });
        receiveDataThread.start();
    }
}
