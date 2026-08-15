package com.example.rps.model;

public class GameRequest {
    private String playerChoice;

    public GameRequest() {}

    public GameRequest(String playerChoice) {
        this.playerChoice = playerChoice;
    }

    public String getPlayerChoice() {
        return playerChoice;
    }

    public void setPlayerChoice(String playerChoice) {
        this.playerChoice = playerChoice;
    }
}