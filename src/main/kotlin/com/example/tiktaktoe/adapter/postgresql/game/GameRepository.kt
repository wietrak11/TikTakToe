package com.example.tiktaktoe.adapter.postgresql.game

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GameRepository : JpaRepository<PersistentGame, Long> {
    fun findFirstBy(): PersistentGame?
}