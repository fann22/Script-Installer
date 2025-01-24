package dev.trindade.shizuku.example;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileProcessor {

    private final Context context;

    public FileProcessor(Context context) {
        this.context = context;
    }

    public void extractZipFile(Uri uri) {
        try {
            InputStream inputStream = getInputStreamFromUri(uri);

            if (inputStream != null) {
                extractZipToFolder(inputStream, new File(context.getFilesDir(), "script_pool"));
            }
        } catch (Exception e) {
            Log.e("FileProcessor", "Error extracting ZIP file", e);
        }
    }

    private InputStream getInputStreamFromUri(Uri uri) throws IOException {
        ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.openInputStream(uri);
    }

    private void extractZipToFolder(InputStream inputStream, File destinationFolder) {
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                File outputFile = new File(destinationFolder, zipEntry.getName());

                if (zipEntry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    extractFile(zipInputStream, outputFile);
                }

                zipInputStream.closeEntry();
            }
        } catch (IOException e) {
            Log.e("FileProcessor", "Error extracting ZIP file", e);
        }
    }

    private void extractFile(ZipInputStream zipInputStream, File outputFile) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = zipInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }
        }
    }
}