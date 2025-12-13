package com.example.weighttrackingapp;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class WeightTrackerApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Realm
        Realm.init(this);
        
        // Configure Realm
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("weight_tracker.realm")
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded() // For development - remove in production
                .build();
        
        Realm.setDefaultConfiguration(config);
    }
}
