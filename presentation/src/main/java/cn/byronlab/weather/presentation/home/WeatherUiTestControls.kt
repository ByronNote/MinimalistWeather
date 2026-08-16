package cn.byronlab.weather.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.byronlab.weather.presentation.weatherui.icons.WeatherConditionIcon
import cn.byronlab.weather.presentation.weatherui.tokens.WeatherVisualStyle

@Composable
internal fun WeatherUiTestLauncher(
    activeScenario: WeatherUiTestScenario?,
    style: WeatherVisualStyle,
    onClick: () -> Unit,
    onStopTesting: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = style.panelColor.copy(alpha = 0.94f),
        contentColor = style.panelContent,
        border = BorderStroke(1.dp, style.panelContent.copy(alpha = 0.14f)),
        shadowElevation = 5.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(
                start = 12.dp,
                top = 8.dp,
                end = if (activeScenario == null) 14.dp else 4.dp,
                bottom = 8.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Science,
                contentDescription = null,
                modifier = Modifier.size(19.dp),
                tint = style.accent,
            )
            Spacer(modifier = Modifier.width(7.dp))
            Column {
                Text(
                    text = activeScenario?.name ?: "天气测试",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                if (activeScenario != null) {
                    Text(
                        text = "点击切换场景",
                        color = style.panelContent.copy(alpha = 0.62f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            if (activeScenario != null) {
                IconButton(
                    modifier = Modifier.size(36.dp),
                    onClick = onStopTesting,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "退出天气测试",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
internal fun WeatherUiTestDialog(
    activeScenario: WeatherUiTestScenario?,
    onDismiss: () -> Unit,
    onScenarioSelected: (WeatherUiTestScenario) -> Unit,
    onStopTesting: () -> Unit,
) {
    var showDaytime by remember(activeScenario?.id) {
        mutableStateOf(activeScenario?.isDay ?: true)
    }
    val visibleScenarios = remember(showDaytime) {
        WeatherUiTestCatalog.scenarios.filter { it.isDay == showDaytime }
    }
    val groupedScenarios = remember(visibleScenarios) { visibleScenarios.groupBy { it.category } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    Text(text = "天气效果测试", fontWeight = FontWeight.Bold)
                    Text(
                        text = "24 种天气均可独立验证白天和夜间",
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                WeatherUiTestPhaseSelector(
                    showDaytime = showDaytime,
                    onPhaseSelected = { showDaytime = it },
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                groupedScenarios.forEach { (category, scenarios) ->
                    item(key = "category-$category") {
                        Text(
                            text = category,
                            modifier = Modifier.padding(start = 4.dp, top = 10.dp, bottom = 2.dp),
                            color = Color(0xFF64748B),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    items(
                        items = scenarios,
                        key = WeatherUiTestScenario::id,
                    ) { scenario ->
                        WeatherUiTestScenarioRow(
                            scenario = scenario,
                            selected = scenario.id == activeScenario?.id,
                            onClick = { onScenarioSelected(scenario) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        dismissButton = if (activeScenario == null) {
            null
        } else {
            {
                TextButton(onClick = onStopTesting) {
                    Text("恢复实时天气")
                }
            }
        },
        containerColor = Color.White,
    )
}

@Composable
private fun WeatherUiTestPhaseSelector(
    showDaytime: Boolean,
    onPhaseSelected: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F5F9), RoundedCornerShape(14.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        listOf(true to "白天", false to "夜间").forEach { (isDay, label) ->
            val selected = showDaytime == isDay
            Surface(
                modifier = Modifier.weight(1f),
                onClick = { onPhaseSelected(isDay) },
                shape = RoundedCornerShape(11.dp),
                color = if (selected) Color.White else Color.Transparent,
                contentColor = if (selected) Color(0xFF0F1E33) else Color(0xFF64748B),
                shadowElevation = if (selected) 1.dp else 0.dp,
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun WeatherUiTestScenarioRow(
    scenario: WeatherUiTestScenario,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val weather = remember(scenario.id) { WeatherUiTestCatalog.createPreview(scenario) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Color(0xFFE8F3FF) else Color(0xFFF8FAFC),
        border = BorderStroke(
            1.dp,
            if (selected) Color(0xFF72AEE8) else Color(0xFFE5EBF3),
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color.White, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center,
            ) {
                WeatherConditionIcon(
                    scene = weather.scene,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(modifier = Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scenario.weatherName,
                    color = Color(0xFF0F1E33),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = scenario.description,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "当前场景",
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF2878C8),
                )
            }
        }
    }
}
