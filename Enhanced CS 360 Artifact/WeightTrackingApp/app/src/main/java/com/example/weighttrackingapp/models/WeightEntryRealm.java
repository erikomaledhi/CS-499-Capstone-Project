package com.example.weighttrackingapp.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class WeightEntryRealm extends RealmObject {
    @PrimaryKey
    private int id;
    
    private int userId;
    private double weight;
    
    @Required
    private String date;
    
    @Required
    private String time;
    
    private String notes;
    
    // Realm requires empty constructor
    public WeightEntryRealm() {}
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
