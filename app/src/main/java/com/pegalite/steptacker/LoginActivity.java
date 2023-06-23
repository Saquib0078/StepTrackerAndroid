package com.pegalite.steptacker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pegalite.steptacker.databinding.ActivityLoginBinding;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;

    DatabaseReference databaseReference;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("app-data", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if (sharedPreferences.getString("user", "null").equals("null")) {

            databaseReference = FirebaseDatabase.getInstance().getReference();

            binding.confirm.setOnClickListener(v -> {

                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Loading...");
                progressDialog.setMessage("Please wait while we are processing");
                progressDialog.setCancelable(false);
                progressDialog.show();

                String user = binding.username.getText().toString();
                String pass = binding.password.getText().toString();

                Map<String, Object> passMap = new HashMap<>();
                Map<String, Object> daysMap = new HashMap<>();
                daysMap.put("1", 0);
                daysMap.put("2", 0);
                daysMap.put("3", 0);
                daysMap.put("4", 0);
                daysMap.put("5", 0);
                daysMap.put("6", 0);
                daysMap.put("7", 0);
                passMap.put("pass", pass);
                passMap.put("days", daysMap);


                databaseReference.child(user).setValue(passMap).addOnSuccessListener(unused -> {


                    editor.putString("user", user);
                    editor.commit();

                    progressDialog.dismiss();

                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
            });
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

    }
}