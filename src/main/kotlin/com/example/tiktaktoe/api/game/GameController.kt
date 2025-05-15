package com.example.tiktaktoe.api.game

import com.example.tiktaktoe.adapter.services.game.GameService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class GameController(
    private val gameService: GameService
) {

    @GetMapping("/game/currentStatus")
    fun currentStatus(): String =
        // I should have used the response class but thanks to the string itself, the board displays nicely in Postman
        gameService.getReadableStatusFromJson(gameService.getCurrentGame().gameStatus)

    @PostMapping("/game/start")
    fun startGame(@RequestBody moveRequest: MoveRequest): String =
        gameService.startGame(moveRequest)

    @PostMapping("/game/move")
    fun move(@RequestBody moveRequest: MoveRequest): String =
        gameService.move(moveRequest)
}