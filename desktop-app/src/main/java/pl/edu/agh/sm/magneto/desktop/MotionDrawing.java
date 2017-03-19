package pl.edu.agh.sm.magneto.desktop;

import java.awt.AWTException;
import java.io.IOException;

import pl.motiondrawing.commons.SensorData;

public class MotionDrawing {
    public static void main(String[] args) throws IOException, AWTException {
        DataReceiver dataReceiver = new DataReceiver();
        MouseDriver mouseDriver = new MouseDriver();

        while (true) {
            SensorData data = dataReceiver.receiveData();
            mouseDriver.moveMouse(data);
        }
    }
}
