package com.pantrick.backend.repository

import com.pantrick.backend.models.CookingSession
import com.pantrick.backend.models.CookingSessionStatus
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

interface CookingSessionRepository {
    fun createSession(session: CookingSession): CookingSession
    fun getSessionById(sessionId: String): CookingSession?
    fun updateSession(session: CookingSession): CookingSession?
    /** Cari session STARTED untuk user+recipe tertentu (untuk idempotency check) */
    fun findActiveSession(userId: Int, recipeId: String): CookingSession?
    fun getSessionsByUserId(userId: Int): List<CookingSession>
}

class InMemoryCookingSessionRepository : CookingSessionRepository {
    private val sessions = ConcurrentHashMap<String, CookingSession>()
    private val idCounter = AtomicLong(1)

    override fun createSession(session: CookingSession): CookingSession {
        val id = if (session.id.isNotBlank()) session.id else "cook-session-${idCounter.getAndIncrement()}"
        val toSave = session.copy(id = id)
        sessions[id] = toSave
        return toSave
    }

    override fun getSessionById(sessionId: String): CookingSession? =
        sessions[sessionId]

    override fun updateSession(session: CookingSession): CookingSession? {
        val existing = sessions[session.id] ?: return null
        // Cegah perubahan userId/recipeId
        val updated = session.copy(userId = existing.userId, recipeId = existing.recipeId)
        sessions[session.id] = updated
        return updated
    }

    override fun findActiveSession(userId: Int, recipeId: String): CookingSession? =
        sessions.values.find {
            it.userId == userId &&
            it.recipeId == recipeId &&
            it.status == CookingSessionStatus.STARTED
        }

    override fun getSessionsByUserId(userId: Int): List<CookingSession> =
        sessions.values
            .filter { it.userId == userId }
            .sortedByDescending { it.startedAt }
}
