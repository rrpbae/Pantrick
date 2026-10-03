package com.pantrick.backend

import com.pantrick.backend.models.LegalDocument
import com.pantrick.backend.repository.InMemoryUserRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test suite untuk fitur informasi hukum (Terms of Service dan Privacy Policy).
 *
 * Mencakup 8 skenario test sesuai spesifikasi B5.
 */
class LegalTest {

    // =========================================================
    // TEST 1: terms of service endpoint return 200 OK
    // =========================================================
    @Test
    fun testGetTermsOfServiceSuccess() = testApplication {
        application { module(InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val response = client.get("/api/legal/terms")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    // =========================================================
    // TEST 2: terms of service return title
    // =========================================================
    @Test
    fun testGetTermsOfServiceHasTitle() = testApplication {
        application { module(InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val response = client.get("/api/legal/terms")
        val doc = response.body<LegalDocument>()
        assertNotNull(doc.title)
        assertTrue("Judul terms tidak boleh kosong", doc.title.isNotBlank())
    }

    // =========================================================
    // TEST 3: terms of service return version
    // =========================================================
    @Test
    fun testGetTermsOfServiceHasVersion() = testApplication {
        application { module(InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val response = client.get("/api/legal/terms")
        val doc = response.body<LegalDocument>()
        assertNotNull(doc.version)
        assertTrue("Versi terms tidak boleh kosong", doc.version.isNotBlank())
    }

    // =========================================================
    // TEST 4: terms of service return content non-empty
    // =========================================================
    @Test
    fun testGetTermsOfServiceHasContent() = testApplication {
        application { module(InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val response = client.get("/api/legal/terms")
        val doc = response.body<LegalDocument>()
        assertNotNull(doc.content)
        assertTrue("Konten terms tidak boleh kosong", doc.content.isNotBlank())
    }

    // =========================================================
    // TEST 5: privacy policy endpoint return 200 OK
    // =========================================================
    @Test
    fun testGetPrivacyPolicySuccess() = testApplication {
        application { module(InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val response = client.get("/api/legal/privacy")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    // =========================================================
    // TEST 6: privacy policy return title
    // =========================================================
    @Test
    fun testGetPrivacyPolicyHasTitle() = testApplication {
        application { module(InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val response = client.get("/api/legal/privacy")
        val doc = response.body<LegalDocument>()
        assertNotNull(doc.title)
        assertTrue("Judul privacy policy tidak boleh kosong", doc.title.isNotBlank())
    }

    // =========================================================
    // TEST 7: privacy policy return version
    // =========================================================
    @Test
    fun testGetPrivacyPolicyHasVersion() = testApplication {
        application { module(InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val response = client.get("/api/legal/privacy")
        val doc = response.body<LegalDocument>()
        assertNotNull(doc.version)
        assertTrue("Versi privacy policy tidak boleh kosong", doc.version.isNotBlank())
    }

    // =========================================================
    // TEST 8: privacy policy return content non-empty
    // =========================================================
    @Test
    fun testGetPrivacyPolicyHasContent() = testApplication {
        application { module(InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val response = client.get("/api/legal/privacy")
        val doc = response.body<LegalDocument>()
        assertNotNull(doc.content)
        assertTrue("Konten privacy policy tidak boleh kosong", doc.content.isNotBlank())
    }
}
