package pl.edu.agh.sm.magneto.desktop;


import org.apache.commons.lang3.SerializationUtils;

import pl.edu.agh.sm.magneto.commons.PositionData;

import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TestDataSender {
    public static void main(String[] args) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("localhost");
        byte[] bytes = Files.readAllBytes(Paths.get(DataListSerializer.FILENAME));
        List<PositionData> deserialized = SerializationUtils.deserialize(bytes);

        while (true) {
            for (PositionData positionData : deserialized) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                byte[] data = positionData.serialize();

                DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, DataReceiver.PORT);
                clientSocket.send(sendPacket);
            }
        }
    }
}
