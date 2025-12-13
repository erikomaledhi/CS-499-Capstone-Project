package com.example.weighttrackingapp;

import android.content.Context;
import android.util.Log;

import com.example.weighttrackingapp.models.UserRealm;
import com.example.weighttrackingapp.models.WeightEntryRealm;

import org.mindrot.jbcrypt.BCrypt;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmList;

public class DatabaseHelper {

    private static final String TAG = "DatabaseHelper";
    private static final int BCRYPT_COST = 12; // BCrypt work factor
    
    private Context context;

    public DatabaseHelper(Context context) {
        this.context = context;
        Log.d(TAG, "DatabaseHelper initialized with Realm and BCrypt");
    }

    // ========================================
    // USER MANAGEMENT METHODS
    // ========================================

    /**
     * Create a new user account with BCrypt password hashing
     */
    public long createUser(String username, String password) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            
            // Get next user ID
            Number maxId = realm.where(UserRealm.class).max("id");
            int nextId = (maxId == null) ? 1 : maxId.intValue() + 1;
            
            realm.beginTransaction();
            UserRealm user = realm.createObject(UserRealm.class, nextId);
            user.setUsername(username);
            user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST)));
            user.setCurrentWeight(0.0);
            user.setGoalWeight(0.0);
            user.setStartingWeight(0.0);
            user.setWeightEntries(new RealmList<>());
            realm.commitTransaction();
            
            Log.d(TAG, "User created with ID: " + nextId + " using BCrypt (cost=" + BCRYPT_COST + ")");
            return nextId;
        } catch (Exception e) {
            if (realm != null && realm.isInTransaction()) {
                realm.cancelTransaction();
            }
            Log.e(TAG, "Error creating user: ", e);
            return -1;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Check if username already exists
     */
    public boolean userExists(String username) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            long count = realm.where(UserRealm.class)
                    .equalTo("username", username)
                    .count();
            boolean exists = count > 0;
            Log.d(TAG, "User exists check for '" + username + "': " + exists);
            return exists;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if user exists: ", e);
            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Validate user credentials using BCrypt
     */
    public boolean validateUser(String username, String password) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            UserRealm user = realm.where(UserRealm.class)
                    .equalTo("username", username)
                    .findFirst();
            
            if (user != null) {
                boolean isValid = BCrypt.checkpw(password, user.getPasswordHash());
                Log.d(TAG, "User validation for '" + username + "' using BCrypt: " + isValid);
                return isValid;
            }
            
            Log.d(TAG, "User not found during validation: " + username);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error validating user: ", e);
            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Get user ID by username
     */
    public int getUserId(String username) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            UserRealm user = realm.where(UserRealm.class)
                    .equalTo("username", username)
                    .findFirst();
            
            if (user != null) {
                int userId = user.getId();
                Log.d(TAG, "Found user ID " + userId + " for username: " + username);
                return userId;
            }
            
            Log.e(TAG, "No user found with username: " + username);
            return -1;
        } catch (Exception e) {
            Log.e(TAG, "Error getting user ID: ", e);
            return -1;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Get user's current weight
     */
    public double getCurrentWeight(String username) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            UserRealm user = realm.where(UserRealm.class)
                    .equalTo("username", username)
                    .findFirst();
            
            if (user != null) {
                double weight = user.getCurrentWeight();
                Log.d(TAG, "Current weight for " + username + ": " + weight);
                return weight;
            }
            
            Log.e(TAG, "No user found when getting current weight: " + username);
            return 0.0;
        } catch (Exception e) {
            Log.e(TAG, "Error getting current weight: ", e);
            return 0.0;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Get user's goal weight
     */
    public double getGoalWeight(String username) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            UserRealm user = realm.where(UserRealm.class)
                    .equalTo("username", username)
                    .findFirst();
            
            if (user != null) {
                double weight = user.getGoalWeight();
                Log.d(TAG, "Goal weight for " + username + ": " + weight);
                return weight;
            }
            
            Log.e(TAG, "No user found when getting goal weight: " + username);
            return 0.0;
        } catch (Exception e) {
            Log.e(TAG, "Error getting goal weight: ", e);
            return 0.0;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Get user's starting weight
     */
    public double getStartingWeight(String username) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            UserRealm user = realm.where(UserRealm.class)
                    .equalTo("username", username)
                    .findFirst();
            
            if (user != null) {
                double weight = user.getStartingWeight();
                Log.d(TAG, "Starting weight for " + username + ": " + weight);
                return weight;
            }
            
            Log.e(TAG, "No user found when getting starting weight: " + username);
            return 0.0;
        } catch (Exception e) {
            Log.e(TAG, "Error getting starting weight: ", e);
            return 0.0;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Update user's current weight
     */
    public boolean updateCurrentWeight(String username, double weight) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            UserRealm user = realm.where(UserRealm.class)
                    .equalTo("username", username)
                    .findFirst();
            
            if (user != null) {
                realm.beginTransaction();
                user.setCurrentWeight(weight);
                realm.commitTransaction();
                Log.d(TAG, "Update current weight for " + username + " to " + weight + ": true");
                return true;
            }
            
            return false;
        } catch (Exception e) {
            if (realm != null && realm.isInTransaction()) {
                realm.cancelTransaction();
            }
            Log.e(TAG, "Error updating current weight: ", e);
            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Update user's goal weight
     */
    public boolean updateGoalWeight(String username, double weight) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            UserRealm user = realm.where(UserRealm.class)
                    .equalTo("username", username)
                    .findFirst();
            
            if (user != null) {
                realm.beginTransaction();
                user.setGoalWeight(weight);
                realm.commitTransaction();
                Log.d(TAG, "Update goal weight for " + username + " to " + weight + ": true");
                return true;
            }
            
            return false;
        } catch (Exception e) {
            if (realm != null && realm.isInTransaction()) {
                realm.cancelTransaction();
            }
            Log.e(TAG, "Error updating goal weight: ", e);
            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Update user's starting weight
     */
    public boolean updateStartingWeight(String username, double weight) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            UserRealm user = realm.where(UserRealm.class)
                    .equalTo("username", username)
                    .findFirst();
            
            if (user != null) {
                realm.beginTransaction();
                user.setStartingWeight(weight);
                realm.commitTransaction();
                Log.d(TAG, "Update starting weight for " + username + " to " + weight + ": true");
                return true;
            }
            
            return false;
        } catch (Exception e) {
            if (realm != null && realm.isInTransaction()) {
                realm.cancelTransaction();
            }
            Log.e(TAG, "Error updating starting weight: ", e);
            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    // ========================================
    // USER DELETION METHODS
    // ========================================

    /**
     * Delete all weight entries for a specific user
     */
    public boolean deleteAllUserEntries(int userId) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            UserRealm user = realm.where(UserRealm.class)
                    .equalTo("id", userId)
                    .findFirst();
            
            if (user != null) {
                realm.beginTransaction();
                user.getWeightEntries().clear();
                realm.commitTransaction();
                Log.d(TAG, "Deleted all entries for user ID: " + userId);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            if (realm != null && realm.isInTransaction()) {
                realm.cancelTransaction();
            }
            Log.e(TAG, "Error deleting user entries: ", e);
            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Delete user by ID
     */
    public boolean deleteUser(int userId) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            UserRealm user = realm.where(UserRealm.class)
                    .equalTo("id", userId)
                    .findFirst();
            
            if (user != null) {
                realm.beginTransaction();
                user.deleteFromRealm();
                realm.commitTransaction();
                Log.d(TAG, "Deleted user ID: " + userId);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            if (realm != null && realm.isInTransaction()) {
                realm.cancelTransaction();
            }
            Log.e(TAG, "Error deleting user: ", e);
            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Delete user by username
     */
    public boolean deleteUserByUsername(String username) {
        int userId = getUserId(username);
        if (userId != -1) {
            return deleteUser(userId);
        }
        return false;
    }

    /**
     * Check if user has any weight entries
     */
    public boolean userHasEntries(String username) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            UserRealm user = realm.where(UserRealm.class)
                    .equalTo("username", username)
                    .findFirst();
            
            if (user != null) {
                boolean hasEntries = user.getWeightEntries().size() > 0;
                Log.d(TAG, "User '" + username + "' has entries: " + hasEntries);
                return hasEntries;
            }
            
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if user has entries: ", e);
            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    // ========================================
    // WEIGHT ENTRY METHODS
    // ========================================

    /**
     * Add weight entry (embedded document)
     */
    public long addWeightEntry(WeightEntry entry) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            UserRealm user = realm.where(UserRealm.class)
                    .equalTo("id", entry.getUserId())
                    .findFirst();
            
            if (user != null) {
                // Get next entry ID across all users
                Number maxId = realm.where(WeightEntryRealm.class).max("id");
                int nextId = (maxId == null) ? 1 : maxId.intValue() + 1;
                
                realm.beginTransaction();
                
                // Create embedded entry
                WeightEntryRealm realmEntry = realm.createObject(WeightEntryRealm.class, nextId);
                realmEntry.setUserId(entry.getUserId());
                realmEntry.setWeight(entry.getWeight());
                realmEntry.setDate(entry.getDate());
                realmEntry.setTime(entry.getTime());
                realmEntry.setNotes(entry.getNotes() != null ? entry.getNotes() : "");
                
                // Add to user's entries list
                user.getWeightEntries().add(realmEntry);
                
                realm.commitTransaction();
                
                Log.d(TAG, "Added weight entry with ID: " + nextId + " (MongoDB embedded document)");
                return nextId;
            }
            
            Log.e(TAG, "User not found for entry: " + entry.getUserId());
            return -1;
        } catch (Exception e) {
            if (realm != null && realm.isInTransaction()) {
                realm.cancelTransaction();
            }
            Log.e(TAG, "Error adding weight entry: ", e);
            return -1;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Update current weight from most recent entry
     */
    public boolean updateCurrentWeightFromMostRecent(String username) {
        WeightEntry mostRecent = getMostRecentWeightEntry(username);
        if (mostRecent != null) {
            return updateCurrentWeight(username, mostRecent.getWeight());
        }
        return false;
    }

    /**
     * Get most recent weight entry by actual date/time
     */
    public WeightEntry getMostRecentWeightEntryByActualDate(String username) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            UserRealm user = realm.where(UserRealm.class)
                    .equalTo("username", username)
                    .findFirst();
            
            if (user == null || user.getWeightEntries().isEmpty()) {
                return null;
            }
            
            // Sort by date+time
            RealmList<WeightEntryRealm> entries = user.getWeightEntries();
            WeightEntryRealm mostRecent = null;
            Date mostRecentDateTime = null;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            
            for (WeightEntryRealm entry : entries) {
                try {
                    String dateTimeStr = entry.getDate() + " " + entry.getTime();
                    Date entryDateTime = format.parse(dateTimeStr);
                    
                    if (mostRecentDateTime == null || entryDateTime.after(mostRecentDateTime)) {
                        mostRecentDateTime = entryDateTime;
                        mostRecent = entry;
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date: ", e);
                }
            }
            
            if (mostRecent != null) {
                return convertToWeightEntry(mostRecent);
            }
            
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting most recent entry by date: ", e);
            return null;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Get recent weight entries with limit
     */
    public List<WeightEntry> getRecentWeightEntries(String username, int limit) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            UserRealm user = realm.where(UserRealm.class)
                    .equalTo("username", username)
                    .findFirst();
            
            if (user == null) {
                return new ArrayList<>();
            }
            
            // Get entries and sort by date descending
            List<WeightEntry> entries = new ArrayList<>();
            for (WeightEntryRealm realmEntry : user.getWeightEntries()) {
                entries.add(convertToWeightEntry(realmEntry));
            }
            
            // Sort by date descending
            entries.sort((e1, e2) -> {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date d1 = format.parse(e1.getDate() + " " + e1.getTime());
                    Date d2 = format.parse(e2.getDate() + " " + e2.getTime());
                    return d2.compareTo(d1); // Descending
                } catch (Exception e) {
                    return 0;
                }
            });
            
            // Apply limit
            if (entries.size() > limit) {
                entries = entries.subList(0, limit);
            }
            
            Log.d(TAG, "Retrieved " + entries.size() + " recent entries for " + username);
            return entries;
        } catch (Exception e) {
            Log.e(TAG, "Error getting recent entries: ", e);
            return new ArrayList<>();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Get all weight entries for user
     */
    public List<WeightEntry> getAllWeightEntries(String username) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            UserRealm user = realm.where(UserRealm.class)
                    .equalTo("username", username)
                    .findFirst();
            
            if (user == null) {
                return new ArrayList<>();
            }
            
            List<WeightEntry> entries = new ArrayList<>();
            for (WeightEntryRealm realmEntry : user.getWeightEntries()) {
                entries.add(convertToWeightEntry(realmEntry));
            }
            
            Log.d(TAG, "Retrieved " + entries.size() + " entries for " + username);
            return entries;
        } catch (Exception e) {
            Log.e(TAG, "Error getting all entries: ", e);
            return new ArrayList<>();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Get most recent weight entry
     */
    public WeightEntry getMostRecentWeightEntry(String username) {
        List<WeightEntry> entries = getRecentWeightEntries(username, 1);
        return entries.isEmpty() ? null : entries.get(0);
    }

    /**
     * Update weight entry
     */
    public boolean updateWeightEntry(int entryId, double weight, String date, String time) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            WeightEntryRealm entry = realm.where(WeightEntryRealm.class)
                    .equalTo("id", entryId)
                    .findFirst();
            
            if (entry != null) {
                realm.beginTransaction();
                entry.setWeight(weight);
                entry.setDate(date);
                entry.setTime(time);
                realm.commitTransaction();
                Log.d(TAG, "Updated entry ID: " + entryId);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            if (realm != null && realm.isInTransaction()) {
                realm.cancelTransaction();
            }
            Log.e(TAG, "Error updating entry: ", e);
            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Delete weight entry
     */
    public boolean deleteWeightEntry(int entryId) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            WeightEntryRealm entry = realm.where(WeightEntryRealm.class)
                    .equalTo("id", entryId)
                    .findFirst();
            
            if (entry != null) {
                realm.beginTransaction();
                entry.deleteFromRealm();
                realm.commitTransaction();
                Log.d(TAG, "Deleted entry ID: " + entryId);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            if (realm != null && realm.isInTransaction()) {
                realm.cancelTransaction();
            }
            Log.e(TAG, "Error deleting entry: ", e);
            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Get entry count for user
     */
    public int getEntryCount(String username) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            UserRealm user = realm.where(UserRealm.class)
                    .equalTo("username", username)
                    .findFirst();
            
            if (user != null) {
                return user.getWeightEntries().size();
            }
            
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Error getting entry count: ", e);
            return 0;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Get total user count
     */
    public int getUserCount() {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            return (int) realm.where(UserRealm.class).count();
        } catch (Exception e) {
            Log.e(TAG, "Error getting user count: ", e);
            return 0;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Convert Realm object to app WeightEntry object
     */
    private WeightEntry convertToWeightEntry(WeightEntryRealm realmEntry) {
        return new WeightEntry(
                realmEntry.getId(),
                realmEntry.getUserId(),
                realmEntry.getWeight(),
                realmEntry.getDate(),
                realmEntry.getTime(),
                realmEntry.getNotes()
        );
    }
}
