package com.ahamedrahim.myeye;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private Switch switchMainService;
    private TextView tvSmsCount, tvMediaCount, tvLastSync, tvStatus;
    private CardView cardSettings, cardDeveloper;
    private Button btnStartService, btnStopService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        switchMainService = findViewById(R.id.switch_main_service);
        tvSmsCount = findViewById(R.id.tv_sms_count);
        tvMediaCount = findViewById(R.id.tv_media_count);
        tvLastSync = findViewById(R.id.tv_last_sync);
        tvStatus = findViewById(R.id.tv_status);
        cardSettings = findViewById(R.id.card_settings);
        cardDeveloper = findViewById(R.id.card_developer);
        btnStartService = findViewById(R.id.btn_start_service);
        btnStopService = findViewById(R.id.btn_stop_service);
        
        // Check permissions
        checkAndRequestPermissions();
        
        // Load statistics
        loadStatistics();
        
        // Update last sync time
        updateLastSyncTime();
        
        // Set up listeners
        switchMainService.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startMonitoringServices();
                updateStatus("🟢 Monitoring Active");
                Toast.makeText(MainActivity.this, "Monitoring started", Toast.LENGTH_SHORT).show();
            } else {
                stopMonitoringServices();
                updateStatus("🔴 Monitoring Stopped");
                Toast.makeText(MainActivity.this, "Monitoring stopped", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnStartService.setOnClickListener(v -> {
            switchMainService.setChecked(true);
            startMonitoringServices();
            updateStatus("🟢 Monitoring Active");
            Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
        });
        
        btnStopService.setOnClickListener(v -> {
            switchMainService.setChecked(false);
            stopMonitoringServices();
            updateStatus("🔴 Monitoring Stopped");
            Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
        });
        
        cardSettings.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });
        
        cardDeveloper.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, DeveloperProfileActivity.class));
        });
    }
    
    private void checkAndRequestPermissions() {
        String[] permissions;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.POST_NOTIFICATIONS
            };
        } else {
            permissions = new String[]{
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
        
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        
        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        } else {
            switchMainService.setChecked(true);
            startMonitoringServices();
            updateStatus("🟢 Monitoring Active");
        }
    }
    
    private void startMonitoringServices() {
        // Start foreground service
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.setAction("START_SMS_SERVICE");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        // Start media scanner
        Intent mediaIntent = new Intent(this, MediaScannerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(mediaIntent);
        } else {
            startService(mediaIntent);
        }
    }
    
    private void stopMonitoringServices() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
        
        Intent mediaIntent = new Intent(this, MediaScannerService.class);
        stopService(mediaIntent);
    }
    
    private void loadStatistics() {
        SharedPreferences prefs = getSharedPreferences("MyEyeStats", MODE_PRIVATE);
        int smsCount = prefs.getInt("sms_count", 0);
        int mediaCount = prefs.getInt("media_count", 0);
        
        tvSmsCount.setText(String.valueOf(smsCount));
        tvMediaCount.setText(String.valueOf(mediaCount));
    }
    
    private void updateLastSyncTime() {
        SharedPreferences prefs = getSharedPreferences("MyEyeStats", MODE_PRIVATE);
        long lastSync = prefs.getLong("last_sync", 0);
        
        if (lastSync > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String time = sdf.format(new Date(lastSync));
            tvLastSync.setText(time);
        } else {
            tvLastSync.setText("Never");
        }
    }
    
    private void updateStatus(String status) {
        tvStatus.setText(status);
        tvStatus.setTextColor(status.contains("🟢") ? 
            ContextCompat.getColor(this, R.color.green) : 
            ContextCompat.getColor(this, R.color.red));
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                switchMainService.setChecked(true);
                startMonitoringServices();
                updateStatus("🟢 Monitoring Active");
                Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
            } else {
                switchMainService.setChecked(false);
                updateStatus("🔴 Permissions Required");
                Toast.makeText(this, "Permissions required for app functionality!", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
        updateLastSyncTime();
    }
                                  }
