package com.example.habittracker.models;

public class Habit {

    private Long id;
    private String title;
    private String description;
    private String frequency;
    private Boolean completed;

    public Habit() {}

    public Habit(Long id, String title, String description, String frequency, Boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.frequency = frequency;
        this.completed = completed;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getFrequency() { return frequency; }
    public Boolean getCompleted() { return completed; }

    public void setCompleted(Boolean completed) { this.completed = completed; }
}
