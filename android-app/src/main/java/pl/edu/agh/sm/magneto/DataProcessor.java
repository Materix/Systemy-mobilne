package pl.edu.agh.sm.magneto;

import android.hardware.Sensor;

import java.util.concurrent.BlockingQueue;

import pl.edu.agh.sm.magneto.commons.PositionData;

class DataProcessor {
    private float[] accelerometerValues = new float[3];
    private float[] gyroscopeValues = new float[3];
    private float[] magnetometerValues = new float[3];

    private BlockingQueue<PositionData> queue;

    DataProcessor(BlockingQueue<PositionData> queue) {
        this.queue = queue;
    }

    synchronized void registerSensorChange(int sensorType, float[] sensorValues, long timestamp) {

        if (sensorType == Sensor.TYPE_LINEAR_ACCELERATION) {
            System.arraycopy(sensorValues, 0, accelerometerValues, 0, accelerometerValues.length);
        } else if (sensorType == Sensor.TYPE_GYROSCOPE) {
            System.arraycopy(sensorValues, 0, gyroscopeValues, 0, gyroscopeValues.length);
        } else if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(sensorValues, 0, magnetometerValues, 0, magnetometerValues.length);
        }

        PositionData data = new PositionData(accelerometerValues, gyroscopeValues, magnetometerValues,timestamp);
        publishState(data);
    }

    private void publishState(PositionData data) {
        queue.offer(data);
    }

}
