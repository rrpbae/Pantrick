package com.pantrick.backend.service

import com.pantrick.backend.models.ExpirationReminderSetting
import com.pantrick.backend.models.FeedbackItem
import com.pantrick.backend.models.FeedbackRequest
import com.pantrick.backend.models.NotificationChannelSetting
import com.pantrick.backend.models.PreferencesData
import com.pantrick.backend.models.ProfileSettingsData
import com.pantrick.backend.models.UpdateExpirationReminderRequest
import com.pantrick.backend.models.UpdateNotificationChannelRequest
import com.pantrick.backend.models.UpdatePreferencesRequest
import com.pantrick.backend.models.UpdateProfileRequest
import com.pantrick.backend.models.User
import com.pantrick.backend.models.UserProfileData
import com.pantrick.backend.repository.UserRepository
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class ProfileService(
    private val userRepository: UserRepository
) {
    // In-Memory stores keyed by userId
    private val preferencesMap = ConcurrentHashMap<Int, PreferencesData>()
    private val settingsMap = ConcurrentHashMap<Int, ProfileSettingsData>()
    private val userXpMap = ConcurrentHashMap<Int, Int>()
    private val feedbackList = mutableListOf<FeedbackItem>()
    private val feedbackIdCounter = AtomicLong(1)

    fun getProfile(userId: Int): UserProfileData? {
        val user = userRepository.findById(userId) ?: return null
        val initial = computeAvatarInitial(user.name)
        val currentXp = userXpMap[userId] ?: 420 // default sensible starting XP for demo
        val (level, levelTitle, nextLevelXp) = computeLevelData(currentXp)

        return UserProfileData(
            id = user.id,
            name = user.name,
            email = user.email,
            avatarInitial = initial,
            level = level,
            levelTitle = levelTitle,
            currentXp = currentXp,
            nextLevelXp = nextLevelXp
        )
    }

    fun updateProfile(userId: Int, request: UpdateProfileRequest): UserProfileData? {
        val newName = request.name?.trim() ?: return getProfile(userId)
        if (newName.isBlank()) throw IllegalArgumentException("Nama tidak boleh kosong")

        val updatedUser = userRepository.updateUserName(userId, newName) ?: return null
        return getProfile(updatedUser.id)
    }

    fun getPreferences(userId: Int): PreferencesData {
        return preferencesMap[userId] ?: PreferencesData(
            foodRestrictions = listOf("VEGETARIAN", "BEBAS_KACANG")
        )
    }

    fun updatePreferences(userId: Int, request: UpdatePreferencesRequest): PreferencesData {
        val current = getPreferences(userId)
        val newRestrictions = request.foodRestrictions ?: current.foodRestrictions
        val updated = current.copy(foodRestrictions = newRestrictions)
        preferencesMap[userId] = updated
        return updated
    }

    fun getSettings(userId: Int): ProfileSettingsData {
        return settingsMap[userId] ?: ProfileSettingsData(
            expirationReminder = ExpirationReminderSetting(enabled = true, reminderTime = "09:00"),
            notificationChannel = NotificationChannelSetting(inApp = true, push = false)
        )
    }

    fun updateExpirationReminder(userId: Int, request: UpdateExpirationReminderRequest): ProfileSettingsData {
        val current = getSettings(userId)
        val enabled = request.enabled ?: current.expirationReminder.enabled
        val time = request.reminderTime?.trim()?.ifBlank { current.expirationReminder.reminderTime }
            ?: current.expirationReminder.reminderTime

        val updatedSettings = current.copy(
            expirationReminder = ExpirationReminderSetting(enabled = enabled, reminderTime = time)
        )
        settingsMap[userId] = updatedSettings
        return updatedSettings
    }

    fun updateNotificationChannel(userId: Int, request: UpdateNotificationChannelRequest): ProfileSettingsData {
        val current = getSettings(userId)
        val inApp = request.inApp ?: current.notificationChannel.inApp
        val push = request.push ?: current.notificationChannel.push

        val updatedSettings = current.copy(
            notificationChannel = NotificationChannelSetting(inApp = inApp, push = push)
        )
        settingsMap[userId] = updatedSettings
        return updatedSettings
    }

    fun submitFeedback(userId: Int, request: FeedbackRequest): FeedbackItem {
        val message = request.message.trim()
        if (message.isBlank()) throw IllegalArgumentException("Pesan masukan tidak boleh kosong")

        val validCategories = setOf("FEEDBACK", "BUG", "HELP")
        val category = request.category.trim().uppercase().let {
            if (it in validCategories) it else "FEEDBACK"
        }

        val feedback = FeedbackItem(
            id = "fb-${feedbackIdCounter.getAndIncrement()}",
            userId = userId,
            message = message,
            category = category,
            createdAt = Instant.now().toString()
        )

        synchronized(feedbackList) {
            feedbackList.add(feedback)
        }
        return feedback
    }

    fun getAllFeedback(): List<FeedbackItem> {
        synchronized(feedbackList) {
            return feedbackList.toList()
        }
    }

    private fun computeAvatarInitial(name: String): String {
        return name.trim().take(1).uppercase().ifBlank { "U" }
    }

    private fun computeLevelData(xp: Int): Triple<Int, String, Int> {
        return when {
            xp < 100 -> Triple(1, "Pemula Dapur", 100)
            xp < 250 -> Triple(2, "Penyaji Muda", 250)
            xp < 400 -> Triple(3, "Pengelola Dapur", 400)
            xp < 500 -> Triple(4, "Koki Ramah Lingkungan", 500)
            else -> {
                val lvl = 4 + ((xp - 500) / 200) + 1
                val nextXp = 500 + ((lvl - 4) * 200)
                Triple(lvl, "Master Pantrick", nextXp)
            }
        }
    }
}
