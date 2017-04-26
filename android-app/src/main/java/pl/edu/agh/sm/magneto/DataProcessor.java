package pl.edu.agh.sm.magneto;

import android.hardware.SensorManager;

import java.util.concurrent.BlockingQueue;

import pl.edu.agh.sm.magneto.commons.PositionData;

class DataProcessor {
    private float[] previousRotationMatrix = new float[9];
    private float[] rotationMatrix = new float[9];
    private float[] angleChange = new float[3];


    private BlockingQueue<PositionData> queue;

    DataProcessor(BlockingQueue<PositionData> queue) {
        this.queue = queue;
    }

    synchronized void registerSensorChange(float x, float y, float z) {

        SensorManager.getRotationMatrixFromVector(rotationMatrix, new float[]{x, y, z});
        SensorManager.getAngleChange(angleChange, rotationMatrix, previousRotationMatrix);
        System.arraycopy(rotationMatrix, 0, previousRotationMatrix, 0, rotationMatrix.length);

        PositionData data = new PositionData(x, y, z);
        publishState(data);
    }

    private void publishState(PositionData data) {
        queue.offer(data);
    }

}
