package com.example.memorizy.ui.screens.statistics

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.domain.cardrisk.CardRiskAnalyzer.CardRisk
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
    onSortOptionClicked: (StatisticsCardsSortOption) -> Unit,
    uiState: StatisticsState
) {
    Scaffold(
        topBar = {
            StatisticsTopBar(onBackClick = onBackClick)
        }
    ) { paddingValues ->
        StatisticsBody(
            paddingValues = paddingValues,
            onSortOptionClicked = onSortOptionClicked,
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
        modifier = Modifier
            .shadow(elevation = 8.dp),
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

private enum class StatisticsTab {
    General,
    Cards
}

@Composable
private fun StatisticsBody(
    paddingValues: PaddingValues,
    onSortOptionClicked: (StatisticsCardsSortOption) -> Unit,
    uiState: StatisticsState
) {
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    var selectedTab by rememberSaveable { mutableStateOf(StatisticsTab.General) }

    Crossfade(
        targetState = selectedTab,
        label = "statistics_tab_content",
        modifier = Modifier.fillMaxSize()
    ) { tab ->
        when (tab) {
            StatisticsTab.General -> {
                StatisticsChartsContent(
                    paddingValues = paddingValues,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    uiState = uiState
                )
            }

            StatisticsTab.Cards -> {
                StatisticsCardsContent(
                    paddingValues = paddingValues,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onSortOptionClicked = onSortOptionClicked,
                    uiState = uiState.cardsState
                )
            }
        }
    }
}

@Composable
private fun StatisticsTabSelector(
    selectedTab: StatisticsTab,
    onTabSelected: (StatisticsTab) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(4.dp)
    ) {
        val tabWidth = maxWidth / 2
        val indicatorOffset by animateDpAsState(
            targetValue = if (selectedTab == StatisticsTab.General) 0.dp else tabWidth,
            label = "statistics_tab_indicator"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(18.dp)
                )
        )

        Row(modifier = Modifier.fillMaxSize()) {
            StatisticsTabItem(
                text = stringResource(R.string.statistics_tab_general),
                isSelected = selectedTab == StatisticsTab.General,
                onClick = { onTabSelected(StatisticsTab.General) },
                modifier = Modifier.weight(1f)
            )
            StatisticsTabItem(
                text = stringResource(R.string.statistics_tab_cards),
                isSelected = selectedTab == StatisticsTab.Cards,
                onClick = { onTabSelected(StatisticsTab.Cards) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatisticsTabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "statistics_tab_text"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
    }
}

@Composable
private fun StatisticsChartsContent(
    paddingValues: PaddingValues,
    selectedTab: StatisticsTab,
    onTabSelected: (StatisticsTab) -> Unit,
    uiState: StatisticsState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() + 16.dp,
            bottom = paddingValues.calculateBottomPadding(),
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StatisticsTabSelector(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.fillMaxWidth()
            )
        }
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
        animateIn = false,
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
            animateIn = false,
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

@Composable
private fun StatisticsCardsContent(
    paddingValues: PaddingValues,
    selectedTab: StatisticsTab,
    onTabSelected: (StatisticsTab) -> Unit,
    onSortOptionClicked: (StatisticsCardsSortOption) -> Unit,
    uiState: StatisticsCardsState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() + 16.dp,
            bottom = paddingValues.calculateBottomPadding(),
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            StatisticsTabSelector(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.fillMaxWidth()
            )
        }

        when {
            uiState.isLoading -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            uiState.isEmpty -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.card_details_statistics_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                item {
                    SortSection(
                        selectedOption = uiState.sortOption,
                        isAscending = uiState.isAscending,
                        onSortOptionClicked = onSortOptionClicked
                    )
                }
                items(items = uiState.cards, key = { it.id }) { card ->
                    CardRiskItem(card = card)
                }
            }
        }
    }
}

@Composable
private fun SortSection(
    selectedOption: StatisticsCardsSortOption,
    isAscending: Boolean,
    onSortOptionClicked: (StatisticsCardsSortOption) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.card_sort_title),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SortAction(
                text = stringResource(R.string.card_sort_level),
                isSelected = selectedOption == StatisticsCardsSortOption.LEVEL,
                isAscending = isAscending,
                onClick = { onSortOptionClicked(StatisticsCardsSortOption.LEVEL) }
            )

            SortAction(
                text = stringResource(R.string.card_sort_date),
                isSelected = selectedOption == StatisticsCardsSortOption.DATE,
                isAscending = isAscending,
                onClick = { onSortOptionClicked(StatisticsCardsSortOption.DATE) }
            )
        }
    }
}

@Composable
private fun SortAction(
    text: String,
    isSelected: Boolean,
    isAscending: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        modifier = Modifier.wrapContentSize(),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            if (isSelected) {
                Icon(
                    painter = painterResource(R.drawable.ic_sort),
                    contentDescription = if (isAscending) {
                        stringResource(R.string.sort_ascending)
                    } else {
                        stringResource(R.string.sort_descending)
                    },
                    modifier = if (isAscending) {
                        Modifier
                            .padding(start = 4.dp)
                            .scale(scaleX = 1f, scaleY = -1f)
                            .size(18.dp)
                    } else {
                        Modifier
                            .padding(start = 4.dp)
                            .size(18.dp)
                    },
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CardRiskItem(
    card: StatisticsCardItemUi
) {
    val riskColor = riskColor(card.risk)

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
                text = card.term,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.card_level_value, card.level),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.card_risk_value, riskLabel(card.risk)),
                    style = MaterialTheme.typography.labelSmall,
                    color = riskColor
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(
                    R.string.card_next_review_value,
                    DateUtils.formatFullDateTime(card.nextReviewDate)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun riskLabel(risk: CardRisk): String {
    return when (risk) {
        CardRisk.HIGH -> stringResource(R.string.card_knowledge_low)
        CardRisk.MEDIUM -> stringResource(R.string.card_knowledge_medium)
        CardRisk.LOW -> stringResource(R.string.card_knowledge_high)
    }
}

@Composable
private fun riskColor(risk: CardRisk): Color {
    return when (risk) {
        CardRisk.HIGH -> MaterialTheme.colorScheme.error
        CardRisk.MEDIUM -> MaterialTheme.colorScheme.secondary
        CardRisk.LOW -> MaterialTheme.colorScheme.primary
    }
}