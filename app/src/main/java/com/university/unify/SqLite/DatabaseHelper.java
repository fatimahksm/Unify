package com.university.unify.SqLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "unify.db";
    private static final int DATABASE_VERSION = 8;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user_session");
        db.execSQL("DROP TABLE IF EXISTS chatbot_messages");
        createTables(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS user_session (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id TEXT, " +
                "firebase_uid TEXT, " +
                "full_name TEXT, " +
                "email TEXT, " +
                "role TEXT, " +
                "faculty_id TEXT, " +
                "major_id TEXT, " +
                "study_year TEXT, " +
                "profile_image_url TEXT, " +    // NEW
                "isApproved TEXT" +
                ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS chatbot_messages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id TEXT, " +
                "message TEXT, " +
                "sender_type TEXT, " +
                "created_at INTEGER" +
                ")");
    }

    // ===== user_session =====

    public void saveLoggedInUser(String userId, String firebaseUid, String fullName,
                                 String email, String role, String facultyId,
                                 String majorId, String studyYear, String isApproved) {
        // back-compat overload — keeps existing callers working (no profile image)
        saveLoggedInUser(userId, firebaseUid, fullName, email, role,
                facultyId, majorId, studyYear, "", isApproved);
    }

    public void saveLoggedInUser(String userId, String firebaseUid, String fullName,
                                 String email, String role, String facultyId,
                                 String majorId, String studyYear,
                                 String profileImageUrl, String isApproved) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("user_session", null, null);

        ContentValues values = new ContentValues();
        values.put("user_id",           userId);
        values.put("firebase_uid",      firebaseUid);
        values.put("full_name",         fullName);
        values.put("email",             email);
        values.put("role",              role);
        values.put("faculty_id",        facultyId);
        values.put("major_id",          majorId);
        values.put("study_year",        studyYear);
        values.put("profile_image_url", profileImageUrl);
        values.put("isApproved",        isApproved);

        db.insert("user_session", null, values);
        db.close();
    }

    public void saveLoggedInUser(String email, String role, String isApproved) {
        saveLoggedInUser("", "", "", email, role, "", "", "", "", isApproved);
    }

    public boolean hasLoggedInUser() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM user_session LIMIT 1", null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    public String getLoggedInUserId()          { return getValue("user_id");           }
    public String getLoggedInFirebaseUid()     { return getValue("firebase_uid");      }
    public String getLoggedInFullName()        { return getValue("full_name");         }
    public String getLoggedInEmail()           { return getValue("email");             }
    public String getLoggedInRole()            { return getValue("role");              }
    public String getLoggedInFacultyId()       { return getValue("faculty_id");        }
    public String getLoggedInMajorId()         { return getValue("major_id");          }
    public String getLoggedInStudyYear()       { return getValue("study_year");        }
    public String getLoggedInProfileImageUrl() { return getValue("profile_image_url"); }
    public String getLoggedInApproval()        { return getValue("isApproved");        }
    public String getLoggedInIsApproved()      { return getValue("isApproved");        }

    public void clearLoggedInUser() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("user_session", null, null);
        db.close();
    }

    private String getValue(String columnName) {
        SQLiteDatabase db = getReadableDatabase();
        String value = "";
        try {
            Cursor cursor = db.rawQuery(
                    "SELECT " + columnName + " FROM user_session LIMIT 1", null);
            if (cursor.moveToFirst()) {
                String v = cursor.getString(0);
                if (v != null) value = v;
            }
            cursor.close();
        } catch (Exception e) {
            // column missing on this device — return "" instead of crashing
        }
        db.close();
        return value;
    }

    // ===== chatbot_messages =====

    public void saveChatBotMessage(String userId, String message, String senderType) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id",     userId);
        values.put("message",     message);
        values.put("sender_type", senderType);
        values.put("created_at",  System.currentTimeMillis());
        db.insert("chatbot_messages", null, values);
        db.close();
    }

    public Cursor getChatBotMessages(String userId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT message, sender_type FROM chatbot_messages " +
                        "WHERE user_id = ? ORDER BY created_at ASC",
                new String[]{ userId });
    }

    public void clearChatBotMessages(String userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("chatbot_messages", "user_id = ?", new String[]{ userId });
        db.close();
    }
}
