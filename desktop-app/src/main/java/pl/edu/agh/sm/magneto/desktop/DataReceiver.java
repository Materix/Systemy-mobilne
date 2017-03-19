package pl.edu.agh.sm.magneto.desktop;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.motiondrawing.commons.SensorData;

public class DataReceiver {
    private static final int PORT = 8192;
    private static final int DATAGRAM_SIZE = 4096;

    private DatagramSocket serverSocket;
    private byte[] receiveData = new byte[DATAGRAM_SIZE];

    private Logger logger = Logger.getLogger(DataReceiver.class.getName());

    public DataReceiver() throws SocketException {
        serverSocket = new DatagramSocket(PORT);
    }

    public SensorData receiveData() throws IOException {
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);

        SensorData data = SensorData.deserialize(receivePacket.getData());
        logger.log(Level.INFO, data.toString());

        return data;
    }
}
