package pl.edu.agh.sm.magneto.desktop;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.edu.agh.sm.magneto.commons.PositionData;
import pl.edu.agh.sm.magneto.desktop.model.PositionHolder;

public class DataReceiver {

    private static final int PORT = 8192;
    private static final int DATAGRAM_SIZE = 4096;

    private DatagramSocket serverSocket;
    private byte[] receiveData = new byte[DATAGRAM_SIZE];

    private Logger logger = Logger.getLogger(DataReceiver.class.getName());

    public DataReceiver() throws SocketException {
        serverSocket = new DatagramSocket(PORT);
    }

    public PositionData receiveData() throws IOException {
//        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//        serverSocket.receive(receivePacket);
//
//        PositionData data = PositionData.deserialize(receivePacket.getData());
//        logger.log(Level.INFO, data.toString());

//        return data;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new PositionData(Long.toString(System.currentTimeMillis()));
    }
}
