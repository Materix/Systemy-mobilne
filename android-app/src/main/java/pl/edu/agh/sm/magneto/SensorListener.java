package pl.edu.agh.sm.magneto;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;


class SensorListener implements SensorEventListener {
    private static final float ALPHA = 0.25f;

    private DataProcessor dataProcessor;

    SensorListener(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        float[] sensorValues = event.values;
//        sensorValues = lowPassFilter(event.values.clone(), sensorValues);
        dataProcessor.registerSensorChange(sensorType, sensorValues, event.timestamp);
    }

    private float[] lowPassFilter(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


}
