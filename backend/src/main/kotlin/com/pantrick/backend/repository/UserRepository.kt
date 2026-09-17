package com.pantrick.backend.repository

import com.pantrick.backend.models.User
import com.pantrick.backend.security.PasswordHasher

interface UserRepository {
    fun findByEmail(email: String): User?
    fun createUser(name: String, email: String, passwordHash: String): User
}

class InMemoryUserRepository : UserRepository {
    private val users = mutableListOf<User>()
    private var nextId = 1

    init {
        // Seed default users for testing login
        createUserInternal(
            name = "Pantrick User",
            email = "user@example.com",
            passwordHash = PasswordHasher.hashPassword("password123")
        )
        createUserInternal(
            name = "Wahid",
            email = "wahid@pantrick.com",
            passwordHash = PasswordHasher.hashPassword("secret123")
        )
    }

    private fun createUserInternal(name: String, email: String, passwordHash: String): User {
        val user = User(
            id = nextId++,
            name = name,
            email = email,
            passwordHash = passwordHash
        )
        users.add(user)
        return user
    }

    override fun findByEmail(email: String): User? {
        return users.find { it.email.equals(email.trim(), ignoreCase = true) }
    }

    override fun createUser(name: String, email: String, passwordHash: String): User {
        synchronized(this) {
            return createUserInternal(name, email, passwordHash)
        }
    }
}

