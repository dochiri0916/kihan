package com.dochiri.kihan.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class DeadlineUiModel(
    val id: Long,
    val title: String,
    val dueText: String,
    var done: Boolean
)

@Composable
fun App() {
    MaterialTheme {
        val deadlines = remember {
            mutableStateListOf(
                DeadlineUiModel(1, "아침 운동", "오늘 07:00", false),
                DeadlineUiModel(2, "백엔드 API 점검", "오늘 11:00", false),
                DeadlineUiModel(3, "프로젝트 회고", "내일 18:00", true)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Kihan Android Front (KMP)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "기한 목록 예시",
                style = MaterialTheme.typography.bodyLarge
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(deadlines, key = { it.id }) { item ->
                    DeadlineItem(
                        item = item,
                        onToggle = {
                            item.done = !item.done
                            val index = deadlines.indexOfFirst { it.id == item.id }
                            if (index >= 0) {
                                deadlines[index] = item.copy(done = item.done)
                            }
                        }
                    )
                }
            }

            Button(
                onClick = {
                    deadlines.add(
                        DeadlineUiModel(
                            id = deadlines.size.toLong() + 1,
                            title = "새 기한 ${deadlines.size + 1}",
                            dueText = "이번 주",
                            done = false
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("샘플 기한 추가")
            }
        }
    }
}

@Composable
private fun DeadlineItem(
    item: DeadlineUiModel,
    onToggle: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text(item.dueText, style = MaterialTheme.typography.bodyMedium)
            }
            Checkbox(
                checked = item.done,
                onCheckedChange = { onToggle() }
            )
        }
    }
}
