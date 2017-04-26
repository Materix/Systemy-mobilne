package pl.edu.agh.sm.magneto;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;


class SensorListener implements SensorEventListener {
    private static final float ALPHA = 0.25f;

    private DataProcessor dataProcessor;

    private float[] sensorValues = new float[5];

    SensorListener(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sensorValues = lowPassFilter(event.values.clone(), sensorValues);

        float x = sensorValues[0];
        float y = sensorValues[1];
        float z = sensorValues[2];

        dataProcessor.registerSensorChange(x, y, z);
    }

    private float[] lowPassFilter(float[] input, float[] output) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }



}
