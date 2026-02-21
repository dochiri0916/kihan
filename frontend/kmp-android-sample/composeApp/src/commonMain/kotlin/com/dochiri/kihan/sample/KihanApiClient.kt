package com.dochiri.kihan.sample

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class KihanApiClient {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        explicitNulls = false
    }

    private val client = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("KihanApi: $message")
                }
            }
            level = LogLevel.INFO
        }
    }

    suspend fun register(baseUrl: String, request: RegisterUserRequest): ApiResult<UserResponse> {
        val response = client.post("${normalizeBaseUrl(baseUrl)}/api/users/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return parseResponse(response)
    }

    suspend fun login(baseUrl: String, request: LoginRequest): ApiResult<AuthResponse> {
        val response = client.post("${normalizeBaseUrl(baseUrl)}/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return parseResponse(response)
    }

    suspend fun reissue(baseUrl: String, refreshToken: String): ApiResult<AuthResponse> {
        val response = client.post("${normalizeBaseUrl(baseUrl)}/api/auth/reissue") {
            contentType(ContentType.Application.Json)
            setBody(ReissueRequest(refreshToken))
        }
        return parseResponse(response)
    }

    suspend fun logout(baseUrl: String, refreshToken: String?): ApiResult<Unit> {
        val response = client.post("${normalizeBaseUrl(baseUrl)}/api/auth/logout") {
            contentType(ContentType.Application.Json)
            setBody(LogoutRequest(refreshToken))
        }
        return parseUnitResponse(response)
    }

    suspend fun getMe(baseUrl: String, accessToken: String): ApiResult<UserResponse> {
        val response = client.get("${normalizeBaseUrl(baseUrl)}/api/users/me") {
            bearerAuth(accessToken)
        }
        return parseResponse(response)
    }

    suspend fun getUserById(baseUrl: String, accessToken: String, userId: Long): ApiResult<UserResponse> {
        val response = client.get("${normalizeBaseUrl(baseUrl)}/api/users/$userId") {
            bearerAuth(accessToken)
        }
        return parseResponse(response)
    }

    suspend fun createDeadline(baseUrl: String, accessToken: String, request: DeadlineRegisterRequest): ApiResult<String> {
        val response = client.post("${normalizeBaseUrl(baseUrl)}/api/deadlines") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        if (response.status == HttpStatusCode.Created) {
            val location = response.headers["Location"].orEmpty()
            return ApiResult.Success(location)
        }

        return parseFailure(response)
    }

    suspend fun listDeadlines(baseUrl: String, accessToken: String): ApiResult<List<DeadlineResponse>> {
        val response = client.get("${normalizeBaseUrl(baseUrl)}/api/deadlines") {
            bearerAuth(accessToken)
        }
        return parseResponse(response)
    }

    suspend fun getDeadline(baseUrl: String, accessToken: String, deadlineId: Long): ApiResult<DeadlineResponse> {
        val response = client.get("${normalizeBaseUrl(baseUrl)}/api/deadlines/$deadlineId") {
            bearerAuth(accessToken)
        }
        return parseResponse(response)
    }

    suspend fun updateDeadline(
        baseUrl: String,
        accessToken: String,
        deadlineId: Long,
        request: DeadlineUpdateRequest
    ): ApiResult<Unit> {
        val response = client.patch("${normalizeBaseUrl(baseUrl)}/api/deadlines/$deadlineId") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return parseUnitResponse(response)
    }

    suspend fun deleteDeadline(baseUrl: String, accessToken: String, deadlineId: Long): ApiResult<Unit> {
        val response = client.delete("${normalizeBaseUrl(baseUrl)}/api/deadlines/$deadlineId") {
            bearerAuth(accessToken)
        }
        return parseUnitResponse(response)
    }

    suspend fun getExecution(baseUrl: String, accessToken: String, executionId: Long): ApiResult<ExecutionResponse> {
        val response = client.get("${normalizeBaseUrl(baseUrl)}/api/executions/$executionId") {
            bearerAuth(accessToken)
        }
        return parseResponse(response)
    }

    suspend fun getExecutionsByDeadline(
        baseUrl: String,
        accessToken: String,
        deadlineId: Long
    ): ApiResult<List<ExecutionResponse>> {
        val response = client.get("${normalizeBaseUrl(baseUrl)}/api/executions/deadline/$deadlineId") {
            bearerAuth(accessToken)
        }
        return parseResponse(response)
    }

    suspend fun getExecutionsByDateRange(
        baseUrl: String,
        accessToken: String,
        startDate: String,
        endDate: String
    ): ApiResult<List<ExecutionResponse>> {
        val response = client.get("${normalizeBaseUrl(baseUrl)}/api/executions") {
            bearerAuth(accessToken)
            url {
                parameters.append("startDate", startDate)
                parameters.append("endDate", endDate)
            }
        }
        return parseResponse(response)
    }

    suspend fun markExecutionDone(baseUrl: String, accessToken: String, executionId: Long): ApiResult<Unit> {
        val response = client.patch("${normalizeBaseUrl(baseUrl)}/api/executions/$executionId/done") {
            bearerAuth(accessToken)
        }
        return parseUnitResponse(response)
    }

    suspend fun markExecutionDelayed(baseUrl: String, accessToken: String, executionId: Long): ApiResult<Unit> {
        val response = client.patch("${normalizeBaseUrl(baseUrl)}/api/executions/$executionId/delayed") {
            bearerAuth(accessToken)
        }
        return parseUnitResponse(response)
    }

    fun close() {
        client.close()
    }

    private suspend inline fun <reified T> parseResponse(response: HttpResponse): ApiResult<T> {
        return if (response.status.value in 200..299) {
            try {
                ApiResult.Success(response.body())
            } catch (e: Exception) {
                ApiResult.Failure("응답 파싱 실패: ${e.message}", response.status.value)
            }
        } else {
            parseFailure(response)
        }
    }

    private suspend fun parseUnitResponse(response: HttpResponse): ApiResult<Unit> {
        return if (response.status.value in 200..299) {
            ApiResult.Success(Unit)
        } else {
            parseFailure(response)
        }
    }

    private suspend fun parseFailure(response: HttpResponse): ApiResult.Failure {
        val bodyText = response.bodyAsText()
        val problem = runCatching { json.decodeFromString<ProblemDetailResponse>(bodyText) }.getOrNull()

        val validationErrors = problem?.errors
            ?.mapNotNull { error ->
                if (error.field.isNullOrBlank() || error.message.isNullOrBlank()) {
                    null
                } else {
                    "${error.field}: ${error.message}"
                }
            }
            .orEmpty()

        val detail = when {
            validationErrors.isNotEmpty() -> validationErrors.joinToString("\n")
            !problem?.detail.isNullOrBlank() -> problem?.detail.orEmpty()
            bodyText.isNotBlank() -> bodyText
            else -> "요청 실패 (${response.status.value})"
        }

        return ApiResult.Failure(detail, response.status.value)
    }

    private fun normalizeBaseUrl(baseUrl: String): String {
        val trimmed = baseUrl.trim()
        return trimmed.replace("10.0.0.2", "10.0.2.2").trimEnd('/')
    }
}
