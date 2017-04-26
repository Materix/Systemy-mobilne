package pl.edu.agh.sm.magneto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;

import pl.edu.agh.sm.magneto.commons.PositionData;


class DataSender implements Runnable {
    private final String serverHost;
    private final int serverPort;

    private final BlockingQueue<PositionData> queue;

    DataSender(String serverHost, int serverPort, BlockingQueue<PositionData> queue) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.queue = queue;
    }

    @Override
    public void run() {
        DatagramSocket socket;
        try {
            socket = new DatagramSocket();
            InetAddress local = InetAddress.getByName(serverHost);
            while (true) {
                PositionData data = queue.take();
                byte[] byte_data = PositionData.serialize(data);
                DatagramPacket p = new DatagramPacket(byte_data, byte_data.length, local, serverPort);
                socket.send(p);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
