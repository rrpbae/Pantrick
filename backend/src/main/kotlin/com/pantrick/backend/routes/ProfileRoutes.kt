package com.pantrick.backend.routes

import com.pantrick.backend.models.ErrorResponse
import com.pantrick.backend.models.FeedbackRequest
import com.pantrick.backend.models.FeedbackResponse
import com.pantrick.backend.models.PreferencesResponse
import com.pantrick.backend.models.ProfileResponse
import com.pantrick.backend.models.ProfileSettingsResponse
import com.pantrick.backend.models.UpdateExpirationReminderRequest
import com.pantrick.backend.models.UpdateNotificationChannelRequest
import com.pantrick.backend.models.UpdatePreferencesRequest
import com.pantrick.backend.models.UpdateProfileRequest
import com.pantrick.backend.service.ProfileService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.profileRoutes(profileService: ProfileService) {
    route("/api/profile") {

        // GET /api/profile
        get {
            val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val profile = profileService.getProfile(userId)
            if (profile == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(success = false, message = "Pengguna tidak ditemukan"))
            } else {
                call.respond(HttpStatusCode.OK, ProfileResponse(success = true, message = "Berhasil mendapatkan profil", data = profile))
            }
        }

        // PUT /api/profile
        put {
            val userId = call.getAuthenticatedUserId() ?: return@put call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val request = runCatching { call.receive<UpdateProfileRequest>() }.getOrNull()
            if (request == null) {
                return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse(success = false, message = "Body request tidak valid"))
            }

            try {
                val updatedProfile = profileService.updateProfile(userId, request)
                call.respond(HttpStatusCode.OK, ProfileResponse(success = true, message = "Profil berhasil diperbarui", data = updatedProfile))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(success = false, message = e.message ?: "Validasi gagal"))
            }
        }

        // GET /api/profile/preferences
        get("/preferences") {
            val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val preferences = profileService.getPreferences(userId)
            call.respond(HttpStatusCode.OK, PreferencesResponse(success = true, message = "Berhasil mendapatkan preferensi", data = preferences))
        }

        // PUT /api/profile/preferences
        put("/preferences") {
            val userId = call.getAuthenticatedUserId() ?: return@put call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val request = runCatching { call.receive<UpdatePreferencesRequest>() }.getOrNull()
            if (request == null) {
                return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse(success = false, message = "Body request tidak valid"))
            }

            val updatedPreferences = profileService.updatePreferences(userId, request)
            call.respond(HttpStatusCode.OK, PreferencesResponse(success = true, message = "Preferensi berhasil diperbarui", data = updatedPreferences))
        }

        // GET /api/profile/settings
        get("/settings") {
            val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val settings = profileService.getSettings(userId)
            call.respond(HttpStatusCode.OK, ProfileSettingsResponse(success = true, message = "Berhasil mendapatkan pengaturan profil", data = settings))
        }

        // PUT /api/profile/settings/expiration-reminder
        put("/settings/expiration-reminder") {
            val userId = call.getAuthenticatedUserId() ?: return@put call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val request = runCatching { call.receive<UpdateExpirationReminderRequest>() }.getOrNull()
            if (request == null) {
                return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse(success = false, message = "Body request tidak valid"))
            }

            val updatedSettings = profileService.updateExpirationReminder(userId, request)
            call.respond(HttpStatusCode.OK, ProfileSettingsResponse(success = true, message = "Pengaturan pengingat kedaluwarsa berhasil diperbarui", data = updatedSettings))
        }

        // PUT /api/profile/settings/notification-channel
        put("/settings/notification-channel") {
            val userId = call.getAuthenticatedUserId() ?: return@put call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val request = runCatching { call.receive<UpdateNotificationChannelRequest>() }.getOrNull()
            if (request == null) {
                return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse(success = false, message = "Body request tidak valid"))
            }

            val updatedSettings = profileService.updateNotificationChannel(userId, request)
            call.respond(HttpStatusCode.OK, ProfileSettingsResponse(success = true, message = "Pengaturan saluran notifikasi berhasil diperbarui", data = updatedSettings))
        }

        // POST /api/profile/feedback
        post("/feedback") {
            val userId = call.getAuthenticatedUserId() ?: return@post call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val request = runCatching { call.receive<FeedbackRequest>() }.getOrNull()
            if (request == null) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(success = false, message = "Body request tidak valid"))
            }

            try {
                val feedback = profileService.submitFeedback(userId, request)
                call.respond(HttpStatusCode.Created, FeedbackResponse(success = true, message = "Masukan berhasil dikirim", data = feedback))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(success = false, message = e.message ?: "Validasi gagal"))
            }
        }
    }
}
