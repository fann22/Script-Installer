package com.youtube.FannMods.ScriptInstaller;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import rikka.shizuku.Shizuku;

public class MainActivlty extends AppCompatActivity {
    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER = this::onRequestPermissionsResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shizuku_not_activated);

        Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
        TextView message = findViewById(R.id.tv_message);
        Button retryButton = findViewById(R.id.btn_retry);
        var ref = new Object() {
            String msg = "";
        };
        if (!MainActivity.isShizukuInstalled(this)) {
            ref.msg = "Shizuku is not installed";
        } else if (!Shizuku.pingBinder()) {
            ref.msg = "Shizuku service is not activated";
        } else if (!MainActivity.checkShizukuPermission()) {
            ref.msg = "Shizuku access is not detected.\nHave you granted the permission?";
            retryButton.setText("Request access");
        }
        message.setText(ref.msg);
        retryButton.setOnClickListener(v -> {
            if (Shizuku.pingBinder() && MainActivity.isShizukuInstalled(this) && MainActivity.checkShizukuPermission()) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else if (!MainActivity.checkShizukuPermission()) {
                Shizuku.requestPermission(MainActivity.SHIZUKU_REQUEST_CODE);
            } else {
                Toast.makeText(this, ref.msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onRequestPermissionsResult(int requestCode, int grantResult) {
        if (requestCode == MainActivity.SHIZUKU_REQUEST_CODE) {
            boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }
    }
}
