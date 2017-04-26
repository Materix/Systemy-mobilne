package pl.edu.agh.sm.magneto.commons;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

public class PositionData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final float x;
    private final float y;
    private final float z;

    /**
     * Create new PositionData instance.
     *
     * @param x right-left
     * @param y up-down
     * @param z front-back
     */
    public PositionData(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public String getStringValue() {
        return x + ";" + y + ";" + z;
    }

    public static byte[] serialize(PositionData positionData) {
        return SerializationUtils.serialize(positionData);
    }

    public byte[] serialize() {
        return serialize(this);
    }

    public static PositionData deserialize(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PositionData that = (PositionData) o;

        if (Float.compare(that.x, x) != 0) return false;
        if (Float.compare(that.y, y) != 0) return false;
        return Float.compare(that.z, z) == 0;

    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + (z != +0.0f ? Float.floatToIntBits(z) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PositionData{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
