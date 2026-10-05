package com.pantrick.backend.routes

import com.pantrick.backend.models.ErrorResponse
import com.pantrick.backend.models.MarkNotificationReadResponse
import com.pantrick.backend.service.NotificationService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.route

fun Route.notificationRoutes(notificationService: NotificationService) {
    route("/api/notifications") {

        // GET /api/notifications
        get {
            println("[NOTIFICATION ROUTE] GET /api/notifications called")
            val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            println("[NOTIFICATION ROUTE] User authenticated: userId=$userId")
            val response = notificationService.getNotifications(userId)
            println("[NOTIFICATION ROUTE] Sending response: ${response.notifications.size} notifications")
            call.respond(HttpStatusCode.OK, response)
        }

        // PATCH /api/notifications/{id}/read
        patch("/{id}/read") {
            val userId = call.getAuthenticatedUserId() ?: return@patch call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val id = call.parameters["id"] ?: return@patch call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "ID notifikasi tidak valid")
            )

            val updatedNotification = notificationService.markAsRead(id, userId)
            if (updatedNotification == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = "Notifikasi tidak ditemukan atau tidak memiliki akses")
                )
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    MarkNotificationReadResponse(
                        success = true,
                        message = "Notifikasi berhasil ditandai sebagai dibaca",
                        data = updatedNotification
                    )
                )
            }
        }
    }
}
