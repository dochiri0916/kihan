package com.dochiri.kihan.sample

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserRequest(
    val email: String,
    val password: String,
    val name: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val userId: Long,
    val role: String,
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class ReissueRequest(
    val refreshToken: String
)

@Serializable
data class LogoutRequest(
    val refreshToken: String? = null
)

@Serializable
data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val role: String
)

@Serializable
data class RecurrenceRuleResponse(
    val pattern: String,
    val interval: Int,
    val startDate: String,
    val endDate: String? = null
)

@Serializable
data class DeadlineResponse(
    val id: Long,
    val title: String,
    val description: String? = null,
    val type: String,
    val dueDate: String? = null,
    val recurrenceRule: RecurrenceRuleResponse? = null,
    val createdAt: String
)

@Serializable
data class DeadlineRegisterRequest(
    val title: String,
    val description: String? = null,
    val type: String,
    val dueDate: String? = null,
    val pattern: String? = null,
    val interval: Int? = null,
    val startDate: String? = null,
    val endDate: String? = null
)

@Serializable
data class DeadlineUpdateRequest(
    val title: String? = null,
    val description: String? = null
)

@Serializable
data class ExecutionResponse(
    val id: Long,
    val deadlineId: Long,
    val scheduledDate: String,
    val status: String,
    val completedAt: String? = null
)

@Serializable
data class ProblemError(
    val field: String? = null,
    val message: String? = null
)

@Serializable
data class ProblemDetailResponse(
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val instance: String? = null,
    val path: String? = null,
    @SerialName("errors") val errors: List<ProblemError>? = null
)

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Failure(val message: String, val statusCode: Int? = null) : ApiResult<Nothing>
}
