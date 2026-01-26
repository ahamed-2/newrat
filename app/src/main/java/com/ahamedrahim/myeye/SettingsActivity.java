package com.ahamedrahim.myeye;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    
    private EditText etBotToken, etChatId;
    private Switch switchAutoSync, switchNotifySMS, switchNotifyMedia;
    private Button btnSave, btnTest, btnClearData;
    private TextView tvStatus;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        // Initialize views
        etBotToken = findViewById(R.id.et_bot_token);
        etChatId = findViewById(R.id.et_chat_id);
        switchAutoSync = findViewById(R.id.switch_auto_sync);
        switchNotifySMS = findViewById(R.id.switch_notify_sms);
        switchNotifyMedia = findViewById(R.id.switch_notify_media);
        btnSave = findViewById(R.id.btn_save);
        btnTest = findViewById(R.id.btn_test);
        btnClearData = findViewById(R.id.btn_clear_data);
        tvStatus = findViewById(R.id.tv_status);
        
        // Load saved settings
        loadSettings();
        
        // Set button listeners
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
        
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testTelegramConnection();
            }
        });
        
        btnClearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllData();
            }
        });
    }
    
    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("TelegramPrefs", MODE_PRIVATE);
        
        etBotToken.setText(prefs.getString("bot_token", ""));
        etChatId.setText(prefs.getString("chat_id", ""));
        switchAutoSync.setChecked(prefs.getBoolean("auto_sync", true));
        switchNotifySMS.setChecked(prefs.getBoolean("notify_sms", true));
        switchNotifyMedia.setChecked(prefs.getBoolean("notify_media", true));
    }
    
    private void saveSettings() {
        String botToken = etBotToken.getText().toString().trim();
        String chatId = etChatId.getText().toString().trim();
        
        if (botToken.isEmpty() || chatId.isEmpty()) {
            Toast.makeText(this, "Please enter both Bot Token and Chat ID", Toast.LENGTH_SHORT).show();
            return;
        }
        
        SharedPreferences prefs = getSharedPreferences("TelegramPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putString("bot_token", botToken);
        editor.putString("chat_id", chatId);
        editor.putBoolean("auto_sync", switchAutoSync.isChecked());
        editor.putBoolean("notify_sms", switchNotifySMS.isChecked());
        editor.putBoolean("notify_media", switchNotifyMedia.isChecked());
        
        if (editor.commit()) {
            tvStatus.setText("✅ Settings saved successfully!");
            tvStatus.setTextColor(getColor(R.color.green));
            Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show();
        } else {
            tvStatus.setText("❌ Failed to save settings");
            tvStatus.setTextColor(getColor(R.color.red));
            Toast.makeText(this, "Failed to save settings", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void testTelegramConnection() {
        String botToken = etBotToken.getText().toString().trim();
        String chatId = etChatId.getText().toString().trim();
        
        if (botToken.isEmpty() || chatId.isEmpty()) {
            Toast.makeText(this, "Please enter both Bot Token and Chat ID", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Save temporarily for test
        SharedPreferences prefs = getSharedPreferences("TelegramPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("bot_token", botToken);
        editor.putString("chat_id", chatId);
        editor.apply();
        
        // Test connection
        TelegramBotManager botManager = new TelegramBotManager(this);
        botManager.testConnection(new TelegramBotManager.TelegramCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText("✅ " + message);
                        tvStatus.setTextColor(getColor(R.color.green));
                        Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText("❌ " + error);
                        tvStatus.setTextColor(getColor(R.color.red));
                        Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    
    private void clearAllData() {
        // Clear all SharedPreferences
        getSharedPreferences("TelegramPrefs", MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("MyEyeStats", MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("UploadedFiles", MODE_PRIVATE).edit().clear().apply();
        
        // Clear database
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.clearAllData();
        dbHelper.close();
        
        // Reset UI
        etBotToken.setText("");
        etChatId.setText("");
        switchAutoSync.setChecked(true);
        switchNotifySMS.setChecked(true);
        switchNotifyMedia.setChecked(true);
        
        tvStatus.setText("✅ All data cleared successfully");
        tvStatus.setTextColor(getColor(R.color.green));
        Toast.makeText(this, "All data has been cleared", Toast.LENGTH_SHORT).show();
    }
                    }
