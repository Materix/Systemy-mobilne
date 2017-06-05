package pl.edu.agh.sm.magneto;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import pl.edu.agh.sm.magneto.commons.PositionData;

public class MainActivity extends AppCompatActivity {
    private BlockingQueue<PositionData> queue = new LinkedBlockingQueue<>();
    private DataProcessor dataProcessor = new DataProcessor(queue);
    private SensorListener sensorListener = new SensorListener(dataProcessor);
    private SensorManager sensorManager;

    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor magnetometer;
    private Sensor pose6Dof;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        pose6Dof = sensorManager.getDefaultSensor(Sensor.TYPE_POSE_6DOF);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Intent intent = getIntent();
        String serverHost = intent.getStringExtra(ConnectActivity.HOST_MESSAGE);
        int serverPort = intent.getIntExtra(ConnectActivity.PORT_MESSAGE, ConnectActivity.DEFAULT_PORT);

        Thread dataSender = new Thread(new DataSender(serverHost, serverPort, queue));
        dataSender.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorListener, gyroscope, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorListener, magnetometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorListener, pose6Dof, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorListener);
    }
}