package com.ahamedrahim.myeye;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(TAG, "Device booted, starting My Eye services");
            
            SharedPreferences prefs = context.getSharedPreferences("TelegramPrefs", Context.MODE_PRIVATE);
            String botToken = prefs.getString("bot_token", "");
            String chatId = prefs.getString("chat_id", "");
            boolean autoStart = prefs.getBoolean("auto_sync", true);
            
            if (autoStart && !botToken.isEmpty() && !chatId.isEmpty()) {
                // Start foreground service
                Intent serviceIntent = new Intent(context, ForegroundService.class);
                serviceIntent.setAction("START_SMS_SERVICE");
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
                
                // Send notification to Telegram
                sendBootNotification(context, botToken, chatId);
            }
        }
    }
    
    private void sendBootNotification(Context context, String botToken, String chatId) {
        TelegramBotManager botManager = new TelegramBotManager(context);
        botManager.sendMessage("📱 My Eye App Started on Boot\n\n" +
                "Device: " + android.os.Build.MODEL + "\n" +
                "Boot Time: " + java.time.LocalDateTime.now() + "\n\n" +
                "Monitoring services are now active.", new TelegramBotManager.TelegramCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "Boot notification sent successfully");
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to send boot notification: " + error);
            }
        });
    }
}
