package com.example.tiktaktoe.adapter.services.game

import com.example.tiktaktoe.adapter.postgresql.game.GameRepository
import com.example.tiktaktoe.adapter.postgresql.game.PersistentGame
import com.example.tiktaktoe.api.game.MoveRequest
import com.example.tiktaktoe.domain.enums.Player
import com.google.gson.Gson
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class GameServiceUnitTest {

    private lateinit var gameRepository: GameRepository
    private lateinit var gson: Gson
    private lateinit var service: GameService

    @BeforeEach
    fun setup() {
        gameRepository = mock(GameRepository::class.java)
        gson = Gson()
        service = GameService(gameRepository, gson)
    }


    @ParameterizedTest
    @MethodSource("winningBoards")
    fun `move should return win message when player X wins`(
        gameArray: Array<Array<String>>,
        moveRequest: MoveRequest
    ) {
        // given
        val game = PersistentGame(
            gameId = 1,
            playerTurn = "X",
            gameStatus = gson.toJson(gameArray)
        )
        `when`(gameRepository.findFirstBy()).thenReturn(game)

        // when
        val result = service.move(moveRequest)

        // then
        assertTrue(result.contains("Player X wins"))
    }

    @Test
    fun `move should return draw message when no more moves are possible`() {
        //given
        val gameArray = arrayOf(
            arrayOf("X", "X", "O"),
            arrayOf("O", "O", "X"),
            arrayOf("X", "O", "")
        )
        val game = PersistentGame(
            gameId = 1,
            playerTurn = "X",
            gameStatus = gson.toJson(gameArray)
        )
        `when`(gameRepository.findFirstBy()).thenReturn(game)
        val moveRequest = MoveRequest(player = Player.X, row = 3, column = 3)

        // when
        val result = service.move(moveRequest)

        assertTrue(result.contains("Its draw"))
    }

    companion object {
        @JvmStatic
        fun winningBoards(): List<Arguments> = listOf(
            Arguments.of(
                arrayOf(
                    arrayOf("X", "X", ""),
                    arrayOf("O", "O", ""),
                    arrayOf("", "", "")
                ),
                MoveRequest(player = Player.X, row = 1, column = 3)
            ),
            Arguments.of(
                arrayOf(
                    arrayOf("O", "O", ""),
                    arrayOf("X", "X", ""),
                    arrayOf("", "", "")
                ),
                MoveRequest(player = Player.X, row = 2, column = 3)
            ),
            Arguments.of(
                arrayOf(
                    arrayOf("", "", ""),
                    arrayOf("O", "O", ""),
                    arrayOf("X", "X", "")
                ),
                MoveRequest(player = Player.X, row = 3, column = 3)
            ),
            Arguments.of(
                arrayOf(
                    arrayOf("X", "O", ""),
                    arrayOf("X", "O", ""),
                    arrayOf("", "", "")
                ),
                MoveRequest(player = Player.X, row = 3, column = 1)
            ),
            Arguments.of(
                arrayOf(
                    arrayOf("O", "X", ""),
                    arrayOf("O", "X", ""),
                    arrayOf("", "", "")
                ),
                MoveRequest(player = Player.X, row = 3, column = 2)
            ),
            Arguments.of(
                arrayOf(
                    arrayOf("", "O", "X"),
                    arrayOf("", "O", "X"),
                    arrayOf("", "", "")
                ),
                MoveRequest(player = Player.X, row = 3, column = 3)
            ),
            Arguments.of(
                arrayOf(
                    arrayOf("X", "O", ""),
                    arrayOf("O", "X", ""),
                    arrayOf("", "", "")
                ),
                MoveRequest(player = Player.X, row = 3, column = 3)
            ),
            Arguments.of(
                arrayOf(
                    arrayOf("", "O", "X"),
                    arrayOf("O", "X", ""),
                    arrayOf("", "", "")
                ),
                MoveRequest(player = Player.X, row = 3, column = 1)
            )
        )
    }
}