package com.example.rps.controller;

import com.example.rps.model.GameRequest;
import com.example.rps.model.GameResponse;
import com.example.rps.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/play")
    public ResponseEntity<GameResponse> play(@RequestBody GameRequest request) {
        GameResponse response = gameService.play(request.getPlayerChoice());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset() {
        gameService.resetGame();
        return ResponseEntity.ok().build();
    }
}