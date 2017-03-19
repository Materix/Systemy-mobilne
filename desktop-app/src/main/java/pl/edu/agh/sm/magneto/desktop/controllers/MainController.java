package pl.edu.agh.sm.magneto.desktop.controllers;

import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import pl.edu.agh.sm.magneto.desktop.model.PositionHolder;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    public SplitPane splitPane;
    public Label positionLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        positionLabel.textProperty().bind(PositionHolder.getInstance().getPositionProperty());
    }
}
