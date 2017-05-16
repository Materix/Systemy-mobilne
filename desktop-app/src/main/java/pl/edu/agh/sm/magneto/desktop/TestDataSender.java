package pl.edu.agh.sm.magneto.desktop;


import pl.edu.agh.sm.magneto.commons.PositionData;

import java.io.IOException;
import java.net.*;

public class TestDataSender {

    private static double y = 0;
    private static double scale = 5;

    public static void main(String[] args) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("localhost");

        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            y += 0.1;
            if (y > 25) {
                y = 0;
                scale += 10;
            }
            byte[] data = new PositionData(new float[]{0, 0, 0},
                    new float[]{0, 0, 0},
                    new float[]{(float) (scale * Math.sin(y)), (float) y, (float) (scale * Math.cos(y))}).serialize();

            DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, DataReceiver.PORT);
            clientSocket.send(sendPacket);
        }
    }
}
