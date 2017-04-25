package pl.edu.agh.sm.magneto.commons;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

public class PositionData implements Serializable {
    private static final long serialVersionUID = 3L;

    private final String value;

    private final double x;

    private final double y;

    private final double z;

    /**
     * Create new PositionData instance.
     *
     * @param x right-left
     * @param y up-down
     * @param z front-back
     */
    public PositionData(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.value = x + ";" + y + ";" + z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
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

        return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0 && Double.compare(that.z, z) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
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

}
