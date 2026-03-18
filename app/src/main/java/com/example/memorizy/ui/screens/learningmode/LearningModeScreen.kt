package com.example.memorizy.ui.screens.learningmode

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.ui.utils.GlassContainer
import com.example.memorizy.ui.utils.DateUtils

@Composable
fun LearningModeScreen(
    onBackClick: () -> Unit,
    uiState: LearningModeState,
    restartGame: () -> Unit,
    onFlipCard: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    toggleShuffle: () -> Unit,
    toggleReviewMode: () -> Unit
) {
    Scaffold(
        topBar = {
            if (!uiState.isLoading && !uiState.isEmpty && !uiState.isFinished) {
                LearningModeTopBar(
                    uiState = uiState
                )
            }
        }
    ) { paddingValues ->
        LearningModeScreenBody(
            paddingValues = paddingValues,
            uiState = uiState,
            onBackClick = onBackClick,
            restartGame = restartGame,
            onFlipCard = onFlipCard,
            onSwipeRight = onSwipeRight,
            onSwipeLeft = onSwipeLeft,
            toggleShuffle = toggleShuffle,
            toggleReviewMode = toggleReviewMode
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LearningModeTopBar(
    uiState: LearningModeState
){
    CenterAlignedTopAppBar(
        modifier = Modifier.shadow(elevation = 8.dp),
        title = {
            Text(
                text = stringResource(
                    R.string.current_card_counter,
                    uiState.currentIndex + 1,
                    uiState.cards.size
                ),
                style = MaterialTheme.typography.displayMedium
            )
        },
        navigationIcon = {
            Row(
                modifier = Modifier
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.7f))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        R.string.incorrect_answ_counter,
                        uiState.incorrectCount
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        actions = {
            Row(
                modifier = Modifier
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.correct_answ_counter,
                        uiState.correctCount),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.Green.copy(alpha = 0.7f))
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        )
    )
}

@Composable
private fun LearningModeScreenBody(
    paddingValues: PaddingValues,
    uiState: LearningModeState,
    onBackClick: () -> Unit,
    restartGame: () -> Unit,
    onFlipCard: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    toggleShuffle: () -> Unit,
    toggleReviewMode: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            uiState.isEmpty -> {
                EmptyStateMessage(
                    uiState = uiState,
                    onBackClick = onBackClick,
                    toggleReviewMode = toggleReviewMode
                )
            }

            uiState.isFinished -> {
                LearningResultContent(
                    paddingValues = paddingValues,
                    uiState = uiState,
                    onRestartClick = restartGame,
                    onBackClick = onBackClick
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = paddingValues.calculateTopPadding() + 16.dp,
                            bottom = paddingValues.calculateBottomPadding(),
                            start = 16.dp,
                            end = 16.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if ((uiState.currentIndex + 1) < uiState.cards.size) {  // Подложка, если это не последняя карточка
                            LearningCard(
                                term = "",
                                definition = "",
                                isFlipped = false,
                                onCardClick = {},
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }

                        key(uiState.currentCard) {  // Отслеживаем изменение текущей карточки, перерисовываем UI
                            val dismissState = rememberSwipeToDismissBoxState(
                                positionalThreshold = { totalDistance -> totalDistance * 0.5f }
                            )

                            LaunchedEffect(dismissState) {  // Отслеживаем свойства текущего dismissState
                                snapshotFlow {  // Сами отслеживаемые свойства
                                    Pair(
                                        dismissState.progress,
                                        dismissState.targetValue
                                    ) }
                                    .collect { (progress, target) ->
                                        if (progress >= 0.9f
                                            && target == SwipeToDismissBoxValue.StartToEnd) {
                                            onSwipeRight()
                                        }
                                        else if (progress >= 0.9f
                                            && target == SwipeToDismissBoxValue.EndToStart) {
                                            onSwipeLeft()
                                        }
                                    }
                            }

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val color = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.StartToEnd
                                            -> Color.Green.copy(alpha = 0.6f)
                                        SwipeToDismissBoxValue.EndToStart
                                            -> Color.Red.copy(alpha = 0.6f)
                                        else
                                            -> Color.Transparent
                                    }

                                    if (dismissState.targetValue
                                        != SwipeToDismissBoxValue.Settled) {    // Если не базовое положение, а свайпаем
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(color)
                                                .padding(horizontal = 20.dp),
                                            contentAlignment =
                                                if (dismissState.targetValue
                                                    == SwipeToDismissBoxValue.StartToEnd)
                                                    Alignment.Center
                                                else
                                                    Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(
                                                    if (dismissState.targetValue
                                                        == SwipeToDismissBoxValue.StartToEnd)
                                                        R.drawable.ic_confirm
                                                    else
                                                        R.drawable.ic_close
                                                ),
                                                contentDescription =
                                                    stringResource(R.string.ic_swipe_result),
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(48.dp)
                                            )
                                        }
                                    }
                                },
                                content = {
                                    LearningCard(
                                        term = uiState.currentCard!!.term,
                                        definition = uiState.currentCard.definition,
                                        isFlipped = uiState.isFlipped,
                                        onCardClick = onFlipCard,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = toggleShuffle) {
                            Icon(
                                painter = painterResource(R.drawable.ic_shuffle),
                                contentDescription = stringResource(R.string.shuffle_button),
                                tint =
                                    if (uiState.isShuffleOn)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                text = stringResource(R.string.shuffle_button_text),
                                color =
                                    if (uiState.isShuffleOn)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        TextButton(onClick = toggleReviewMode) {
                            Text(
                                text = if (uiState.isReviewMode)
                                    stringResource(R.string.review_mode_text)
                                else
                                    stringResource(R.string.all_cards_mode_text),
                                color = if (uiState.isReviewMode)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(
    uiState: LearningModeState = LearningModeState(),
    onBackClick: () -> Unit,
    toggleReviewMode: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        if (uiState.isReviewMode && uiState.totalCardsCount > 0) {
            Text(
                text = stringResource(R.string.next_review_text),
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))

            if (uiState.nextReviewInMs != null) {
                Text(
                    text = stringResource(
                        R.string.next_review_time_text,
                        DateUtils.formatTimeUntil(uiState.nextReviewInMs)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
            }

            GlassContainer(
                modifier = Modifier
                    .height(40.dp)
                    .width(160.dp),
                onClick = toggleReviewMode,
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Text(
                    text = stringResource(R.string.all_cards_mode_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(Modifier.height(12.dp))

            GlassContainer(
                modifier = Modifier
                    .height(40.dp)
                    .width(160.dp),
                onClick = onBackClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = stringResource(R.string.back_button),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Text(
                text = stringResource(R.string.empty_set_warn),
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.empty_set_sugg),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            GlassContainer(
                modifier = Modifier
                    .height(40.dp)
                    .width(90.dp),
                onClick = onBackClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = stringResource(R.string.back_button),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun LearningResultContent(
    paddingValues: PaddingValues,
    uiState: LearningModeState,
    onBackClick: () -> Unit,
    onRestartClick: () -> Unit,
){
    val total = uiState.cards.size
    val progress = if (total > 0) uiState.correctCount.toFloat() / total else 0f
    val percentage = (progress * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                top = paddingValues.calculateTopPadding() + 24.dp,
                bottom = paddingValues.calculateBottomPadding(),
                start = 24.dp,
                end = 24.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.Result_text),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(32.dp))

        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(200.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 16.dp,
                gapSize = (-15).dp,
            )
            CircularProgressIndicator(  // Накладываем диаграмму на пустую в прошлом виджете
                progress = { progress },
                modifier = Modifier.size(200.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 16.dp,
                gapSize = (-15).dp,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(
                        R.string.learning_percentage_text,
                        percentage),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text =
                        if (percentage >= 75)
                            stringResource(R.string.good_result_text)
                        else
                            stringResource(R.string.bad_result_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                count = uiState.correctCount,
                label = stringResource(R.string.i_know_text),
                color = Color.Green
            )
            StatItem(
                count = uiState.incorrectCount,
                label = stringResource(R.string.i_dont_know_text),
                color = Color.Red
            )
        }

        Spacer(Modifier.height(48.dp))

        GlassContainer(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            onClick = onRestartClick,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    painterResource(R.drawable.ic_refresh), contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.learn_again_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        GlassContainer(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            onClick = onBackClick,
            containerColor = MaterialTheme.colorScheme.secondary
        ) {
            Text(
                text = stringResource(R.string.go_back_to_set_text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun LearningCard(
    term: String,
    definition: String,
    isFlipped: Boolean,
    onCardClick: () -> Unit,
    modifier: Modifier
) {
    val rotation by animateFloatAsState(    // Плавное изменение значения угла поворота карточки
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "cardFlip"
    )

    GlassContainer(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationY = rotation
                cameraDistance =
                    12f * density  // Настройка перспективы, чтобы карточка была будто 3D
            },
        onClick = onCardClick,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        if (rotation <= 90f) {
            CardContent(
                text = term,
                isBackSide = false
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY =
                            180f    // Отзеркаливаем контент внутри карточки (из-за прошлого поворота он в другую сторону)
                    }
            ) {
                CardContent(
                    text = definition,
                    isBackSide = true
                )
            }
        }
    }
}

@Composable
private fun CardContent(
    text: String,
    isBackSide: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style =
                    if (isBackSide)
                        MaterialTheme.typography.bodyMedium
                    else
                        MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StatItem(
    count: Int,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.this_count_text, count),
            style = MaterialTheme.typography.displayMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}