package com.example.weighttrackingapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Database Info
    private static final String DATABASE_NAME = "WeightTracker.db";
    private static final int DATABASE_VERSION = 3; // Increased version to add starting weight

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_WEIGHT_ENTRIES = "weight_entries";

    // Users Table Columns
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD_HASH = "password_hash";
    private static final String KEY_CURRENT_WEIGHT = "current_weight";
    private static final String KEY_GOAL_WEIGHT = "goal_weight";
    private static final String KEY_STARTING_WEIGHT = "starting_weight";
    private static final String KEY_CREATED_AT = "created_at";

    // Weight Entries Table Columns
    private static final String KEY_ENTRY_ID = "entry_id";
    private static final String KEY_WEIGHT = "weight";
    private static final String KEY_DATE = "date";
    private static final String KEY_TIME = "time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table with starting weight
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USERNAME + " TEXT UNIQUE NOT NULL,"
                + KEY_PASSWORD_HASH + " TEXT NOT NULL,"
                + KEY_CURRENT_WEIGHT + " REAL DEFAULT 0,"
                + KEY_GOAL_WEIGHT + " REAL DEFAULT 0,"
                + KEY_STARTING_WEIGHT + " REAL DEFAULT 0,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        // Create Weight Entries table
        String CREATE_WEIGHT_ENTRIES_TABLE = "CREATE TABLE " + TABLE_WEIGHT_ENTRIES + "("
                + KEY_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_ID + " INTEGER NOT NULL,"
                + KEY_WEIGHT + " REAL NOT NULL,"
                + KEY_DATE + " TEXT NOT NULL,"
                + KEY_TIME + " TEXT NOT NULL,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ")"
                + ")";

        try {
            db.execSQL(CREATE_USERS_TABLE);
            Log.d(TAG, "Users table created successfully");

            db.execSQL(CREATE_WEIGHT_ENTRIES_TABLE);
            Log.d(TAG, "Weight entries table created successfully");

            Log.d(TAG, "Database tables created");
        } catch (SQLiteException e) {
            Log.e(TAG, "Error creating database tables: ", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Add starting_weight column to existing users table
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + KEY_STARTING_WEIGHT + " REAL DEFAULT 0");
                Log.d(TAG, "Added starting_weight column to users table");
            } catch (SQLiteException e) {
                Log.e(TAG, "Error adding starting_weight column: ", e);
                // If alter fails, recreate tables
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHT_ENTRIES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
                onCreate(db);
            }
        }
    }

    // ========================================
    // USER MANAGEMENT METHODS
    // ========================================

    /**
     * Create a new user account
     */
    public long createUser(String username, String password) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_USERNAME, username);
            values.put(KEY_PASSWORD_HASH, hashPassword(password));
            values.put(KEY_CURRENT_WEIGHT, 0.0);
            values.put(KEY_GOAL_WEIGHT, 0.0);
            values.put(KEY_STARTING_WEIGHT, 0.0);

            long userId = db.insert(TABLE_USERS, null, values);
            Log.d(TAG, "User created with ID: " + userId);
            return userId;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error creating user: ", e);
            return -1;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Check if username already exists
     */
    public boolean userExists(String username) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();

            String selectQuery = "SELECT " + KEY_USER_ID + " FROM " + TABLE_USERS
                    + " WHERE " + KEY_USERNAME + " = ?";

            cursor = db.rawQuery(selectQuery, new String[]{username});
            boolean exists = cursor.getCount() > 0;
            Log.d(TAG, "User exists check for '" + username + "': " + exists);
            return exists;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error checking if user exists: ", e);
            return false;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * Validate user credentials
     */
    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();

            String selectQuery = "SELECT " + KEY_PASSWORD_HASH + " FROM " + TABLE_USERS
                    + " WHERE " + KEY_USERNAME + " = ?";

            cursor = db.rawQuery(selectQuery, new String[]{username});

            if (cursor.moveToFirst()) {
                String storedHash = cursor.getString(0);
                String inputHash = hashPassword(password);
                boolean isValid = storedHash.equals(inputHash);
                Log.d(TAG, "User validation for '" + username + "': " + isValid);
                return isValid;
            }

            Log.d(TAG, "User not found during validation: " + username);
            return false;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error validating user: ", e);
            return false;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * Get user ID by username
     */
    public int getUserId(String username) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();

            String selectQuery = "SELECT " + KEY_USER_ID + " FROM " + TABLE_USERS
                    + " WHERE " + KEY_USERNAME + " = ?";

            cursor = db.rawQuery(selectQuery, new String[]{username});

            if (cursor.moveToFirst()) {
                int userId = cursor.getInt(0);
                Log.d(TAG, "Found user ID " + userId + " for username: " + username);
                return userId;
            }

            Log.e(TAG, "No user found with username: " + username);
            return -1;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error getting user ID: ", e);
            return -1;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * Get user's current weight
     */
    public double getCurrentWeight(String username) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();

            String selectQuery = "SELECT " + KEY_CURRENT_WEIGHT + " FROM " + TABLE_USERS
                    + " WHERE " + KEY_USERNAME + " = ?";

            cursor = db.rawQuery(selectQuery, new String[]{username});

            if (cursor.moveToFirst()) {
                double weight = cursor.getDouble(0);
                Log.d(TAG, "Current weight for " + username + ": " + weight);
                return weight;
            }

            Log.e(TAG, "No user found when getting current weight: " + username);
            return 0.0;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error getting current weight: ", e);
            return 0.0;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * Get user's goal weight
     */
    public double getGoalWeight(String username) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();

            String selectQuery = "SELECT " + KEY_GOAL_WEIGHT + " FROM " + TABLE_USERS
                    + " WHERE " + KEY_USERNAME + " = ?";

            cursor = db.rawQuery(selectQuery, new String[]{username});

            if (cursor.moveToFirst()) {
                double weight = cursor.getDouble(0);
                Log.d(TAG, "Goal weight for " + username + ": " + weight);
                return weight;
            }

            Log.e(TAG, "No user found when getting goal weight: " + username);
            return 0.0;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error getting goal weight: ", e);
            return 0.0;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * Get user's starting weight
     */
    public double getStartingWeight(String username) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();

            String selectQuery = "SELECT " + KEY_STARTING_WEIGHT + " FROM " + TABLE_USERS
                    + " WHERE " + KEY_USERNAME + " = ?";

            cursor = db.rawQuery(selectQuery, new String[]{username});

            if (cursor.moveToFirst()) {
                double weight = cursor.getDouble(0);
                Log.d(TAG, "Starting weight for " + username + ": " + weight);
                return weight;
            }

            Log.e(TAG, "No user found when getting starting weight: " + username);
            return 0.0;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error getting starting weight: ", e);
            return 0.0;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * Update user's current weight
     */
    public boolean updateCurrentWeight(String username, double weight) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_CURRENT_WEIGHT, weight);

            int rowsAffected = db.update(TABLE_USERS, values,
                    KEY_USERNAME + " = ?", new String[]{username});

            boolean success = rowsAffected > 0;
            Log.d(TAG, "Update current weight for " + username + " to " + weight + ": " + success);
            return success;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error updating current weight: ", e);
            return false;
        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * Update user's goal weight
     */
    public boolean updateGoalWeight(String username, double weight) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_GOAL_WEIGHT, weight);

            int rowsAffected = db.update(TABLE_USERS, values,
                    KEY_USERNAME + " = ?", new String[]{username});

            boolean success = rowsAffected > 0;
            Log.d(TAG, "Update goal weight for " + username + " to " + weight + ": " + success);
            return success;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error updating goal weight: ", e);
            return false;
        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * Update user's starting weight
     */
    public boolean updateStartingWeight(String username, double weight) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_STARTING_WEIGHT, weight);

            int rowsAffected = db.update(TABLE_USERS, values,
                    KEY_USERNAME + " = ?", new String[]{username});

            boolean success = rowsAffected > 0;
            Log.d(TAG, "Update starting weight for " + username + " to " + weight + ": " + success);
            return success;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error updating starting weight: ", e);
            return false;
        } finally {
            if (db != null) db.close();
        }
    }

    // ========================================
    // USER DELETION METHODS
    // ========================================

    /**
     * Delete all weight entries for a specific user
     */
    public boolean deleteAllUserEntries(int userId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            int rowsAffected = db.delete(TABLE_WEIGHT_ENTRIES,
                    KEY_USER_ID + " = ?", new String[]{String.valueOf(userId)});

            boolean success = rowsAffected >= 0; // >= 0 because user might have 0 entries
            Log.d(TAG, "Delete all entries for user " + userId + ": " + success + " (" + rowsAffected + " entries deleted)");
            return success;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error deleting all user entries: ", e);
            return false;
        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * Delete a user account and all associated data
     */
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            // First delete all weight entries for this user
            int entriesDeleted = db.delete(TABLE_WEIGHT_ENTRIES,
                    KEY_USER_ID + " = ?", new String[]{String.valueOf(userId)});
            Log.d(TAG, "Weight entries deletion result for user " + userId + ": " + entriesDeleted + " entries deleted");

            // Then delete the user record
            int rowsAffected = db.delete(TABLE_USERS,
                    KEY_USER_ID + " = ?", new String[]{String.valueOf(userId)});

            boolean success = rowsAffected > 0;
            Log.d(TAG, "Delete user " + userId + ": " + success);
            return success;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error deleting user: ", e);
            return false;
        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * Delete a user account by username
     */
    public boolean deleteUserByUsername(String username) {
        try {
            int userId = getUserId(username);
            if (userId == -1) {
                Log.e(TAG, "Cannot delete user - user not found: " + username);
                return false;
            }

            return deleteUser(userId);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting user by username: ", e);
            return false;
        }
    }

    /**
     * Check if user has any weight entries
     */
    public boolean userHasEntries(String username) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            // Get user ID
            int userId = getUserId(username);
            if (userId == -1) {
                return false;
            }

            String selectQuery = "SELECT COUNT(*) FROM " + TABLE_WEIGHT_ENTRIES
                    + " WHERE " + KEY_USER_ID + " = ?";

            cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});

            int count = 0;
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }

            boolean hasEntries = count > 0;
            Log.d(TAG, "User " + username + " has entries: " + hasEntries + " (" + count + " entries)");
            return hasEntries;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error checking if user has entries: ", e);
            return false;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    // ========================================
    // WEIGHT ENTRIES METHODS
    // ========================================

    /**
     * Add a new weight entry
     */
    public long addWeightEntry(WeightEntry entry) {
        SQLiteDatabase db = null;
        try {
            Log.d(TAG, "Attempting to add weight entry: " + entry.toString());

            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_USER_ID, entry.getUserId());
            values.put(KEY_WEIGHT, entry.getWeight());
            values.put(KEY_DATE, entry.getDate());
            values.put(KEY_TIME, entry.getTime());

            Log.d(TAG, "ContentValues: " + values.toString());

            long entryId = db.insert(TABLE_WEIGHT_ENTRIES, null, values);

            if (entryId == -1) {
                Log.e(TAG, "Database insert failed - returned -1");
            } else {
                Log.d(TAG, "Weight entry added successfully with ID: " + entryId);
            }

            return entryId;
        } catch (SQLiteException e) {
            Log.e(TAG, "SQLite error adding weight entry: ", e);
            return -1;
        } catch (Exception e) {
            Log.e(TAG, "General error adding weight entry: ", e);
            return -1;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Update current weight with the most recent entry by actual date/time
     */
    public boolean updateCurrentWeightFromMostRecent(String username) {
        WeightEntry mostRecentEntry = getMostRecentWeightEntryByActualDate(username);
        if (mostRecentEntry != null) {
            boolean updated = updateCurrentWeight(username, mostRecentEntry.getWeight());
            Log.d(TAG, "Updated current weight from most recent entry: " + mostRecentEntry.getWeight() + "kg");
            return updated;
        } else {
            Log.d(TAG, "No entries found to update current weight from");
            return false;
        }
    }

    /**
     * Get the most recent weight entry by actual date/time
     */
    public WeightEntry getMostRecentWeightEntryByActualDate(String username) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            // Get user ID
            String userQuery = "SELECT " + KEY_USER_ID + " FROM " + TABLE_USERS + " WHERE " + KEY_USERNAME + " = ?";
            cursor = db.rawQuery(userQuery, new String[]{username});

            if (!cursor.moveToFirst()) {
                Log.e(TAG, "Cannot get recent entry - user not found: " + username);
                return null;
            }

            int userId = cursor.getInt(0);
            cursor.close();

            // Order by actual date and time, not created_at
            String selectQuery = "SELECT * FROM " + TABLE_WEIGHT_ENTRIES
                    + " WHERE " + KEY_USER_ID + " = ?"
                    + " ORDER BY " + KEY_DATE + " DESC, " + KEY_TIME + " DESC LIMIT 1";

            cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});

            WeightEntry entry = null;
            if (cursor.moveToFirst()) {
                entry = new WeightEntry(
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ENTRY_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ID)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_WEIGHT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME)),
                        ""
                );
                Log.d(TAG, "Found most recent entry by actual date for " + username + ": " + entry.getWeight() + "kg on " + entry.getDate() + " at " + entry.getTime());
            } else {
                Log.d(TAG, "No entries found for " + username);
            }

            return entry;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error getting most recent weight entry by actual date: ", e);
            return null;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * Get recent weight entries for a user (ordered by actual date/time)
     */
    public List<WeightEntry> getRecentWeightEntries(String username, int limit) {
        List<WeightEntry> entries = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            // Get user ID
            String userQuery = "SELECT " + KEY_USER_ID + " FROM " + TABLE_USERS + " WHERE " + KEY_USERNAME + " = ?";
            cursor = db.rawQuery(userQuery, new String[]{username});

            if (!cursor.moveToFirst()) {
                Log.e(TAG, "Cannot get entries - user not found: " + username);
                return entries;
            }

            int userId = cursor.getInt(0);
            cursor.close();

            // Order by actual date and time, not created_at
            String selectQuery = "SELECT * FROM " + TABLE_WEIGHT_ENTRIES
                    + " WHERE " + KEY_USER_ID + " = ?"
                    + " ORDER BY " + KEY_DATE + " DESC, " + KEY_TIME + " DESC"
                    + " LIMIT " + limit;

            cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});

            if (cursor.moveToFirst()) {
                do {
                    WeightEntry entry = new WeightEntry(
                            cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ENTRY_ID)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ID)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_WEIGHT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME)),
                            ""
                    );
                    entries.add(entry);
                } while (cursor.moveToNext());
            }

            Log.d(TAG, "Retrieved " + entries.size() + " recent entries for " + username);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error getting recent weight entries: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return entries;
    }

    /**
     * Get all weight entries for a user (ordered by actual date/time)
     */
    public List<WeightEntry> getAllWeightEntries(String username) {
        List<WeightEntry> entries = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            // Get user ID
            String userQuery = "SELECT " + KEY_USER_ID + " FROM " + TABLE_USERS + " WHERE " + KEY_USERNAME + " = ?";
            cursor = db.rawQuery(userQuery, new String[]{username});

            if (!cursor.moveToFirst()) {
                Log.e(TAG, "Cannot get all entries - user not found: " + username);
                return entries;
            }

            int userId = cursor.getInt(0);
            cursor.close();

            // Order by actual date and time, not created_at
            String selectQuery = "SELECT * FROM " + TABLE_WEIGHT_ENTRIES
                    + " WHERE " + KEY_USER_ID + " = ?"
                    + " ORDER BY " + KEY_DATE + " DESC, " + KEY_TIME + " DESC";

            cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});

            if (cursor.moveToFirst()) {
                do {
                    WeightEntry entry = new WeightEntry(
                            cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ENTRY_ID)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ID)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_WEIGHT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME)),
                            ""
                    );
                    entries.add(entry);
                } while (cursor.moveToNext());
            }

            Log.d(TAG, "Retrieved " + entries.size() + " total entries for " + username);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error getting all weight entries: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return entries;
    }

    /**
     * Get the most recent weight entry for a user
     */
    public WeightEntry getMostRecentWeightEntry(String username) {
        return getMostRecentWeightEntryByActualDate(username);
    }

    /**
     * Update a weight entry
     */
    public boolean updateWeightEntry(int entryId, double weight, String date, String time) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_WEIGHT, weight);
            values.put(KEY_DATE, date);
            values.put(KEY_TIME, time);

            int rowsAffected = db.update(TABLE_WEIGHT_ENTRIES, values,
                    KEY_ENTRY_ID + " = ?", new String[]{String.valueOf(entryId)});

            boolean success = rowsAffected > 0;
            Log.d(TAG, "Update entry " + entryId + " to weight " + weight + " on " + date + " at " + time + ": " + success);
            return success;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error updating weight entry: ", e);
            return false;
        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * Delete a weight entry
     */
    public boolean deleteWeightEntry(int entryId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            int rowsAffected = db.delete(TABLE_WEIGHT_ENTRIES,
                    KEY_ENTRY_ID + " = ?", new String[]{String.valueOf(entryId)});

            boolean success = rowsAffected > 0;
            Log.d(TAG, "Delete entry " + entryId + ": " + success);
            return success;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error deleting weight entry: ", e);
            return false;
        } finally {
            if (db != null) db.close();
        }
    }

    /**
     * Get total number of entries for a user
     */
    public int getEntryCount(String username) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            // Get user ID within
            String userQuery = "SELECT " + KEY_USER_ID + " FROM " + TABLE_USERS + " WHERE " + KEY_USERNAME + " = ?";
            cursor = db.rawQuery(userQuery, new String[]{username});

            if (!cursor.moveToFirst()) {
                Log.e(TAG, "Cannot get entry count - user not found: " + username);
                return 0;
            }

            int userId = cursor.getInt(0);
            cursor.close();

            String selectQuery = "SELECT COUNT(*) FROM " + TABLE_WEIGHT_ENTRIES
                    + " WHERE " + KEY_USER_ID + " = ?";

            cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});

            int count = 0;
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }

            Log.d(TAG, "Entry count for " + username + ": " + count);
            return count;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error getting entry count: ", e);
            return 0;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * Hash password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error hashing password", e);
            return password;
        }
    }

    /**
     * Get total number of users (for testing)
     */
    public int getUserCount() {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);

            int count = 0;
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }

            Log.d(TAG, "Total user count: " + count);
            return count;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error getting user count: ", e);
            return 0;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }
}