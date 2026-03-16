package com.example.memorizy.ui.screens.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.ui.utils.DateUtils
import com.example.memorizy.ui.utils.GlassContainer
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer

@Composable
fun StatisticsScreen(
    onBackClick: () -> Unit,
    uiState: StatisticsState
) {
    Scaffold(
        topBar = {
            StatisticsTopBar(onBackClick = onBackClick)
        }
    ) { paddingValues ->
        StatisticsBody(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatisticsTopBar(
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.statistics_title),
                style = MaterialTheme.typography.labelMedium
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = stringResource(R.string.back_button)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun StatisticsBody(
    modifier: Modifier,
    uiState: StatisticsState
) {
    if (uiState.isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionCard(title = stringResource(R.string.level_distribution_title)) {
                LevelDistributionChart(
                    levelDistribution = uiState.levelDistribution,
                    isLevelDistributionEmpty = uiState.isLevelDistributionEmpty
                )
            }
        }
        item {
            SectionCard(title = stringResource(R.string.session_progress_title)) {
                SessionProgressChart(chartData = uiState.sessionChartData)
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    GlassContainer(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun LevelDistributionChart(
    levelDistribution: List<Int>,
    isLevelDistributionEmpty: Boolean
) {
    if (isLevelDistributionEmpty) {
        EmptyState(text = stringResource(R.string.empty_set_warn))
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(levelDistribution) {
        modelProducer.runTransaction {  // Передаем все данные
            columnSeries {
                series(y = levelDistribution)
            }
        }
    }
    val levelLabelFormat = stringResource(R.string.level_label)

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        fill = fill(MaterialTheme.colorScheme.primary),
                        thickness = 8.dp,
                        shape = CorneredShape.rounded(topLeftPercent = 35, topRightPercent = 35)
                    )
                )
            ), // Слой столбиков
            startAxis = VerticalAxis.rememberStart(
                label = rememberTextComponent(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                guideline = null
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberTextComponent(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    String.format(levelLabelFormat, value.toInt())
                },
                guideline = null
            )
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
private fun SessionProgressChart(chartData: SessionChartData) {
    if (chartData.isEmpty) {
        EmptyState(text = stringResource(R.string.not_enough_sessions))
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }
    val learningColor = MaterialTheme.colorScheme.primary
    val testingColor = MaterialTheme.colorScheme.inversePrimary

    LaunchedEffect(chartData) {
        modelProducer.runTransaction {
            lineSeries {
                if (chartData.hasLearning) {
                    series(x = chartData.learningX, y = chartData.learningY)
                }
                if (chartData.hasTesting) {
                    series(x = chartData.testingX, y = chartData.testingY)
                }
            }
        }
    }

    val learningLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(learningColor))
    )
    val testingLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(testingColor))
    )

    val activeLines = buildList {   // Добавляем элементы в список когда надо
        if (chartData.hasLearning) add(learningLine)
        if (chartData.hasTesting) add(testingLine)
    }

    Column {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        *activeLines.toTypedArray() // Распаковываем список, так как series требует конкретные элементы
                    )
                ),
                startAxis = VerticalAxis.rememberStart(
                    label = rememberTextComponent(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    guideline = null
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = rememberTextComponent(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    valueFormatter = CartesianValueFormatter { _, value, _ ->
                        val dayIndex = value.toInt().coerceIn(0, chartData.dateLabels.lastIndex)
                        DateUtils.formatShortDate(chartData.dateLabels[dayIndex])
                    },
                    guideline = null
                )
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (chartData.hasLearning) LegendItem(color = learningColor, label = stringResource(R.string.learning_label))
            if (chartData.hasTesting) LegendItem(color = testingColor, label = stringResource(R.string.testing_label))
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(12.dp),
            color = color,
            shape = CircleShape
        ) {}
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}