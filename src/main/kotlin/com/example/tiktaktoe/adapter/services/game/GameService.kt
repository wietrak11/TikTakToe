package com.example.tiktaktoe.adapter.services.game

import com.example.tiktaktoe.adapter.postgresql.game.GameRepository
import com.example.tiktaktoe.adapter.postgresql.game.PersistentGame
import com.example.tiktaktoe.api.game.MoveRequest
import com.example.tiktaktoe.domain.enums.Player
import com.example.tiktaktoe.domain.game.Game
import com.example.tiktaktoe.infrastructure.NoCurrentGameFoundException
import com.example.tiktaktoe.infrastructure.NotEmptyFieldException
import com.example.tiktaktoe.infrastructure.WrongPlayerException
import com.google.gson.Gson
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class GameService(
    private val gameRepository: GameRepository,
    private val gson: Gson
) {

    fun getReadableStatusFromJson(): String {
        val currentGame = getCurrentGame()
        return formatTicTacToeBoard(
            gson.fromJson(currentGame.gameStatus, Array<Array<String>>::class.java),
            currentGame.playerTurn
        )
    }

    fun startGame(moveRequest: MoveRequest): String {
        gameRepository.deleteAll()
        val gameArray = Array(3) { Array(3) { "" } }
        gameArray[moveRequest.row - 1][moveRequest.column - 1] = moveRequest.player.toString()

        val playerTurn = getNextPlayer(moveRequest.player)
        gameRepository.save(
            PersistentGame(
                playerTurn = playerTurn.toString(),
                gameStatus = gson.toJson(gameArray)
            )
        )

        return formatTicTacToeBoard(gameArray, playerTurn)
    }

    fun move(moveRequest: MoveRequest): String {
        val game =
            gameRepository.findFirstBy() ?: throw NoCurrentGameFoundException("There is no game currently in progress")
        if (game.playerTurn != moveRequest.player.toString()) {
            throw WrongPlayerException("Wrong player. Its ${game.playerTurn} turn")
        }

        val gameArray = gson.fromJson(game.gameStatus, Array<Array<String>>::class.java)

        if (gameArray[moveRequest.row - 1][moveRequest.column - 1] != "") {
            throw NotEmptyFieldException("Field [${moveRequest.column}][${moveRequest.row}] is not empty. Try again")
        }

        gameArray[moveRequest.row - 1][moveRequest.column - 1] = moveRequest.player.toString()
        if (checkWin(gameArray, moveRequest.player.toString())) {
            gameRepository.deleteAll()
            return gameArray.contentDeepToString() + "\n" + "Player ${moveRequest.player} wins"
        }

        if (checkDraw(gameArray)) {
            gameRepository.deleteAll()
            return "Its draw"
        }

        val playerTurn = getNextPlayer(moveRequest.player)
        gameRepository.save(
            game.copy(
                playerTurn = playerTurn.toString(),
                gameStatus = gson.toJson(gameArray)
            )
        )
        return formatTicTacToeBoard(gameArray, playerTurn)
    }

    private fun getCurrentGame(): Game =
        gameRepository.findFirstBy()?.toDomain()
            ?: throw NoCurrentGameFoundException("There is no game currently in progress")

    private fun checkWin(gameArray: Array<Array<String>>, player: String): Boolean {
        val size = gameArray.size

        for (i in 0 until size) {
            if ((0 until size).all { j -> gameArray[i][j] == player }) return true
            if ((0 until size).all { j -> gameArray[j][i] == player }) return true
        }

        if ((0 until size).all { i -> gameArray[i][i] == player }) return true
        if ((0 until size).all { i -> gameArray[i][size - 1 - i] == player }) return true

        return false
    }

    private fun checkDraw(gameArray: Array<Array<String>>): Boolean {
        for (i in gameArray) {
            for (j in i) {
                if (j == "") return false
            }
        }
        return true
    }

    private fun getNextPlayer(player: Player): Player =
        if (player == Player.X) {
            Player.O
        } else {
            Player.X
        }

    private fun formatTicTacToeBoard(gameArray: Array<Array<String>>, playerTurn: Player): String {
        val builder = StringBuilder()
        for (i in gameArray.indices) {
            for (j in gameArray[i].indices) {
                if (gameArray[i][j] == "") {
                    builder.append("  ${gameArray[i][j]} ")
                } else {
                    builder.append(" ${gameArray[i][j]} ")
                }
                if (j < gameArray[i].size - 1) builder.append("|")
            }
            builder.append("\n")
            if (i < gameArray.size - 1) {
                builder.append("---+---+---\n")
            }
        }
        builder.append("\n")
        builder.append("Turn for player $playerTurn")
        return builder.toString()
    }
}