package pl.edu.agh.sm.magneto.desktop;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;

import pl.motiondrawing.commons.ButtonChange;
import pl.motiondrawing.commons.SensorData;

public class MouseDriver {

    private static final int X_FACTOR = 2000;
    private static final double X_THRESHOLD = 0.002f;

    private static final int Y_FACTOR = 2000;
    private static final double Y_THRESHOLD = 0.002f;

    private static final int LEFT_BUTTON_NUMBER = InputEvent.BUTTON1_MASK;
    private static final int RIGHT_BUTTON_NUMBER = InputEvent.BUTTON3_MASK;

    private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private int screenWidth = (int) screenSize.getWidth();
    private int screenHeight = (int) screenSize.getHeight();
    private Robot robot;

    public MouseDriver() throws AWTException {
        robot = new Robot();
    }

    public void moveMouse(SensorData data) {
        Point currentLocation = MouseInfo.getPointerInfo().getLocation();

        float dx = data.getX();
        float dy = data.getY();

        dx = (Math.abs(dx) > X_THRESHOLD) ? dx * X_FACTOR : 0;
        dy = (Math.abs(dy) > Y_THRESHOLD) ? dy * Y_FACTOR : 0;

        int mouseX = (int) (dx + currentLocation.getX());
        int mouseY = (int) (dy + currentLocation.getY());

        mouseX = limitCoordinate(mouseX, screenWidth);
        mouseY = limitCoordinate(mouseY, screenHeight);

        robot.mouseMove(mouseX, mouseY);

        changeButtonState(LEFT_BUTTON_NUMBER, data.getLeftButtonChange());
        changeButtonState(RIGHT_BUTTON_NUMBER, data.getRightButtonChange());
    }

    private int limitCoordinate(int coordinate, int screenSizeDimension) {
        if (coordinate < 0) {
            coordinate = 0;
        } else if (coordinate > screenSizeDimension) {
            coordinate = screenSizeDimension;
        }

        return coordinate;
    }

    private void changeButtonState(int buttonNumber, ButtonChange buttonChange) {
        if (buttonChange == ButtonChange.DOWN) {
            robot.mousePress(buttonNumber);
        } else if (buttonChange == ButtonChange.UP) {
            robot.mouseRelease(buttonNumber);
        }
    }
}
