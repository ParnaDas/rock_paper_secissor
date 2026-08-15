package com.example.rps.model;

public class GameResponse {

    private String playerChoice;
    private String aiChoice;
    private String result;
    private String message;
    private int playerScore;
    private int aiScore;
    private int currentStreak;
    
    // AI Telemetry
    private String predictedPlayerMove;
    private int aiConfidence; // Percentage (0 - 100%)
    private String aiStrategy; // e.g., "3rd Order Markov", "Win-Stay Psychology"

    public GameResponse() {}

    public GameResponse(String playerChoice, String aiChoice, String result, String message, 
                        int playerScore, int aiScore, int currentStreak, 
                        String predictedPlayerMove, int aiConfidence, String aiStrategy) {
        this.playerChoice = playerChoice;
        this.aiChoice = aiChoice;
        this.result = result;
        this.message = message;
        this.playerScore = playerScore;
        this.aiScore = aiScore;
        this.currentStreak = currentStreak;
        this.predictedPlayerMove = predictedPlayerMove;
        this.aiConfidence = aiConfidence;
        this.aiStrategy = aiStrategy;
    }

    // Getters
    public String getPlayerChoice() { return playerChoice; }
    public String getAiChoice() { return aiChoice; }
    public String getResult() { return result; }
    public String getMessage() { return message; }
    public int getPlayerScore() { return playerScore; }
    public int getAiScore() { return aiScore; }
    public int getCurrentStreak() { return currentStreak; }
    public String getPredictedPlayerMove() { return predictedPlayerMove; }
    public int getAiConfidence() { return aiConfidence; }
    public String getAiStrategy() { return aiStrategy; }
}