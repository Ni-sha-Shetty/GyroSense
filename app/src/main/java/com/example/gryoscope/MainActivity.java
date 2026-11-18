package com.example.gryoscope;

import android.app.Activity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity
        implements SensorEventListener {

    // Sensor variables

    private SensorManager sensorManager;
    private Sensor gyroscope;

    // TextViews

    // 3D Rotation Indicator
    private RotationView rotationView;
    private TextView tvStatus;
    private TextView tvX, tvY, tvZ;
    private TextView tvDegrees;
    private TextView tvMagnitude;
    private TextView tvPeak;
    private TextView tvSensorInfo;

    // Buttons

    private Button btnStart, btnPause, btnReset;

    // Sensor readings

    private float xValue = 0;
    private float yValue = 0;
    private float zValue = 0;

    private float peakMagnitude = 0;

    private boolean isMonitoring = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Connect 3D rotation indicator

        rotationView = findViewById(R.id.rotationView);

        // Connect XML components with Java

        tvStatus = findViewById(R.id.tvStatus);

        tvX = findViewById(R.id.tvX);
        tvY = findViewById(R.id.tvY);
        tvZ = findViewById(R.id.tvZ);

        tvDegrees = findViewById(R.id.tvDegrees);
        tvMagnitude = findViewById(R.id.tvMagnitude);
        tvPeak = findViewById(R.id.tvPeak);

        tvSensorInfo = findViewById(R.id.tvSensorInfo);

        btnStart = findViewById(R.id.btnStart);
        btnPause = findViewById(R.id.btnPause);
        btnReset = findViewById(R.id.btnReset);

        // Access Android Sensor Service

        sensorManager = (SensorManager)
                getSystemService(SENSOR_SERVICE);

        // Find the gyroscope sensor

        gyroscope = sensorManager.getDefaultSensor(
                Sensor.TYPE_GYROSCOPE);

        // Check sensor availability

        if (gyroscope == null) {

            tvStatus.setText(
                    "Gyroscope is NOT available on this device."
            );

            tvSensorInfo.setText(
                    "This phone does not have a gyroscope sensor."
            );

            btnStart.setEnabled(false);
            btnPause.setEnabled(false);

        } else {

            tvStatus.setText(
                    "Gyroscope detected successfully!"
            );

            displaySensorInformation();
        }

        // Start button

        btnStart.setOnClickListener(v -> startMonitoring());

        // Pause button

        btnPause.setOnClickListener(v -> pauseMonitoring());

        // Reset button

        btnReset.setOnClickListener(v -> resetStatistics());

        // Initial button state

        btnPause.setEnabled(false);
    }

    // Display hardware sensor information

    private void displaySensorInformation() {

        if (gyroscope == null) {
            return;
        }

        String information =
                "Sensor Name: " + gyroscope.getName() +
                        "\n\nVendor: " + gyroscope.getVendor() +
                        "\n\nVersion: " + gyroscope.getVersion() +
                        "\n\nPower: " + gyroscope.getPower() + " mA" +
                        "\n\nResolution: " + gyroscope.getResolution() +
                        "\n\nMaximum Range: " + gyroscope.getMaximumRange();

        tvSensorInfo.setText(information);
    }

    // Start sensor monitoring

    private void startMonitoring() {

        if (gyroscope == null) {
            return;
        }

        if (!isMonitoring) {

            boolean registered = sensorManager.registerListener(
                    this,
                    gyroscope,
                    SensorManager.SENSOR_DELAY_UI
            );

            if (registered) {

                isMonitoring = true;

                tvStatus.setText("Monitoring is ACTIVE");

                btnStart.setEnabled(false);
                btnPause.setEnabled(true);

            } else {

                tvStatus.setText(
                        "Unable to start sensor monitoring."
                );
            }
        }
    }

    // Pause sensor monitoring

    private void pauseMonitoring() {

        sensorManager.unregisterListener(this);

        isMonitoring = false;

        tvStatus.setText("Monitoring is PAUSED");

        btnStart.setEnabled(true);
        btnPause.setEnabled(false);
    }

    // Reset recorded statistics

    private void resetStatistics() {

        xValue = 0;
        yValue = 0;
        zValue = 0;

        peakMagnitude = 0;

        tvX.setText("0.000 rad/s");
        tvY.setText("0.000 rad/s");
        tvZ.setText("0.000 rad/s");

        tvDegrees.setText(
                "X: 0.00°/s\n" +
                        "Y: 0.00°/s\n" +
                        "Z: 0.00°/s"
        );

        tvMagnitude.setText("0.000 rad/s");
        tvPeak.setText("0.000 rad/s");

        // Reset virtual phone orientation

        rotationView.resetRotation();
    }

    // Receive live sensor data

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() ==
                Sensor.TYPE_GYROSCOPE) {

            // Get X, Y and Z readings

            xValue = event.values[0];
            yValue = event.values[1];
            zValue = event.values[2];

            // Update virtual 3D phone rotation

            rotationView.updateRotation(
                    xValue,
                    yValue,
                    zValue,
                    event.timestamp
            );

            // Convert radians into degrees

            float xDegrees = (float)
                    Math.toDegrees(xValue);

            float yDegrees = (float)
                    Math.toDegrees(yValue);

            float zDegrees = (float)
                    Math.toDegrees(zValue);

            // Calculate rotation magnitude

            float magnitude = (float) Math.sqrt(
                    xValue * xValue +
                            yValue * yValue +
                            zValue * zValue
            );

            // Find peak rotation

            if (magnitude > peakMagnitude) {
                peakMagnitude = magnitude;
            }

            // Display X, Y and Z values

            tvX.setText(String.format(
                    Locale.US, "%.3f rad/s", xValue));

            tvY.setText(String.format(
                    Locale.US, "%.3f rad/s", yValue));

            tvZ.setText(String.format(
                    Locale.US, "%.3f rad/s", zValue));

            // Display degrees per second

            tvDegrees.setText(String.format(
                    Locale.US,
                    "X: %.2f°/s\nY: %.2f°/s\nZ: %.2f°/s",
                    xDegrees, yDegrees, zDegrees
            ));

            // Display magnitude

            tvMagnitude.setText(String.format(
                    Locale.US, "%.3f rad/s", magnitude));

            // Display peak value

            tvPeak.setText(String.format(
                    Locale.US, "%.3f rad/s", peakMagnitude));
        }
    }

    // Sensor accuracy callback

    @Override
    public void onAccuracyChanged(
            Sensor sensor, int accuracy) {

        // No additional action required
    }

    // Register sensor when app resumes

    @Override
    protected void onResume() {
        super.onResume();

        if (isMonitoring && gyroscope != null) {

            sensorManager.registerListener(
                    this,
                    gyroscope,
                    SensorManager.SENSOR_DELAY_UI
            );
        }
    }

    // Stop sensor when app is paused

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
    }
}
