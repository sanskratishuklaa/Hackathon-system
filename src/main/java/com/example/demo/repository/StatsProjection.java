package com.example.demo.repository;

/**
 * Projection interface for leaderboard data.
 */
public interface StatsProjection {
    String getTitle();

    Integer getScore();

    String getHackathonName();
}