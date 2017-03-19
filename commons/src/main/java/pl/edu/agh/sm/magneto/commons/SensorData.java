package pl.edu.agh.sm.magneto.commons;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

public class SensorData implements Serializable {
    private static final long serialVersionUID = 3L;

    private final float x;
    private final float y;
    private final float z;
    private final ButtonChange leftButtonChange;
    private final ButtonChange rightButtonChange;

    public SensorData(float x, float y, float z, ButtonChange leftButtonChange, ButtonChange rightButtonChange) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.leftButtonChange = leftButtonChange;
        this.rightButtonChange = rightButtonChange;
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

    public static byte[] serialize(SensorData sensorData) {
        return SerializationUtils.serialize(sensorData);
    }

    public static SensorData deserialize(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }

    public ButtonChange getLeftButtonChange() {
        return leftButtonChange;
    }

    public ButtonChange getRightButtonChange() {
        return rightButtonChange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SensorData data = (SensorData) o;

        if (Float.compare(data.x, x) != 0) return false;
        if (Float.compare(data.y, y) != 0) return false;
        if (Float.compare(data.z, z) != 0) return false;
        if (leftButtonChange != data.leftButtonChange) return false;
        return rightButtonChange == data.rightButtonChange;

    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + (z != +0.0f ? Float.floatToIntBits(z) : 0);
        result = 31 * result + (leftButtonChange != null ? leftButtonChange.hashCode() : 0);
        result = 31 * result + (rightButtonChange != null ? rightButtonChange.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", leftButtonChange=" + leftButtonChange +
                ", rightButtonChange=" + rightButtonChange +
                '}';
    }
}
