package com.pegalite.steptacker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pegalite.steptacker.databinding.ActivityMainBinding;

import java.text.DateFormatSymbols;
import java.util.Calendar;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private int stepCount = 0;
    private boolean isPeak = false;
    private float previousY = 0f;

    private static final float PEAK_THRESHOLD = 8.5f;
    private static final float SENSITIVITY_FACTOR = 0.1f;

    ActivityMainBinding binding;

    DatabaseReference databaseReference;

    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /* For Window Color Adjustments */
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.main_color));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.main_color));

        sharedPreferences = getSharedPreferences("app-data", MODE_PRIVATE);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        binding.startStop.setOnClickListener(v -> {
            if (isStarted) {
                pauseCounter();
            } else {
                startCounter();
            }
        });


        databaseReference = FirebaseDatabase.getInstance().getReference(sharedPreferences.getString("user", ""));

        initDays();

        initTotalCounting();
    }


    int total;

    private void initTotalCounting() {
        Calendar calendar = Calendar.getInstance();
        int dayIndex = calendar.get(Calendar.DAY_OF_WEEK);
        databaseReference.child("days").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                total = 0;
                int count = 1;
                for (DataSnapshot dataSnap : snapshot.getChildren()) {
                    int currentCount = dataSnap.getValue(Integer.class);
                    total += currentCount;
                    if (count == dayIndex) {
                        finalCount = currentCount;
                        binding.progressBar.setProgress(finalCount);
                        binding.heading.setText("You Have Completed " + finalCount + " Steps Today");
                        if (finalCount == 3000) {
                            binding.completed.setVisibility(View.VISIBLE);
                            binding.startStop.setVisibility(View.GONE);
                        }
                    }

                    ((ProgressBar) ((ViewGroup) binding.days.getChildAt(count - 1)).getChildAt(1)).setProgress(currentCount);
                    count++;
                }
                binding.totalCount.setText("Your total weekly steps is " + total);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void startCounter() {
        stepCount = 0;
        isStarted = true;
        if (binding.count.getText().equals("Click here to start")) {
            binding.count.setText("0");
        }
        binding.stop.setVisibility(View.VISIBLE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "Device not supported", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(this, "Step Counting stated", Toast.LENGTH_SHORT).show();
    }

    int finalCount = 0;

    private void pauseCounter() {
        updateStepInFirebase();
        binding.stop.setVisibility(View.GONE);
        finalCount += stepCount;
        binding.progressBar.setProgress(finalCount);
        binding.heading.setText("You Have Completed " + finalCount + " Steps Today");
        binding.count.setText("Click here to start");
        isStarted = false;
        sensorManager.unregisterListener(this);
        Toast.makeText(this, "Step Counting Stopped", Toast.LENGTH_SHORT).show();
    }


    private void updateStepInFirebase() {

        Calendar calendar = Calendar.getInstance();
        int dayIndex = calendar.get(Calendar.DAY_OF_WEEK);


        databaseReference.child("days").child("" + dayIndex).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int oldCounting = snapshot.getValue(Integer.class);
                databaseReference.child("days").child("" + dayIndex).setValue(oldCounting + stepCount);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    boolean isStarted = false;

    private void initDays() {

        for (int i = 0; i < binding.days.getChildCount(); i++) {
            int finalI = i;
            binding.days.getChildAt(i).setOnClickListener(v -> {
                showData(finalI + 1, ((ProgressBar) ((ViewGroup) binding.days.getChildAt(finalI)).getChildAt(1)).getProgress());
            });
        }
    }

    public void showData(int day, int steps) {
        DateFormatSymbols symbols = new DateFormatSymbols();
        String[] dayNames = symbols.getWeekdays();
        Intent intent = new Intent(MainActivity.this, StepTrackerActivity.class);
        intent.putExtra("progress", steps);
        intent.putExtra("day", dayNames[day]);
        startActivity(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float y = event.values[1];
        detectStep(y);
        previousY = y;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for step counting
    }

    private void detectStep(float currentY) {
        if (isPeak) {
            if (currentY > previousY && currentY >= (PEAK_THRESHOLD * SENSITIVITY_FACTOR)) {
                isPeak = false;
            }
        } else {
            if (currentY < previousY && currentY <= -(PEAK_THRESHOLD * SENSITIVITY_FACTOR)) {
                stepCount++;
                binding.count.setText(String.valueOf(stepCount));
                isPeak = true;
            }
        }
    }
}
