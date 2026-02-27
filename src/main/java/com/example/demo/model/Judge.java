package com.example.demo.model;

public class Judge {
    private String name;
    private int fatigue;

    public Judge() {}
    public Judge(String name) { this.name = name; this.fatigue = 0; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getFatigue() { return fatigue; }
    public void setFatigue(int fatigue) { this.fatigue = fatigue; }

    public boolean canEvaluate() { return fatigue < 5; }
    public void increaseFatigue() { fatigue++; }
}