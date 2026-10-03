package com.pantrick.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class LegalDocument(
    val type: String,
    val title: String,
    val version: String,
    val updatedAt: String,
    val content: String
)
