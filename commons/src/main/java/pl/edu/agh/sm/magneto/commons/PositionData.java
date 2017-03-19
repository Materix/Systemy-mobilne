package pl.edu.agh.sm.magneto.commons;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

public class PositionData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String value;

    public PositionData(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static byte[] serialize(PositionData positionData) {
        return SerializationUtils.serialize(positionData);
    }

    public static PositionData deserialize(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }

}
