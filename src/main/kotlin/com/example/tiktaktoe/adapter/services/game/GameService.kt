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
import org.springframework.stereotype.Service

@Service
class GameService(
    private val gameRepository: GameRepository,
    private val gson: Gson
) {

    fun getCurrentGame(): Game =
        gameRepository.findFirstBy()?.toDomain()
            ?: throw NoCurrentGameFoundException("There is no game currently in progress")

    fun getReadableStatusFromJson(gameStatus: String): String =
        formatTicTacToeBoard(gson.fromJson(gameStatus, Array<Array<String>>::class.java))

    fun startGame(moveRequest: MoveRequest): String {
        gameRepository.deleteAll()
        val gameArray = Array(3) { Array(3) { "" } }
        gameArray[moveRequest.y - 1][moveRequest.x - 1] = moveRequest.player.toString()

        gameRepository.save(
            PersistentGame(
                playerTurn = getNextPlayer(moveRequest.player),
                gameStatus = gson.toJson(gameArray)
            )
        )

        return formatTicTacToeBoard(gameArray)
    }

    fun move(moveRequest: MoveRequest): String {
        val game =
            gameRepository.findFirstBy() ?: throw NoCurrentGameFoundException("There is no game currently in progress")
        if (game.playerTurn != moveRequest.player.toString()) {
            throw WrongPlayerException("Wrong player. Its ${game.playerTurn} turn")
        }

        val gameArray = gson.fromJson(game.gameStatus, Array<Array<String>>::class.java)

        if (gameArray[moveRequest.y - 1][moveRequest.x - 1] != "") {
            throw NotEmptyFieldException("Field [${moveRequest.x}][${moveRequest.y}] is not empty. Try again")
        }

        gameArray[moveRequest.y - 1][moveRequest.x - 1] = moveRequest.player.toString()
        if (checkWin(gameArray, moveRequest.player.toString())) {
            gameRepository.deleteAll()
            return gameArray.contentDeepToString() + "\n" + "Player ${moveRequest.player} wins"
        }

        if (checkDraw(gameArray)) {
            gameRepository.deleteAll()
            return "Its draw"
        }

        gameRepository.save(
            game.copy(
                playerTurn = getNextPlayer(moveRequest.player),
                gameStatus = gson.toJson(gameArray)
            )
        )
        return formatTicTacToeBoard(gameArray)
    }

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

    private fun getNextPlayer(player: Player): String =
        if (player == Player.X) {
            Player.Y.toString()
        } else {
            Player.X.toString()
        }

    private fun formatTicTacToeBoard(gameArray: Array<Array<String>>): String {
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
        return builder.toString()
    }
}