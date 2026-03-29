package com.example.memorizy.ui.screens.carddetailsstatistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.domain.cardrisk.CardRiskAnalyzer.CardRisk
import com.example.memorizy.ui.utils.DateUtils
import com.example.memorizy.ui.utils.GlassContainer

@Composable
fun CardDetailsStatisticsScreen(
    onBackClick: () -> Unit,
    onSortOptionClicked: (CardDetailsSortOption) -> Unit,
    uiState: CardDetailsStatisticsState
) {
    Scaffold(
        topBar = {
            CardDetailsStatisticsTopBar(onBackClick = onBackClick)
        }
    ) { paddingValues ->
        CardDetailsStatisticsBody(
            paddingValues = paddingValues,
            onSortOptionClicked = onSortOptionClicked,
            uiState = uiState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardDetailsStatisticsTopBar(
    onBackClick: () -> Unit
) {
    TopAppBar(
        modifier = Modifier.shadow(elevation = 8.dp),
        title = {
            Text(
                text = stringResource(R.string.card_details_statistics_title),
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
private fun CardDetailsStatisticsBody(
    paddingValues: PaddingValues,
    onSortOptionClicked: (CardDetailsSortOption) -> Unit,
    uiState: CardDetailsStatisticsState
) {
    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.isEmpty -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding(),
                        start = 16.dp,
                        end = 16.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.card_details_statistics_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding(),
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
    selectedOption: CardDetailsSortOption,
    isAscending: Boolean,
    onSortOptionClicked: (CardDetailsSortOption) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
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
                isSelected = selectedOption == CardDetailsSortOption.LEVEL,
                isAscending = isAscending,
                onClick = { onSortOptionClicked(CardDetailsSortOption.LEVEL) }
            )

            SortAction(
                text = stringResource(R.string.card_sort_date),
                isSelected = selectedOption == CardDetailsSortOption.DATE,
                isAscending = isAscending,
                onClick = { onSortOptionClicked(CardDetailsSortOption.DATE) }
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
        modifier = Modifier
            .wrapContentSize(),
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
    card: CardRiskItemUi
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