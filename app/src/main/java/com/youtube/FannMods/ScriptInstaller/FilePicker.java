package com.youtube.FannMods.ScriptInstaller;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

                        if (uri != null && isValidZipFile(uri) && isZipFileReadable(uri)) {
                            Log.d("FilePicker", "Valid ZIP file selected: " + uri.toString());
                            handleFileSelection(uri); // Proses file ZIP lebih lanjut
                        } else {
                            showInvalidFileDialog();
                            Toast.makeText(context, "Invalid ZIP file. Please select a valid .zip file.", Toast.LENGTH_SHORT).show();
                        }
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
            fileProcessor.processZipFile(uri);
            Log.d("FilePicker", "File selected: " + uri.toString());
        }
    }

    private boolean isValidZipFile(Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        String mimeType = contentResolver.getType(uri);

        // Periksa MIME type dan ekstensi file
        return "application/zip".equals(mimeType) || uri.toString().endsWith(".zip");
    }

    private boolean isZipFileReadable(Uri uri) {
        try {
            File tempZipFile = copyUriToTempFile(uri); // Salin URI ke file sementara
            ZipFile zipFile = new ZipFile(tempZipFile);
            zipFile.getFileHeaders(); // Periksa apakah file dapat dibaca sebagai ZIP
            tempZipFile.delete(); // Hapus file sementara
            return true;
        } catch (Exception e) {
            Log.e("FilePicker", "Invalid ZIP file: " + e.getMessage());
            return false;
        }
    }

    private File copyUriToTempFile(Uri uri) throws IOException {
        // Dapatkan resolver untuk membaca file dari URI
        ContentResolver resolver = context.getContentResolver();

        // Buka input stream dari URI
        InputStream inputStream = resolver.openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("Unable to open InputStream for URI: " + uri.toString());
        }

        // Buat file sementara di cache aplikasi
        File tempFile = File.createTempFile("temp_file", ".tmp",context.getCacheDir());

        // Gunakan FileOutputStream untuk menyalin data
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096]; // Buffer 4 KB
            int bytesRead;

            // Salin data dari inputStream ke outputStream
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } finally {
            // Tutup InputStream
            inputStream.close();
        }

        // Kembalikan file sementara
        return tempFile;
    }

    private void showInvalidFileDialog() {
        new AlertDialog.Builder(context)
                .setTitle("Invalid File")
                .setMessage("The selected file is not a valid ZIP file. Please try again.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}