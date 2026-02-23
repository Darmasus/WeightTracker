package com.example.weighttracker;

/**
 * Simple model for one weight row in the database.
 * This is used by DashboardActivity when building the grid.
 */
public class WeightEntry {

    public final long id;
    public final String dateText;
    public final double weight;

    public WeightEntry(long id, String dateText, double weight) {
        this.id = id;
        this.dateText = dateText;
        this.weight = weight;
    }
}