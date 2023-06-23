package com.pegalite.steptacker;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class StepCounter implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private StepCountListener stepCountListener;
    private int stepCount = 0;

    public StepCounter(SensorManager sensorManager, StepCountListener stepCountListener) {
        this.sensorManager = sensorManager;
        this.stepCountListener = stepCountListener;
        initializeSensors();
    }

    private void initializeSensors() {
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (stepDetectorSensor == null) {
            // Step detector sensor not available on this device
            // You can handle this case as needed
        }
    }

    public void startCountingSteps() {
        sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopCountingSteps() {
        sensorManager.unregisterListener(this, stepDetectorSensor);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            // A step is detected
            stepCount++;
            stepCountListener.onStepCountUpdated(stepCount);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for step counting
    }

    public interface StepCountListener {
        void onStepCountUpdated(int stepCount);
    }
}
