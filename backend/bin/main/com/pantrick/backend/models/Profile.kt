package com.pantrick.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileData(
    val id: Int,
    val name: String,
    val email: String,
    val avatarInitial: String,
    val level: Int,
    val levelTitle: String,
    val currentXp: Int,
    val nextLevelXp: Int
)

@Serializable
data class ProfileResponse(
    val success: Boolean = true,
    val message: String = "Berhasil mendapatkan profil",
    val data: UserProfileData? = null
)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null
)

@Serializable
data class PreferencesData(
    val foodRestrictions: List<String> = emptyList()
)

@Serializable
data class PreferencesResponse(
    val success: Boolean = true,
    val message: String = "Berhasil mendapatkan preferensi",
    val data: PreferencesData
)

@Serializable
data class UpdatePreferencesRequest(
    val foodRestrictions: List<String>? = null
)

@Serializable
data class ExpirationReminderSetting(
    val enabled: Boolean = true,
    val reminderTime: String = "09:00"
)

@Serializable
data class NotificationChannelSetting(
    val inApp: Boolean = true,
    val push: Boolean = false
)

@Serializable
data class ProfileSettingsData(
    val expirationReminder: ExpirationReminderSetting = ExpirationReminderSetting(),
    val notificationChannel: NotificationChannelSetting = NotificationChannelSetting()
)

@Serializable
data class ProfileSettingsResponse(
    val success: Boolean = true,
    val message: String = "Berhasil mendapatkan pengaturan profil",
    val data: ProfileSettingsData
)

@Serializable
data class UpdateExpirationReminderRequest(
    val enabled: Boolean? = null,
    val reminderTime: String? = null
)

@Serializable
data class UpdateNotificationChannelRequest(
    val inApp: Boolean? = null,
    val push: Boolean? = null
)

@Serializable
data class FeedbackRequest(
    val message: String,
    val category: String = "FEEDBACK"
)

@Serializable
data class FeedbackItem(
    val id: String,
    val userId: Int,
    val message: String,
    val category: String,
    val createdAt: String
)

@Serializable
data class FeedbackResponse(
    val success: Boolean = true,
    val message: String = "Masukan berhasil dikirim",
    val data: FeedbackItem? = null
)
