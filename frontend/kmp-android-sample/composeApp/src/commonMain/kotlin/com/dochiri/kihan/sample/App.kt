package com.dochiri.kihan.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun App() {
    MaterialTheme {
        val apiClient = remember { KihanApiClient() }
        DisposableEffect(Unit) {
            onDispose { apiClient.close() }
        }

        var selectedTab by remember { mutableIntStateOf(0) }
        var loading by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf("") }

        var baseUrl by remember { mutableStateOf("http://10.0.2.2:8080") }
        var accessToken by remember { mutableStateOf("") }
        var refreshToken by remember { mutableStateOf("") }

        var authUserId by remember { mutableStateOf("") }
        var authRole by remember { mutableStateOf("") }

        var registerEmail by remember { mutableStateOf("") }
        var registerPassword by remember { mutableStateOf("") }
        var registerName by remember { mutableStateOf("") }

        var loginEmail by remember { mutableStateOf("") }
        var loginPassword by remember { mutableStateOf("") }

        var meText by remember { mutableStateOf("") }
        var lookupUserIdText by remember { mutableStateOf("") }
        var lookupUserText by remember { mutableStateOf("") }

        val deadlines = remember { mutableStateListOf<DeadlineResponse>() }
        var deadlineDetailText by remember { mutableStateOf("") }

        var createTitle by remember { mutableStateOf("") }
        var createDescription by remember { mutableStateOf("") }
        var createType by remember { mutableStateOf("ONE_TIME") }
        var createDueDate by remember { mutableStateOf("2027-12-31T23:59:59") }
        var createPattern by remember { mutableStateOf("WEEKLY") }
        var createInterval by remember { mutableStateOf("1") }
        var createStartDate by remember { mutableStateOf("2027-01-01") }
        var createEndDate by remember { mutableStateOf("2027-12-31") }

        var deadlineIdText by remember { mutableStateOf("") }
        var updateTitle by remember { mutableStateOf("") }
        var updateDescription by remember { mutableStateOf("") }

        val executions = remember { mutableStateListOf<ExecutionResponse>() }
        var executionDetailText by remember { mutableStateOf("") }

        var executionIdText by remember { mutableStateOf("") }
        var executionDeadlineIdText by remember { mutableStateOf("") }
        var executionRangeStart by remember { mutableStateOf("2026-02-01") }
        var executionRangeEnd by remember { mutableStateOf("2026-02-28") }

        val scope = rememberCoroutineScope()

        fun requireToken(): String? {
            if (accessToken.isBlank()) {
                message = "보호 API는 로그인/재발급으로 access token을 먼저 확보해야 합니다."
                return null
            }
            return accessToken
        }

        fun launchApi(call: suspend () -> Unit) {
            scope.launch {
                loading = true
                try {
                    call()
                } catch (e: Exception) {
                    message = "오류: ${e.message}"
                } finally {
                    loading = false
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Kihan KMP Client",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "서버 Base URL (에뮬레이터: http://10.0.2.2:8080)",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("Base URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (loading) {
                Text("요청 처리 중...")
            }

            if (authUserId.isNotBlank() || authRole.isNotBlank()) {
                Text("인증 사용자: id=$authUserId role=$authRole")
            }

            if (message.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Auth") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Users") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Deadlines") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Executions") })
            }

            when (selectedTab) {
                0 -> {
                    SectionCard("회원가입 / 로그인 / 재발급 / 로그아웃") {
                        LabeledTextField("회원가입 이메일", registerEmail) { registerEmail = it }
                        LabeledTextField("회원가입 비밀번호", registerPassword) { registerPassword = it }
                        LabeledTextField("회원가입 이름", registerName) { registerName = it }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    launchApi {
                                        when (
                                            val result = apiClient.register(
                                                baseUrl,
                                                RegisterUserRequest(registerEmail, registerPassword, registerName)
                                            )
                                        ) {
                                            is ApiResult.Success -> message = "회원가입 성공: ${result.data.id}"
                                            is ApiResult.Failure -> message = "회원가입 실패: ${result.message}"
                                        }
                                    }
                                }
                            ) { Text("회원가입") }
                        }

                        LabeledTextField("로그인 이메일", loginEmail) { loginEmail = it }
                        LabeledTextField("로그인 비밀번호", loginPassword) { loginPassword = it }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    launchApi {
                                        when (val result = apiClient.login(baseUrl, LoginRequest(loginEmail, loginPassword))) {
                                            is ApiResult.Success -> {
                                                accessToken = result.data.accessToken
                                                refreshToken = result.data.refreshToken
                                                authUserId = result.data.userId.toString()
                                                authRole = result.data.role
                                                message = "로그인 성공"
                                            }

                                            is ApiResult.Failure -> message = "로그인 실패: ${result.message}"
                                        }
                                    }
                                }
                            ) { Text("로그인") }

                            Button(
                                onClick = {
                                    launchApi {
                                        if (refreshToken.isBlank()) {
                                            message = "재발급하려면 refresh token이 필요합니다."
                                            return@launchApi
                                        }

                                        when (val result = apiClient.reissue(baseUrl, refreshToken)) {
                                            is ApiResult.Success -> {
                                                accessToken = result.data.accessToken
                                                refreshToken = result.data.refreshToken
                                                authUserId = result.data.userId.toString()
                                                authRole = result.data.role
                                                message = "재발급 성공"
                                            }

                                            is ApiResult.Failure -> message = "재발급 실패: ${result.message}"
                                        }
                                    }
                                }
                            ) { Text("토큰 재발급") }

                            Button(
                                onClick = {
                                    launchApi {
                                        when (val result = apiClient.logout(baseUrl, refreshToken.ifBlank { null })) {
                                            is ApiResult.Success -> {
                                                accessToken = ""
                                                refreshToken = ""
                                                authUserId = ""
                                                authRole = ""
                                                message = "로그아웃 성공"
                                            }

                                            is ApiResult.Failure -> message = "로그아웃 실패: ${result.message}"
                                        }
                                    }
                                }
                            ) { Text("로그아웃") }
                        }

                        LabeledTextField("Access Token", accessToken) { accessToken = it }
                        LabeledTextField("Refresh Token", refreshToken) { refreshToken = it }
                    }
                }

                1 -> {
                    SectionCard("유저 조회") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val token = requireToken() ?: return@Button
                                    launchApi {
                                        when (val result = apiClient.getMe(baseUrl, token)) {
                                            is ApiResult.Success -> {
                                                meText = "id=${result.data.id}, email=${result.data.email}, name=${result.data.name}, role=${result.data.role}"
                                                message = "내 정보 조회 성공"
                                            }

                                            is ApiResult.Failure -> message = "내 정보 조회 실패: ${result.message}"
                                        }
                                    }
                                }
                            ) { Text("GET /users/me") }
                        }

                        if (meText.isNotBlank()) {
                            Text(meText, style = MaterialTheme.typography.bodyMedium)
                        }

                        LabeledTextField("조회할 유저 ID", lookupUserIdText) { lookupUserIdText = it }
                        Button(
                            onClick = {
                                val token = requireToken() ?: return@Button
                                val userId = lookupUserIdText.toLongOrNull()
                                if (userId == null) {
                                    message = "유효한 사용자 ID를 입력하세요."
                                    return@Button
                                }
                                launchApi {
                                    when (val result = apiClient.getUserById(baseUrl, token, userId)) {
                                        is ApiResult.Success -> {
                                            lookupUserText = "id=${result.data.id}, email=${result.data.email}, name=${result.data.name}, role=${result.data.role}"
                                            message = "사용자 조회 성공"
                                        }

                                        is ApiResult.Failure -> message = "사용자 조회 실패: ${result.message}"
                                    }
                                }
                            }
                        ) { Text("GET /users/{id}") }

                        if (lookupUserText.isNotBlank()) {
                            Text(lookupUserText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                2 -> {
                    SectionCard("기한 등록") {
                        LabeledTextField("제목", createTitle) { createTitle = it }
                        LabeledTextField("설명", createDescription) { createDescription = it }
                        LabeledTextField("타입 (ONE_TIME/RECURRING)", createType) { createType = it }

                        if (createType == "ONE_TIME") {
                            LabeledTextField("dueDate (YYYY-MM-DDTHH:mm:ss)", createDueDate) { createDueDate = it }
                        } else {
                            LabeledTextField("pattern (DAILY/WEEKLY/MONTHLY/YEARLY)", createPattern) { createPattern = it }
                            LabeledTextField("interval", createInterval) { createInterval = it }
                            LabeledTextField("startDate (YYYY-MM-DD)", createStartDate) { createStartDate = it }
                            LabeledTextField("endDate (선택)", createEndDate) { createEndDate = it }
                        }

                        Button(
                            onClick = {
                                val token = requireToken() ?: return@Button
                                launchApi {
                                    val request = if (createType == "ONE_TIME") {
                                        DeadlineRegisterRequest(
                                            title = createTitle,
                                            description = createDescription.ifBlank { null },
                                            type = "ONE_TIME",
                                            dueDate = createDueDate.ifBlank { null }
                                        )
                                    } else {
                                        DeadlineRegisterRequest(
                                            title = createTitle,
                                            description = createDescription.ifBlank { null },
                                            type = "RECURRING",
                                            pattern = createPattern.ifBlank { null },
                                            interval = createInterval.toIntOrNull(),
                                            startDate = createStartDate.ifBlank { null },
                                            endDate = createEndDate.ifBlank { null }
                                        )
                                    }

                                    when (val result = apiClient.createDeadline(baseUrl, token, request)) {
                                        is ApiResult.Success -> {
                                            message = "기한 등록 성공: ${result.data}"
                                        }

                                        is ApiResult.Failure -> message = "기한 등록 실패: ${result.message}"
                                    }
                                }
                            }
                        ) { Text("POST /deadlines") }
                    }

                    SectionCard("기한 조회/수정/삭제") {
                        Button(
                            onClick = {
                                val token = requireToken() ?: return@Button
                                launchApi {
                                    when (val result = apiClient.listDeadlines(baseUrl, token)) {
                                        is ApiResult.Success -> {
                                            deadlines.clear()
                                            deadlines.addAll(result.data)
                                            message = "기한 목록 조회 성공 (${result.data.size}건)"
                                        }

                                        is ApiResult.Failure -> message = "기한 목록 조회 실패: ${result.message}"
                                    }
                                }
                            }
                        ) { Text("GET /deadlines") }

                        deadlines.forEach { deadline ->
                            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                Text(
                                    text = "#${deadline.id} ${deadline.title} [${deadline.type}]",
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        LabeledTextField("기한 ID", deadlineIdText) { deadlineIdText = it }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val token = requireToken() ?: return@Button
                                    val deadlineId = deadlineIdText.toLongOrNull()
                                    if (deadlineId == null) {
                                        message = "유효한 기한 ID를 입력하세요."
                                        return@Button
                                    }
                                    launchApi {
                                        when (val result = apiClient.getDeadline(baseUrl, token, deadlineId)) {
                                            is ApiResult.Success -> {
                                                val recurrence = result.data.recurrenceRule
                                                deadlineDetailText = buildString {
                                                    append("id=${result.data.id}\n")
                                                    append("title=${result.data.title}\n")
                                                    append("description=${result.data.description}\n")
                                                    append("type=${result.data.type}\n")
                                                    append("dueDate=${result.data.dueDate}\n")
                                                    if (recurrence != null) {
                                                        append("pattern=${recurrence.pattern}, interval=${recurrence.interval}, start=${recurrence.startDate}, end=${recurrence.endDate}\n")
                                                    }
                                                }
                                                message = "기한 상세 조회 성공"
                                            }

                                            is ApiResult.Failure -> message = "기한 상세 조회 실패: ${result.message}"
                                        }
                                    }
                                }
                            ) { Text("GET /deadlines/{id}") }

                            Button(
                                onClick = {
                                    val token = requireToken() ?: return@Button
                                    val deadlineId = deadlineIdText.toLongOrNull()
                                    if (deadlineId == null) {
                                        message = "유효한 기한 ID를 입력하세요."
                                        return@Button
                                    }
                                    launchApi {
                                        when (
                                            val result = apiClient.updateDeadline(
                                                baseUrl,
                                                token,
                                                deadlineId,
                                                DeadlineUpdateRequest(
                                                    title = updateTitle.ifBlank { null },
                                                    description = updateDescription.ifBlank { null }
                                                )
                                            )
                                        ) {
                                            is ApiResult.Success -> message = "기한 수정 성공"
                                            is ApiResult.Failure -> message = "기한 수정 실패: ${result.message}"
                                        }
                                    }
                                }
                            ) { Text("PATCH /deadlines/{id}") }

                            Button(
                                onClick = {
                                    val token = requireToken() ?: return@Button
                                    val deadlineId = deadlineIdText.toLongOrNull()
                                    if (deadlineId == null) {
                                        message = "유효한 기한 ID를 입력하세요."
                                        return@Button
                                    }
                                    launchApi {
                                        when (val result = apiClient.deleteDeadline(baseUrl, token, deadlineId)) {
                                            is ApiResult.Success -> message = "기한 삭제 성공"
                                            is ApiResult.Failure -> message = "기한 삭제 실패: ${result.message}"
                                        }
                                    }
                                }
                            ) { Text("DELETE /deadlines/{id}") }
                        }

                        LabeledTextField("수정 제목", updateTitle) { updateTitle = it }
                        LabeledTextField("수정 설명", updateDescription) { updateDescription = it }

                        if (deadlineDetailText.isNotBlank()) {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Text(deadlineDetailText, modifier = Modifier.padding(10.dp))
                            }
                        }
                    }
                }

                3 -> {
                    SectionCard("실행 조회/상태 변경") {
                        LabeledTextField("실행 ID", executionIdText) { executionIdText = it }
                        LabeledTextField("기한 ID", executionDeadlineIdText) { executionDeadlineIdText = it }
                        LabeledTextField("startDate (YYYY-MM-DD)", executionRangeStart) { executionRangeStart = it }
                        LabeledTextField("endDate (YYYY-MM-DD)", executionRangeEnd) { executionRangeEnd = it }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val token = requireToken() ?: return@Button
                                    val executionId = executionIdText.toLongOrNull()
                                    if (executionId == null) {
                                        message = "유효한 실행 ID를 입력하세요."
                                        return@Button
                                    }
                                    launchApi {
                                        when (val result = apiClient.getExecution(baseUrl, token, executionId)) {
                                            is ApiResult.Success -> {
                                                executionDetailText = "id=${result.data.id}, deadlineId=${result.data.deadlineId}, date=${result.data.scheduledDate}, status=${result.data.status}, completedAt=${result.data.completedAt}"
                                                message = "실행 단건 조회 성공"
                                            }

                                            is ApiResult.Failure -> message = "실행 단건 조회 실패: ${result.message}"
                                        }
                                    }
                                }
                            ) { Text("GET /executions/{id}") }

                            Button(
                                onClick = {
                                    val token = requireToken() ?: return@Button
                                    val executionId = executionIdText.toLongOrNull()
                                    if (executionId == null) {
                                        message = "유효한 실행 ID를 입력하세요."
                                        return@Button
                                    }
                                    launchApi {
                                        when (val result = apiClient.markExecutionDone(baseUrl, token, executionId)) {
                                            is ApiResult.Success -> message = "실행 완료 처리 성공"
                                            is ApiResult.Failure -> message = "실행 완료 처리 실패: ${result.message}"
                                        }
                                    }
                                }
                            ) { Text("PATCH done") }

                            Button(
                                onClick = {
                                    val token = requireToken() ?: return@Button
                                    val executionId = executionIdText.toLongOrNull()
                                    if (executionId == null) {
                                        message = "유효한 실행 ID를 입력하세요."
                                        return@Button
                                    }
                                    launchApi {
                                        when (val result = apiClient.markExecutionDelayed(baseUrl, token, executionId)) {
                                            is ApiResult.Success -> message = "실행 지연 처리 성공"
                                            is ApiResult.Failure -> message = "실행 지연 처리 실패: ${result.message}"
                                        }
                                    }
                                }
                            ) { Text("PATCH delayed") }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val token = requireToken() ?: return@Button
                                    val deadlineId = executionDeadlineIdText.toLongOrNull()
                                    if (deadlineId == null) {
                                        message = "유효한 기한 ID를 입력하세요."
                                        return@Button
                                    }
                                    launchApi {
                                        when (val result = apiClient.getExecutionsByDeadline(baseUrl, token, deadlineId)) {
                                            is ApiResult.Success -> {
                                                executions.clear()
                                                executions.addAll(result.data)
                                                message = "기한별 실행 조회 성공 (${result.data.size}건)"
                                            }

                                            is ApiResult.Failure -> message = "기한별 실행 조회 실패: ${result.message}"
                                        }
                                    }
                                }
                            ) { Text("GET by deadline") }

                            Button(
                                onClick = {
                                    val token = requireToken() ?: return@Button
                                    launchApi {
                                        when (
                                            val result = apiClient.getExecutionsByDateRange(
                                                baseUrl,
                                                token,
                                                executionRangeStart,
                                                executionRangeEnd
                                            )
                                        ) {
                                            is ApiResult.Success -> {
                                                executions.clear()
                                                executions.addAll(result.data)
                                                message = "기간별 실행 조회 성공 (${result.data.size}건)"
                                            }

                                            is ApiResult.Failure -> message = "기간별 실행 조회 실패: ${result.message}"
                                        }
                                    }
                                }
                            ) { Text("GET by range") }
                        }

                        if (executionDetailText.isNotBlank()) {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Text(executionDetailText, modifier = Modifier.padding(10.dp))
                            }
                        }

                        executions.forEach { execution ->
                            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                Text(
                                    text = "#${execution.id} deadline=${execution.deadlineId} date=${execution.scheduledDate} status=${execution.status}",
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine
    )
}
