package com.pantrick.backend.repository

import com.pantrick.backend.models.User
import com.pantrick.backend.security.PasswordHasher

interface UserRepository {
    fun findByEmail(email: String): User?
    fun findById(id: Int): User?
    fun createUser(name: String, email: String, passwordHash: String): User
    fun updatePasswordHash(userId: Int, newPasswordHash: String): Boolean
    fun updateUserName(userId: Int, newName: String): User?
}

class InMemoryUserRepository : UserRepository {
    private val users = java.util.concurrent.CopyOnWriteArrayList<User>()
    private val nextId = java.util.concurrent.atomic.AtomicInteger(1)

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
            id = nextId.getAndIncrement(),
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

    override fun findById(id: Int): User? {
        return users.find { it.id == id }
    }

    override fun createUser(name: String, email: String, passwordHash: String): User {
        synchronized(this) {
            return createUserInternal(name, email, passwordHash)
        }
    }

    override fun updatePasswordHash(userId: Int, newPasswordHash: String): Boolean {
        synchronized(this) {
            val index = users.indexOfFirst { it.id == userId }
            if (index == -1) return false
            users[index] = users[index].copy(passwordHash = newPasswordHash)
            return true
        }
    }

    override fun updateUserName(userId: Int, newName: String): User? {
        synchronized(this) {
            val index = users.indexOfFirst { it.id == userId }
            if (index == -1) return null
            val updatedUser = users[index].copy(name = newName)
            users[index] = updatedUser
            return updatedUser
        }
    }
}
