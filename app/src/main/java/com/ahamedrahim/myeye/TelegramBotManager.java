package com.ahamedrahim.myeye;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TelegramBotManager {
    
    private static final String TAG = "TelegramBotManager";
    private final Context context;
    private final OkHttpClient client;
    
    public TelegramBotManager(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
    }
    
    public interface TelegramCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    public void testConnection(final TelegramCallback callback) {
        SharedPreferences prefs = context.getSharedPreferences("TelegramPrefs", Context.MODE_PRIVATE);
        String botToken = prefs.getString("bot_token", "");
        String chatId = prefs.getString("chat_id", "");
        
        if (botToken.isEmpty() || chatId.isEmpty()) {
            callback.onError("Please enter bot token and chat ID first");
            return;
        }
        
        String message = "✅ My Eye App Connected Successfully!\n\n" +
                        "Device: " + android.os.Build.MODEL + "\n" +
                        "Time: " + java.time.LocalDateTime.now().toString() + "\n\n" +
                        "All monitoring services are now active.";
        
        sendMessage(message, new TelegramCallback() {
            @Override
            public void onSuccess(String response) {
                callback.onSuccess("✅ Connection successful! Test message sent.");
            }
            
            @Override
            public void onError(String error) {
                callback.onError("❌ Connection failed: " + error);
            }
        });
    }
    
    public void sendMessage(String text, final TelegramCallback callback) {
        SharedPreferences prefs = context.getSharedPreferences("TelegramPrefs", Context.MODE_PRIVATE);
        String botToken = prefs.getString("bot_token", "");
        String chatId = prefs.getString("chat_id", "");
        
        if (botToken.isEmpty() || chatId.isEmpty()) {
            callback.onError("Telegram credentials not set");
            return;
        }
        
        RequestBody formBody = new FormBody.Builder()
                .add("chat_id", chatId)
                .add("text", text)
                .add("parse_mode", "HTML")
                .build();
        
        Request request = new Request.Builder()
                .url("https://api.telegram.org/bot" + botToken + "/sendMessage")
                .post(formBody)
                .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess("Message sent successfully");
                } else {
                    callback.onError("Failed: " + response.code());
                }
                response.close();
            }
        });
    }
                                 }
