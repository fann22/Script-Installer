package dev.trindade.shizuku.example;

import static dev.trindade.shizuku.example.FilePicker.PICK_FILE_REQUEST_CODE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.content.pm.PackageManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import rikka.shizuku.Shizuku;

import dev.trindade.shizuku.example.databinding.ActivityMainBinding;

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

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri fileUri = data.getData();
            String filePath = filePicker.getPathFromUri(fileUri);

            // Cek apakah file path valid
            if (filePath != null) {
                Toast.makeText(this, "File Path: " + filePath, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Gagal mengambil file", Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        someLogic();
    }

    private void someLogic() {
        if (!Shizuku.pingBinder() || !isShizukuInstalled(this) || !checkShizukuPermission()) {
            // Arahkan ke layar Shizuku Not Activated
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
        /*Button status_button = findViewById(R.id.statusButton);
        Button request_button = findViewById(R.id.requestButton);
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            Execute.setClickable(true);
            status_button.setText("Shizuku access is granted!");
            request_button.setVisibility(View.GONE);
            status_button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00C800")));
            return true;
        } else {
            Execute.setClickable(false);
            status_button.setText("Shizuku access is not detected.\nHave you granted permission?\nClick here to request access");
            status_button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C80000")));
            //status_button.setTextColor(Color.parseColor("#ffffff"));
            request_button.setVisibility(View.VISIBLE);
            request_button.setOnClickListener(v -> Shizuku.requestPermission(MainActivity.SHIZUKU_REQUEST_CODE));
            return false;
        }*/
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