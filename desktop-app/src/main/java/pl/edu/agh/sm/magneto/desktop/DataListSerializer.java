package pl.edu.agh.sm.magneto.desktop;


import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import pl.edu.agh.sm.magneto.commons.PositionData;

public class DataListSerializer {
    public static final String FILENAME = "data.bytes";
    private static final long NANOSEC_PER_SEC = 1000L * 1000 * 1000;
    private static final long SECONDS_RUNNING = 120;

    public static void main(String[] args) throws IOException {
        DataReceiver dataReceiver = new DataReceiver();
        List<PositionData> dataList = new LinkedList<>();

        long startTime = System.nanoTime();
        while ((System.nanoTime() - startTime) < SECONDS_RUNNING * NANOSEC_PER_SEC) {
            PositionData positionData = dataReceiver.receiveData();
            dataList.add(positionData);
        }
        System.out.println(dataList.size());
        byte[] serialized = SerializationUtils.serialize((Serializable) dataList);
        Files.write(Paths.get(FILENAME), serialized);

        byte[] bytes_read = Files.readAllBytes(Paths.get(FILENAME));
        List<PositionData> deserialized = SerializationUtils.deserialize(bytes_read);
        deserialized.forEach(entry -> System.out.println(entry.toString()));
        System.out.print(deserialized.size());
    }
}
