package com.a_track_it.workout.common.data_model;

public interface ITennisGame {
    void wonPoint(String playerName);
    void reversePoint(String playName);
    String getScore();
}
