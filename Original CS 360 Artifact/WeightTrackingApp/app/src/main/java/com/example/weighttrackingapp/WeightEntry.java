package com.example.weighttrackingapp;


public class WeightEntry {
    private int id;
    private int userId;
    private double weight;
    private String date;
    private String time;
    private String notes;

    // Constructors
    public WeightEntry() {}

    public WeightEntry(int id, int userId, double weight, String date, String time, String notes) {
        this.id = id;
        this.userId = userId;
        this.weight = weight;
        this.date = date;
        this.time = time;
        this.notes = notes;
    }

    public WeightEntry(int userId, double weight, String date, String time, String notes) {
        this.userId = userId;
        this.weight = weight;
        this.date = date;
        this.time = time;
        this.notes = notes;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Helper methods
    public String getFormattedWeight() {
        return String.format("%.1f kg", weight);
    }

    public String getDisplayDate() {
        // You can format this as needed
        return date;
    }

    public String getDisplayTime() {
        // You can format this as needed
        return time;
    }

    @Override
    public String toString() {
        return "WeightEntry{" +
                "id=" + id +
                ", userId=" + userId +
                ", weight=" + weight +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}
