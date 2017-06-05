package pl.edu.agh.sm.magneto.desktop;


import org.apache.commons.lang3.SerializationUtils;

import pl.edu.agh.sm.magneto.commons.PositionData;

import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringJoiner;

public class TestDataSender {
    public static void main(String[] args) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("localhost");
        byte[] bytes = Files.readAllBytes(Paths.get(DataListSerializer.FILENAME));
        List<PositionData> deserialized = SerializationUtils.deserialize(bytes);

        StringJoiner global = new StringJoiner("," + System.lineSeparator(), "[", "]");
        while (true) {

            for (PositionData positionData : deserialized) {
//                global.add(new StringJoiner(", ", "[", "]")
//                        .add(Float.toString(positionData.getMagnetometer()[0]))
//                        .add(Float.toString(positionData.getMagnetometer()[1]))
//                        .add(Float.toString(positionData.getMagnetometer()[2])).toString());
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                byte[] data = positionData.serialize();

                DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, DataReceiver.PORT);
                clientSocket.send(sendPacket);
            }

        }
//        System.out.println(global);
    }
}
