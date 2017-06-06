package pl.edu.agh.sm.magneto.commons;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.Arrays;

public class PositionData implements Serializable {
	private static final long serialVersionUID = 2L;

	private final float[] accelerometer;
	private final float[] gyroscope;
	private final float[] magnetometer;
	private long timestamp;

	public PositionData(float[] accelerometer, float[] gyroscope, float[] magnetometer, long timestamp) {
		this.accelerometer = accelerometer;
		this.gyroscope = gyroscope;
		this.magnetometer = magnetometer;
		this.timestamp = timestamp;
	}

	public float[] getAccelerometer() {
		return accelerometer;
	}

	public float[] getGyroscope() {
		return gyroscope;
	}

	public float[] getMagnetometer() {
		return magnetometer;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getStringValue() {
		return "ACC: " + Arrays.toString(accelerometer)
				+ "; GYR: " + Arrays.toString(gyroscope)
				+ "; MAG: " + Arrays.toString(magnetometer);
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
	public String toString() {
		return "PositionData{" +
				"accelerometer=" + Arrays.toString(accelerometer) +
				", gyroscope=" + Arrays.toString(gyroscope) +
				", magnetometer=" + Arrays.toString(magnetometer) +
				'}';
	}



	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PositionData that = (PositionData) o;

		if (!Arrays.equals(accelerometer, that.accelerometer)) return false;
		if (!Arrays.equals(gyroscope, that.gyroscope)) return false;
		return Arrays.equals(magnetometer, that.magnetometer);

	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(accelerometer);
		result = 31 * result + Arrays.hashCode(gyroscope);
		result = 31 * result + Arrays.hashCode(magnetometer);
		return result;
	}
}
