package com.pantrick.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val passwordHash: String
)

@Serializable
data class UserData(
    val id: Int,
    val name: String,
    val email: String
)

@Serializable
data class LoginRequest(
    val email: String? = null,
    val password: String? = null
)

@Serializable
data class LoginResponseData(
    val token: String,
    val user: UserData
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginResponseData? = null
)

@Serializable
data class ErrorResponse(
    val success: Boolean = false,
    val message: String,
    val errors: Map<String, String>? = null
)

@Serializable
data class RegisterRequest(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val passwordConfirmation: String? = null,
    val password_confirmation: String? = null
) {
    val effectivePasswordConfirmation: String?
        get() = passwordConfirmation ?: password_confirmation
}

@Serializable
data class RegisterResponseData(
    val user: UserData
)

@Serializable
data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val data: RegisterResponseData? = null
)

/** Request body untuk endpoint POST /api/auth/forgot-password */
@Serializable
data class ForgotPasswordRequest(
    val email: String? = null
)

/** Response generik untuk endpoint forgot-password */
@Serializable
data class ForgotPasswordResponse(
    val message: String
)

/** Response untuk endpoint GET /api/auth/reset-password/verify */
@Serializable
data class VerifyResetTokenResponse(
    val valid: Boolean,
    val message: String? = null
)

/** Request body untuk endpoint POST /api/auth/reset-password */
@Serializable
data class ResetPasswordRequest(
    val token: String? = null,
    val password: String? = null,
    val passwordConfirmation: String? = null
)

/** Response untuk endpoint POST /api/auth/reset-password */
@Serializable
data class ResetPasswordResponse(
    val message: String
)
