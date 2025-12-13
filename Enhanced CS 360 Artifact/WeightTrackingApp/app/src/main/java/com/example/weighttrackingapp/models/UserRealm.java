package com.example.weighttrackingapp.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class UserRealm extends RealmObject {
    @PrimaryKey
    private int id;
    
    @Required
    private String username;
    
    @Required
    private String passwordHash;
    
    private double startingWeight;
    private double currentWeight;
    private double goalWeight;
    
    private RealmList<WeightEntryRealm> weightEntries;
    
    // Realm requires empty constructor
    public UserRealm() {}
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public double getStartingWeight() { return startingWeight; }
    public void setStartingWeight(double startingWeight) { this.startingWeight = startingWeight; }
    
    public double getCurrentWeight() { return currentWeight; }
    public void setCurrentWeight(double currentWeight) { this.currentWeight = currentWeight; }
    
    public double getGoalWeight() { return goalWeight; }
    public void setGoalWeight(double goalWeight) { this.goalWeight = goalWeight; }
    
    public RealmList<WeightEntryRealm> getWeightEntries() { return weightEntries; }
    public void setWeightEntries(RealmList<WeightEntryRealm> weightEntries) { this.weightEntries = weightEntries; }
}
