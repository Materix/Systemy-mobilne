package pl.edu.agh.sm.magneto.commons;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

public class PositionData implements Serializable {
    private static final long serialVersionUID = 2L;

    private final String value;

    private final int x;

    private final int y;

    private final int z;

    /**
     * Create new PositionData instance.
     *
     * @param x right-left
     * @param y up-down
     * @param z front-back
     */
    public PositionData(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.value = x + ";" + y + ";" + z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PositionData that = (PositionData) o;

        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    public static byte[] serialize(PositionData positionData) {
        return SerializationUtils.serialize(positionData);
    }

    public static PositionData deserialize(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }

}
