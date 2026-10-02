package com.pantrick.backend.routes

import com.pantrick.backend.models.ErrorResponse
import com.pantrick.backend.models.LoginRequest
import com.pantrick.backend.models.LoginResponse
import com.pantrick.backend.models.LoginResponseData
import com.pantrick.backend.models.UserData
import com.pantrick.backend.repository.UserRepository
import com.pantrick.backend.security.PasswordHasher
import com.pantrick.backend.security.TokenProvider
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond

import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

import com.pantrick.backend.models.RegisterRequest
import com.pantrick.backend.models.RegisterResponse
import com.pantrick.backend.models.RegisterResponseData

private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

fun Route.authRoutes(userRepository: UserRepository) {
    route("/api") {
        post("/register") {
            val request = runCatching { call.receive<RegisterRequest>() }.getOrNull()

            val errors = mutableMapOf<String, String>()

            val name = request?.name?.trim()
            val email = request?.email?.trim()
            val password = request?.password
            val passwordConfirmation = request?.effectivePasswordConfirmation

            if (name.isNullOrBlank()) {
                errors["name"] = "Nama wajib diisi"
            }

            if (email.isNullOrBlank()) {
                errors["email"] = "Email wajib diisi"
            } else if (!EMAIL_REGEX.matches(email)) {
                errors["email"] = "Format email tidak valid"
            } else if (userRepository.findByEmail(email) != null) {
                errors["email"] = "Email sudah terdaftar"
            }

            if (password.isNullOrBlank()) {
                errors["password"] = "Password wajib diisi"
            }

            if (passwordConfirmation.isNullOrBlank()) {
                errors["password_confirmation"] = "Konfirmasi password wajib diisi"
            } else if (!password.isNullOrBlank() && password != passwordConfirmation) {
                errors["password_confirmation"] = "Konfirmasi password tidak cocok"
            }

            if (errors.isNotEmpty()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        success = false,
                        message = "Validasi gagal",
                        errors = errors
                    )
                )
                return@post
            }

            val passwordHash = PasswordHasher.hashPassword(password!!)
            val newUser = userRepository.createUser(
                name = name!!,
                email = email!!,
                passwordHash = passwordHash
            )

            call.respond(
                HttpStatusCode.Created,
                RegisterResponse(
                    success = true,
                    message = "Registrasi berhasil",
                    data = RegisterResponseData(
                        user = UserData(
                            id = newUser.id,
                            name = newUser.name,
                            email = newUser.email
                        )
                    )
                )
            )
        }

        post("/login") {
            val request = runCatching { call.receive<LoginRequest>() }.getOrNull()


            val errors = mutableMapOf<String, String>()

            val email = request?.email?.trim()
            val password = request?.password


            if (email.isNullOrBlank()) {
                errors["email"] = "Email wajib diisi"
            } else if (!EMAIL_REGEX.matches(email)) {
                errors["email"] = "Format email tidak valid"
            }

            if (password.isNullOrBlank()) {
                errors["password"] = "Password wajib diisi"
            }

            if (errors.isNotEmpty()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        success = false,
                        message = "Validasi gagal",
                        errors = errors
                    )
                )
                return@post
            }

            val user = userRepository.findByEmail(email!!)
            if (user == null || !PasswordHasher.verifyPassword(password!!, user.passwordHash)) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        success = false,
                        message = "Email atau password salah"
                    )
                )
                return@post
            }

            val token = TokenProvider.generateToken(user.id, user.email)
            val responseData = LoginResponseData(
                token = token,
                user = UserData(
                    id = user.id,
                    name = user.name,
                    email = user.email
                )
            )

            call.respond(
                HttpStatusCode.OK,
                LoginResponse(
                    success = true,
                    message = "Login berhasil",
                    data = responseData
                )
            )
        }
    }
}
