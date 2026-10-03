package com.pantrick.backend.repository

import com.pantrick.backend.models.User
import com.pantrick.backend.security.PasswordHasher

interface UserRepository {
    fun findByEmail(email: String): User?
    fun findById(id: Int): User?
    fun updatePasswordHash(userId: Int, newPasswordHash: String): Boolean
}

class InMemoryUserRepository : UserRepository {
    private val users = mutableListOf<User>()

    init {
        // Seed default users for testing login
        users.add(
            User(
                id = 1,
                name = "Pantrick User",
                email = "user@example.com",
                passwordHash = PasswordHasher.hashPassword("password123")
            )
        )
        users.add(
            User(
                id = 2,
                name = "Wahid",
                email = "wahid@pantrick.com",
                passwordHash = PasswordHasher.hashPassword("secret123")
            )
        )
    }

    override fun findByEmail(email: String): User? {
        return users.find { it.email.equals(email.trim(), ignoreCase = true) }
    }

    override fun findById(id: Int): User? {
        return users.find { it.id == id }
    }

    override fun updatePasswordHash(userId: Int, newPasswordHash: String): Boolean {
        synchronized(this) {
            val index = users.indexOfFirst { it.id == userId }
            if (index == -1) return false
            users[index] = users[index].copy(passwordHash = newPasswordHash)
            return true
        }
    }
}
