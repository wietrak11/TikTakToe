package com.example.tiktaktoe.api.game

import com.example.tiktaktoe.adapter.postgresql.game.GameRepository
import com.example.tiktaktoe.adapter.postgresql.game.PersistentGame
import com.google.gson.Gson
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class GameControllerIntegrationTest {

    @Autowired
    private lateinit var gson: Gson

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var gameRepository: GameRepository

    @LocalServerPort
    var port: Int = 0

    @BeforeEach
    fun setup() {
        gameRepository.deleteAll()
    }

    @Test
    fun `should start a new game`() {
        // given
        val request = mapOf(
            "player" to "X",
            "row" to 1,
            "column" to 1
        )

        // when
        val response = restTemplate.postForEntity(
            "http://localhost:$port/api/game/start",
            request,
            String::class.java
        )

        // then
        assert(response.statusCode.is2xxSuccessful)
        assert(gameRepository.findAll().size == 1)
        assert(response.body!!.contains("Turn for player O"))
    }

    @Test
    fun `should return bad request when passed row is bigger than 3`() {
        // given
        val request = mapOf(
            "player" to "X",
            "row" to 5,
            "column" to 1
        )

        // when
        val response = restTemplate.postForEntity(
            "http://localhost:$port/api/game/start",
            request,
            String::class.java
        )

        // then
        assert(response.statusCode.isSameCodeAs(HttpStatus.BAD_REQUEST))
        assert(response.body!!.contains("Row value must be at most 3"))
    }

    @Test
    fun `should return bad request when passed column is smaller than 1`() {
        // given
        val request = mapOf(
            "player" to "X",
            "row" to 1,
            "column" to 0
        )

        // when
        val response = restTemplate.postForEntity(
            "http://localhost:$port/api/game/start",
            request,
            String::class.java
        )

        // then
        assert(response.statusCode.isSameCodeAs(HttpStatus.BAD_REQUEST))
        assert(response.body!!.contains("Column value must be at least 1"))
    }

    @Test
    fun `should return bad request when passed player is different than X or O`() {
        // given
        val request = mapOf(
            "player" to "ASD",
            "row" to 1,
            "column" to 1
        )

        // when
        val response = restTemplate.postForEntity(
            "http://localhost:$port/api/game/start",
            request,
            String::class.java
        )

        // then
        assert(response.statusCode.isSameCodeAs(HttpStatus.BAD_REQUEST))
    }

    @Test
    fun `should make move by player O`() {
        // given
        gameRepository.save(
            PersistentGame(
                playerTurn = "O", gameStatus = gson.toJson(
                    arrayOf(
                        arrayOf("X", "", ""),
                        arrayOf("", "", ""),
                        arrayOf("", "", "")
                    )
                )
            )
        )
        val request = mapOf(
            "player" to "O",
            "row" to 2,
            "column" to 1
        )

        // when
        val response = restTemplate.postForEntity(
            "http://localhost:$port/api/game/move",
            request,
            String::class.java
        )

        // then
        assert(response.statusCode.is2xxSuccessful)
        assert(gameRepository.findAll().size == 1)
        assert(response.body!!.contains("Turn for player X"))
    }

    @Test
    fun `should return bad request when try to make move to occupied field`() {
        // given
        gameRepository.save(
            PersistentGame(
                playerTurn = "O", gameStatus = gson.toJson(
                    arrayOf(
                        arrayOf("X", "", ""),
                        arrayOf("", "", ""),
                        arrayOf("", "", "")
                    )
                )
            )
        )
        val request = mapOf(
            "player" to "O",
            "row" to 1,
            "column" to 1
        )

        // when
        val response = restTemplate.postForEntity(
            "http://localhost:$port/api/game/move",
            request,
            String::class.java
        )

        // then
        assert(response.statusCode.isSameCodeAs(HttpStatus.BAD_REQUEST))
        assert(response.body!!.contains("Field [1][1] is not empty. Try again"))
    }

    @Test
    fun `should return bad request when try to make two moves in row`() {
        // given
        gameRepository.save(
            PersistentGame(
                playerTurn = "O", gameStatus = gson.toJson(
                    arrayOf(
                        arrayOf("X", "", ""),
                        arrayOf("", "", ""),
                        arrayOf("", "", "")
                    )
                )
            )
        )
        val request = mapOf(
            "player" to "X",
            "row" to 2,
            "column" to 2
        )

        // when
        val response = restTemplate.postForEntity(
            "http://localhost:$port/api/game/move",
            request,
            String::class.java
        )

        // then
        assert(response.statusCode.isSameCodeAs(HttpStatus.BAD_REQUEST))
        assert(response.body!!.contains("Wrong player. Its O turn"))
    }

    @Test
    fun `should return not found when trying to make move and no game is currently in progress`() {
        // given
        val request = mapOf(
            "player" to "X",
            "row" to 1,
            "column" to 1
        )

        // when
        val response = restTemplate.postForEntity(
            "http://localhost:$port/api/game/move",
            request,
            String::class.java
        )

        // then
        assert(response.statusCode.isSameCodeAs(HttpStatus.NOT_FOUND))
    }

    @Test
    fun `should return current status of game`() {
        // given
        gameRepository.save(
            PersistentGame(
                playerTurn = "O", gameStatus = gson.toJson(
                    arrayOf(
                        arrayOf("X", "", ""),
                        arrayOf("", "", ""),
                        arrayOf("", "", "")
                    )
                )
            )
        )

        // when
        val response = restTemplate.getForEntity(
            "http://localhost:$port/api/game/currentStatus",
            String::class.java
        )

        // then
        assert(response.statusCode.is2xxSuccessful)
        assert(
            response.body!!.contains(
                " X |   |   \n" +
                        "---+---+---\n" +
                        "   |   |   \n" +
                        "---+---+---\n" +
                        "   |   |   \n" +
                        "\n" +
                        "Turn for player O"
            )
        )
    }

    @Test
    fun `should return not found when no game is currently in progress`() {
        // when
        val response = restTemplate.getForEntity(
            "http://localhost:$port/api/game/currentStatus",
            String::class.java
        )

        // then
        assert(response.statusCode.isSameCodeAs(HttpStatus.NOT_FOUND))
    }

    @Test
    fun `should make move and win`() {
        // given
        gameRepository.save(
            PersistentGame(
                playerTurn = "X", gameStatus = gson.toJson(
                    arrayOf(
                        arrayOf("X", "O", ""),
                        arrayOf("X", "O", ""),
                        arrayOf("", "", "")
                    )
                )
            )
        )
        val request = mapOf(
            "player" to "X",
            "row" to 3,
            "column" to 1
        )

        // when
        val response = restTemplate.postForEntity(
            "http://localhost:$port/api/game/move",
            request,
            String::class.java
        )

        // then
        assert(response.statusCode.is2xxSuccessful)
        assert(gameRepository.findAll().size == 0)
        assert(response.body!!.contains("Player X wins"))
    }

    @Test
    fun `should make move and draw`() {
        // given
        gameRepository.save(
            PersistentGame(
                playerTurn = "X", gameStatus = gson.toJson(
                    arrayOf(
                        arrayOf("X", "O", "X"),
                        arrayOf("X", "O", "O"),
                        arrayOf("O", "X", "")
                    )
                )
            )
        )
        val request = mapOf(
            "player" to "X",
            "row" to 3,
            "column" to 3
        )

        // when
        val response = restTemplate.postForEntity(
            "http://localhost:$port/api/game/move",
            request,
            String::class.java
        )

        // then
        assert(response.statusCode.is2xxSuccessful)
        assert(gameRepository.findAll().size == 0)
        assert(response.body!!.contains("Its draw"))
    }

    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:15").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun overrideProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

}