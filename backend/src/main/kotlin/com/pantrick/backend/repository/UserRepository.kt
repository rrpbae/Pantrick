package com.pantrick.backend.repository

import com.pantrick.backend.models.User
import com.pantrick.backend.security.PasswordHasher

interface UserRepository {
    fun findByEmail(email: String): User?
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
}
