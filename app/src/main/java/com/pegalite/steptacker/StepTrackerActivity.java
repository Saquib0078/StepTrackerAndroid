package com.pegalite.steptacker;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.pegalite.steptacker.databinding.ActivityStepTrackerBinding;

public class StepTrackerActivity extends AppCompatActivity {


    ActivityStepTrackerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStepTrackerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /* For Window Color Adjustments */
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.main_color));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.main_color));

        if (getIntent().getIntExtra("progress", 0) == 0) {
            binding.progressBar.setProgress(1);
        } else {
            binding.progressBar.setProgress(getIntent().getIntExtra("progress", 0));
        }
        binding.count.setText(getIntent().getIntExtra("progress", 0) + " Steps");
        binding.day.setText(getIntent().getStringExtra("day") + "'s Activity");

    }


}