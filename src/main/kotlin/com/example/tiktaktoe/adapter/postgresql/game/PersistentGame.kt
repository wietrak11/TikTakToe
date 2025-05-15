package com.example.tiktaktoe.adapter.postgresql.game

import com.example.tiktaktoe.domain.enums.Player
import com.example.tiktaktoe.domain.game.Game

import jakarta.persistence.*

@Entity(name = "game")
data class PersistentGame(

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val gameId: Long = 0,

    @Column
    val playerTurn: String,

    @Column
    val gameStatus: String
) {
    fun toDomain(): Game =
        Game(
            gameId = gameId,
            playerTurn = Player.valueOf(playerTurn),
            gameStatus = gameStatus
        )
}