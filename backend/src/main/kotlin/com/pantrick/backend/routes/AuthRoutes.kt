package com.pantrick.backend.routes

import com.pantrick.backend.models.ErrorResponse
import com.pantrick.backend.models.ForgotPasswordRequest
import com.pantrick.backend.models.ForgotPasswordResponse
import com.pantrick.backend.models.LoginRequest
import com.pantrick.backend.models.LoginResponse
import com.pantrick.backend.models.LoginResponseData
import com.pantrick.backend.models.ResetPasswordRequest
import com.pantrick.backend.models.ResetPasswordResponse
import com.pantrick.backend.models.UserData
import com.pantrick.backend.models.VerifyResetTokenResponse
import com.pantrick.backend.repository.UserRepository
import com.pantrick.backend.security.PasswordHasher
import com.pantrick.backend.security.TokenProvider
import com.pantrick.backend.service.PasswordResetService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond

import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

import com.pantrick.backend.models.RegisterRequest
import com.pantrick.backend.models.RegisterResponse
import com.pantrick.backend.models.RegisterResponseData

private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

fun Route.authRoutes(
    userRepository: UserRepository,
    passwordResetService: PasswordResetService
) {
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

        // =========================================================
        // FITUR: LUPA KATA SANDI
        // =========================================================

        /**
         * POST /api/auth/forgot-password
         * Meminta pengiriman instruksi reset password.
         * Response selalu generik untuk mencegah email enumeration attack.
         */
        route("/auth") {
            post("/forgot-password") {
                val request = runCatching { call.receive<ForgotPasswordRequest>() }.getOrNull()
                val email = request?.email?.trim()

                if (!email.isNullOrBlank()) {
                    // Jalankan request (token akan dibuat jika email valid)
                    // Hasilnya tidak dikembalikan ke client untuk mencegah enumeration attack
                    passwordResetService.requestPasswordReset(email)
                }

                // Response selalu generik, baik email terdaftar maupun tidak
                call.respond(
                    HttpStatusCode.OK,
                    ForgotPasswordResponse(
                        message = "Jika email terdaftar, instruksi reset kata sandi telah dibuat."
                    )
                )
            }

            /**
             * GET /api/auth/reset-password/verify?token=TOKEN
             * Memverifikasi apakah token reset password valid.
             */
            get("/reset-password/verify") {
                val token = call.request.queryParameters["token"]

                if (token.isNullOrBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        VerifyResetTokenResponse(
                            valid = false,
                            message = "Token reset kata sandi tidak valid atau sudah kedaluwarsa."
                        )
                    )
                    return@get
                }

                val validationResult = passwordResetService.validateToken(token)
                when (validationResult) {
                    PasswordResetService.TokenValidationResult.VALID -> {
                        call.respond(HttpStatusCode.OK, VerifyResetTokenResponse(valid = true))
                    }
                    else -> {
                        call.respond(
                            HttpStatusCode.OK,
                            VerifyResetTokenResponse(
                                valid = false,
                                message = "Token reset kata sandi tidak valid atau sudah kedaluwarsa."
                            )
                        )
                    }
                }
            }

            /**
             * POST /api/auth/reset-password
             * Melakukan reset password menggunakan token yang valid.
             */
            post("/reset-password") {
                val request = runCatching { call.receive<ResetPasswordRequest>() }.getOrNull()

                val token = request?.token
                val password = request?.password
                val passwordConfirmation = request?.passwordConfirmation

                val errors = mutableMapOf<String, String>()

                if (token.isNullOrBlank()) {
                    errors["token"] = "Token wajib diisi"
                }
                if (password.isNullOrBlank()) {
                    errors["password"] = "Password wajib diisi"
                }
                if (passwordConfirmation.isNullOrBlank()) {
                    errors["passwordConfirmation"] = "Konfirmasi password wajib diisi"
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

                val result = passwordResetService.resetPassword(
                    token = token!!,
                    newPassword = password!!,
                    passwordConfirmation = passwordConfirmation!!
                )

                when (result) {
                    is PasswordResetService.ResetPasswordResult.Success -> {
                        call.respond(
                            HttpStatusCode.OK,
                            ResetPasswordResponse(message = "Kata sandi berhasil diubah.")
                        )
                    }
                    is PasswordResetService.ResetPasswordResult.PasswordMismatch -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(
                                success = false,
                                message = "Validasi gagal",
                                errors = mapOf("passwordConfirmation" to "Konfirmasi password tidak cocok")
                            )
                        )
                    }
                    is PasswordResetService.ResetPasswordResult.InvalidToken,
                    is PasswordResetService.ResetPasswordResult.ExpiredToken,
                    is PasswordResetService.ResetPasswordResult.AlreadyUsedToken -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(
                                success = false,
                                message = "Token reset kata sandi tidak valid atau sudah kedaluwarsa.",
                                errors = mapOf("token" to "Token tidak valid")
                            )
                        )
                    }
                    is PasswordResetService.ResetPasswordResult.UserNotFound -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(
                                success = false,
                                message = "Terjadi kesalahan saat mereset password."
                            )
                        )
                    }
                }
            }
        }
    }
}

