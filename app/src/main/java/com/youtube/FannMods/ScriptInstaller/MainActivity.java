package com.youtube.FannMods.ScriptInstaller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.content.pm.PackageManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import rikka.shizuku.Shizuku;

import com.youtube.FannMods.ScriptInstaller.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    public static final int SHIZUKU_REQUEST_CODE = 258;
    private FilePicker filePicker;
    private final Shizuku.OnBinderDeadListener BINDER_DEAD_LISTENER = this::someLogic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        someLogic();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        Shizuku.addBinderDeadListener(BINDER_DEAD_LISTENER);

        binding.execute.setOnClickListener(v -> runShellCommand());
        filePicker = new FilePicker(this);
        binding.pickerButton.setOnClickListener(v -> filePicker.openFilePicker());
    }

    @Override
    protected void onResume() {
        super.onResume();
        someLogic();
    }

    private void someLogic() {
        if (!Shizuku.pingBinder() || !isShizukuInstalled(this) || !checkShizukuPermission()) {
            startActivity(new Intent(this, MainActivlty.class));
            finish();
        }
    }

    public static boolean isShizukuInstalled(Context context) {
        try {
            context.getPackageManager().getPackageInfo("moe.shizuku.privileged.api", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean checkShizukuPermission() {
        if (Shizuku.isPreV11()) {
            return false;
        }
        return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
    }

    private void runShellCommand() {
        try {
            String[] command = {"ls", "/sdcard/Android/data/com.mobile.legends"};
            Process process = Shizuku.newProcess(command, null, null);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            Log.d("Shizuku", "Output: " + output.toString().trim());
            process.waitFor();
        } catch (Exception e) {
            Log.e("Shizuku", "Error running shell command", e);
        }
    }
}