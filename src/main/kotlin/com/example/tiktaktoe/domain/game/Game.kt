package com.example.tiktaktoe.domain.game

import com.example.tiktaktoe.adapter.postgresql.game.PersistentGame
import com.example.tiktaktoe.domain.enums.Player

data class Game(
    val gameId: Long,
    val playerTurn: Player,
    val gameStatus: String
) {
    fun toPersistent(): PersistentGame =
        PersistentGame(
            gameId = gameId,
            playerTurn = playerTurn.toString(),
            gameStatus = gameStatus
        )
}