package com.example.tiktaktoe.api.game

import com.example.tiktaktoe.adapter.services.game.GameService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/game")
class GameController(
    private val gameService: GameService
) {

    @GetMapping("/currentStatus")
    fun currentStatus(): String =
        // I should have used the response class but thanks to the string itself, the board displays nicely in Postman
        gameService.getReadableStatusFromJson()

    @PostMapping("/start")
    fun startGame(@Valid @RequestBody moveRequest: MoveRequest): String =
        gameService.startGame(moveRequest)

    @PostMapping("/move")
    fun move(@Valid @RequestBody moveRequest: MoveRequest): String =
        gameService.move(moveRequest)
}