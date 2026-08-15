package com.example.rps.service;

import com.example.rps.model.GameResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.*;

@Service
@SessionScope
public class GameService {

    private final List<String> choices = List.of("ROCK", "PAPER", "SCISSORS");
    private final List<String> playerHistory = new ArrayList<>();
    private final List<String> aiHistory = new ArrayList<>();
    private final Random random = new Random();

    private int playerScore = 0;
    private int aiScore = 0;
    private int currentStreak = 0;

    private static final int MAX_HISTORY_WINDOW = 30;

    private static record AiDecision(String aiMove, String predictedPlayerMove, int confidence, String strategy) {}

    public GameResponse play(String playerChoice) {
        if (playerChoice == null) {
            throw new IllegalArgumentException("Choice is required");
        }

        playerChoice = playerChoice.toUpperCase();
        if (!choices.contains(playerChoice)) {
            throw new IllegalArgumentException("Invalid choice");
        }

        // 1. AI computes decision & prediction
        AiDecision decision = computeAiDecision();

        String aiChoice = decision.aiMove();
        String result = evaluateResult(playerChoice, aiChoice);

        // 2. Update scores & streaks
        if ("PLAYER_WINS".equals(result)) {
            playerScore++;
            currentStreak = (currentStreak > 0) ? currentStreak + 1 : 1;
        } else if ("AI_WINS".equals(result)) {
            aiScore++;
            currentStreak = (currentStreak < 0) ? currentStreak - 1 : -1;
        }

        String resultMessage = createResultMessage(playerChoice, aiChoice, result);

        // 3. Store history AFTER move evaluation
        playerHistory.add(playerChoice);
        aiHistory.add(aiChoice);

        return new GameResponse(
                playerChoice,
                aiChoice,
                result,
                resultMessage,
                playerScore,
                aiScore,
                currentStreak,
                decision.predictedPlayerMove(),
                decision.confidence(),
                decision.strategy()
        );
    }

    // =========================================================
    // ENHANCED AI ENGINE (WITH FORCED MOVE SHIFTING)
    // =========================================================

    private AiDecision computeAiDecision() {
        String lastAiMove = aiHistory.isEmpty() ? null : aiHistory.get(aiHistory.size() - 1);

        // 1. Build list of valid choices excluding AI's last move (No repeats)
        List<String> nonRepeatingChoices = new ArrayList<>(choices);
        if (lastAiMove != null) {
            nonRepeatingChoices.remove(lastAiMove);
        }

        if (playerHistory.size() < 2) {
            String randMove = getRandomValidMove(nonRepeatingChoices);
            return new AiDecision(randMove, "UNKNOWN", 33, "Random Baseline (No Repeat)");
        }

        Map<String, Double> scores = new HashMap<>();
        for (String choice : choices) {
            scores.put(choice, 0.0);
        }

        int startIdx = Math.max(0, playerHistory.size() - MAX_HISTORY_WINDOW);
        List<String> recentPlayer = playerHistory.subList(startIdx, playerHistory.size());
        int n = recentPlayer.size();

        String dominantStrategy = "Frequency Decay";

        // --- PATTERN ANALYSIS ---
        for (int i = 0; i < n; i++) {
            String move = recentPlayer.get(i);
            double recencyWeight = (double) (i + 1) / n;
            scores.put(move, scores.get(move) + recencyWeight * 10.0);
        }

        String lastPlayerMove = recentPlayer.get(n - 1);
        String lastResult = evaluateResult(lastPlayerMove, lastAiMove);

        if ("PLAYER_WINS".equals(lastResult)) {
            scores.put(lastPlayerMove, scores.get(lastPlayerMove) + 25.0);
            dominantStrategy = "Win-Stay Psychology";
        } else if ("AI_WINS".equals(lastResult)) {
            String counterToAiLast = counter(lastAiMove);
            scores.put(counterToAiLast, scores.get(counterToAiLast) + 25.0);
            dominantStrategy = "Lose-Shift Psychology";
        }

        for (int i = 0; i < n - 1; i++) {
            if (recentPlayer.get(i).equals(lastPlayerMove)) {
                String nextMove = recentPlayer.get(i + 1);
                double weight = ((double) (i + 1) / n) * 20.0;
                scores.put(nextMove, scores.get(nextMove) + weight);
                if (weight > 10.0) dominantStrategy = "1st Order Pattern";
            }
        }

        if (n >= 2) {
            String prev1 = recentPlayer.get(n - 2);
            String prev2 = recentPlayer.get(n - 1);
            for (int i = 0; i < n - 2; i++) {
                if (recentPlayer.get(i).equals(prev1) && recentPlayer.get(i + 1).equals(prev2)) {
                    String predicted = recentPlayer.get(i + 2);
                    double weight = ((double) (i + 1) / n) * 35.0;
                    scores.put(predicted, scores.get(predicted) + weight);
                    dominantStrategy = "2nd Order Pattern";
                }
            }
        }

        if (n >= 3) {
            String p1 = recentPlayer.get(n - 3);
            String p2 = recentPlayer.get(n - 2);
            String p3 = recentPlayer.get(n - 1);
            for (int i = 0; i < n - 3; i++) {
                if (recentPlayer.get(i).equals(p1) &&
                    recentPlayer.get(i + 1).equals(p2) &&
                    recentPlayer.get(i + 2).equals(p3)) {

                    String predicted = recentPlayer.get(i + 3);
                    double weight = ((double) (i + 1) / n) * 50.0;
                    scores.put(predicted, scores.get(predicted) + weight);
                    dominantStrategy = "3rd Order Deep Pattern";
                }
            }
        }

        String predictedPlayerMove = Collections.max(scores.entrySet(), Map.Entry.comparingByValue()).getKey();
        double maxScore = scores.get(predictedPlayerMove);
        double totalScore = scores.values().stream().mapToDouble(Double::doubleValue).sum();

        int confidence = (totalScore > 0) ? (int) Math.min(99, Math.max(34, (maxScore / totalScore) * 100)) : 33;

        String winningCounter = counter(predictedPlayerMove);
        String aiChoice;

        // 2. ANTI-DRAW FILTERING: Build fallback list removing BOTH last AI move AND drawing move
        List<String> antiDrawChoices = new ArrayList<>(nonRepeatingChoices);
        antiDrawChoices.remove(predictedPlayerMove); // Removes the move that causes a draw!

        if (winningCounter.equals(lastAiMove)) {
            // Winning move is blocked by "no repeat" rule -> pick remaining non-drawing move
            aiChoice = !antiDrawChoices.isEmpty() ? antiDrawChoices.get(0) : nonRepeatingChoices.get(0);
            dominantStrategy += " (Decisive Shift)";
        } else {
            // Apply small wildcard noise with anti-draw filter
            double noiseProbability = (confidence > 75) ? 0.02 : 0.08;
            if (random.nextDouble() < noiseProbability && !antiDrawChoices.isEmpty()) {
                aiChoice = getRandomValidMove(antiDrawChoices);
                dominantStrategy += " (Wildcard Anti-Draw)";
            } else {
                aiChoice = winningCounter;
            }
        }

        return new AiDecision(aiChoice, predictedPlayerMove, confidence, dominantStrategy);
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private String getRandomValidMove(List<String> availableChoices) {
        return availableChoices.get(random.nextInt(availableChoices.size()));
    }

    private String counter(String move) {
        return switch (move) {
            case "ROCK" -> "PAPER";
            case "PAPER" -> "SCISSORS";
            case "SCISSORS" -> "ROCK";
            default -> choices.get(random.nextInt(choices.size()));
        };
    }

    private String evaluateResult(String player, String ai) {
        if (player.equals(ai)) {
            return "DRAW";
        }

        if ((player.equals("ROCK") && ai.equals("SCISSORS")) ||
            (player.equals("PAPER") && ai.equals("ROCK")) ||
            (player.equals("SCISSORS") && ai.equals("PAPER"))) {
            return "PLAYER_WINS";
        }

        return "AI_WINS";
    }

    private String createResultMessage(String playerChoice, String aiChoice, String result) {
        if ("DRAW".equals(result)) {
            return "It's a Draw! Both selected " + playerChoice + ".";
        }

        if ("PLAYER_WINS".equals(result)) {
            String verb = getActionVerb(playerChoice, aiChoice);
            String streakNotice = (currentStreak >= 3) ? " 🔥 (" + currentStreak + " win streak!)" : "";
            return "You Won! " + playerChoice + " " + verb + " " + aiChoice + "." + streakNotice;
        } else {
            String verb = getActionVerb(aiChoice, playerChoice);
            String streakNotice = (currentStreak <= -3) ? " 🤖 (AI is on a " + Math.abs(currentStreak) + " win streak!)" : "";
            return "AI Won! " + aiChoice + " " + verb + " " + playerChoice + "." + streakNotice;
        }
    }

    private String getActionVerb(String winner, String loser) {
        if ("ROCK".equals(winner) && "SCISSORS".equals(loser)) return "crushes";
        if ("PAPER".equals(winner) && "ROCK".equals(loser)) return "covers";
        if ("SCISSORS".equals(winner) && "PAPER".equals(loser)) return "cuts";
        return "beats";
    }

    public void resetGame() {
        playerHistory.clear();
        aiHistory.clear();
        playerScore = 0;
        aiScore = 0;
        currentStreak = 0;
    }
}