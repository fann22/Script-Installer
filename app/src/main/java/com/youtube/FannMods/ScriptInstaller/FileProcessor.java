package com.youtube.FannMods.ScriptInstaller;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FileProcessor {

    private final Context context;

    public FileProcessor(Context context) {
        this.context = context;
    }

    // Metode utama untuk mengekstrak file ZIP
    public void processZipFile(Uri uri) {
        try {
            // Salin file ZIP ke penyimpanan internal sementara
            File tempZipFile = copyUriToTempFile(uri);

            ZipFile zipFile = new ZipFile(tempZipFile);

            if (zipFile.isEncrypted()) {
                // Tampilkan dialog untuk meminta password jika ZIP terenkripsi
                askForPassword(zipFile, tempZipFile);
            } else {
                // Langsung ekstrak jika tidak memerlukan password
                extractZipFile(zipFile);
            }
        } catch (Exception e) {
            Log.e("FileProcessor", "Error processing ZIP file", e);
            Toast.makeText(context, "Failed to process ZIP file", Toast.LENGTH_SHORT).show();
        }
    }

    // Meminta password menggunakan dialog
    private void askForPassword(ZipFile zipFile, File tempZipFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Password Required");
        builder.setMessage("This ZIP file is password-protected. Please enter the password:");

        final EditText input = new EditText(context);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String password = input.getText().toString();
            try {
                zipFile.setPassword(password.toCharArray());
                extractZipFile(zipFile);
                tempZipFile.delete(); // Hapus file sementara setelah selesai
            } catch (Exception e) {
                Log.e("FileProcessor", "Invalid password", e);
                Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            tempZipFile.delete(); // Hapus file sementara jika pengguna membatalkan
        });

        builder.show();
    }

    // Mengekstrak file ZIP ke folder script_pool
    private void extractZipFile(ZipFile zipFile) {
        try {
            File destinationFolder = new File(context.getFilesDir(), "script_pool");

            if (!destinationFolder.exists()) {
                destinationFolder.mkdirs();
            }

            zipFile.extractAll(destinationFolder.getAbsolutePath());

            // Log nama file yang diekstrak
            List<FileHeader> fileHeaders = zipFile.getFileHeaders();
            for (FileHeader fileHeader : fileHeaders) {
                String[] path = fileHeader.getFileName().split("/");
                String fixedName = path[path.length - 1];
                Log.d("FileProcessor", "Extracted: " + fixedName);
            }

            Toast.makeText(context, "ZIP file extracted successfully", Toast.LENGTH_SHORT).show();
        } catch (ZipException e) {
            Log.e("FileProcessor", "Error extracting ZIP file", e);
            Toast.makeText(context, "Failed to extract ZIP file", Toast.LENGTH_SHORT).show();
        }
    }

    // Mendapatkan semua nama file dalam ZIP
    public List<String> getFileNamesFromZip(Uri uri) {
        List<String> fileNames = new ArrayList<>();
        try {
            File tempZipFile = copyUriToTempFile(uri);

            ZipFile zipFile = new ZipFile(tempZipFile);

            if (zipFile.isEncrypted()) {
                Log.w("FileProcessor", "ZIP is encrypted. Cannot read file names without password.");
                return fileNames;
            }

            List<FileHeader> fileHeaders = zipFile.getFileHeaders();
            for (FileHeader fileHeader : fileHeaders) {
                fileNames.add(fileHeader.getFileName());
            }

            tempZipFile.delete(); // Hapus file sementara setelah selesai
        } catch (Exception e) {
            Log.e("FileProcessor", "Error reading ZIP file entries", e);
        }

        return fileNames;
    }

    // Salin file dari URI ke file sementara di penyimpanan internal
    private File copyUriToTempFile(Uri uri) throws Exception {
        ContentResolver resolver = context.getContentResolver();
        InputStream inputStream = resolver.openInputStream(uri);
        File tempFile = File.createTempFile("temp_zip", ".zip", context.getCacheDir());

        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }

        return tempFile;
    }
}
