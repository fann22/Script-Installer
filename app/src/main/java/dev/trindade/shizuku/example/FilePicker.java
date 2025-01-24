package dev.trindade.shizuku.example;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class FilePicker {

    public static final int PICK_FILE_REQUEST_CODE = 1;

    private final Context context;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    public FilePicker(Context context) {
        this.context = context;
        initializeFilePicker();
    }

    private void initializeFilePicker() {
        filePickerLauncher = ((AppCompatActivity) context).registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        handleFileSelection(uri);
                    }
                });
    }

    public void openFilePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/zip");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                filePickerLauncher.launch(intent);
            } else {
                ((AppCompatActivity) context).startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No file manager found", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleFileSelection(Uri uri) {
        if (uri != null) {
            FileProcessor fileProcessor = new FileProcessor(context);
            fileProcessor.extractZipFile(uri);
            Log.d("FilePicker", "File selected: " + uri.toString());
        }
    }
}