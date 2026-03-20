package com.example.memorizy.ui.screens.testingmode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.ui.utils.GlassContainer

@Composable
fun TestingModeScreen(
    onBackClick: () -> Unit,
    uiState: TestingModeState,
    onTermsSelected: () -> Unit,
    onDefinitionsSelected: () -> Unit,
    onAnswerChanged: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    restartTesting: () -> Unit,
    showAnswers: () -> Unit
) {
    if (uiState.isShowingAnswers) {
        AnswersReviewContent(
            uiState = uiState,
            onBackClick = onBackClick
        )
    } else {
        val isQuestionActive = !uiState.isLoading && !uiState.isEmpty
                && !uiState.isChoosingMode && !uiState.isFinished

        Scaffold(
            topBar = {
                if (isQuestionActive) {
                    TestingModeTopBar(uiState = uiState)
                }
            }
        ) { paddingValues ->
            TestingModeScreenBody(
                paddingValues = paddingValues,
                uiState = uiState,
                onBackClick = onBackClick,
                onTermsSelected = onTermsSelected,
                onDefinitionsSelected = onDefinitionsSelected,
                onAnswerChanged = onAnswerChanged,
                onSubmitAnswer = onSubmitAnswer,
                restartTesting = restartTesting,
                showAnswers = showAnswers
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestingModeTopBar(
    uiState: TestingModeState
) {
    CenterAlignedTopAppBar(
        modifier = Modifier
            .shadow(elevation = 8.dp),
        title = {
            Text(
                text = stringResource(
                    R.string.testing_question_counter,
                    uiState.currentIndex + 1,
                    uiState.cards.size
                ),
                style = MaterialTheme.typography.displayMedium
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun TestingModeScreenBody(
    paddingValues: PaddingValues,
    uiState: TestingModeState,
    onBackClick: () -> Unit,
    onTermsSelected: () -> Unit,
    onDefinitionsSelected: () -> Unit,
    onAnswerChanged: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    restartTesting: () -> Unit,
    showAnswers: () -> Unit
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
                    onBackClick = onBackClick
                )
            }

            uiState.isChoosingMode -> {
                ChoosingModeContent(
                    onTermsSelected = onTermsSelected,
                    onDefinitionsSelected = onDefinitionsSelected
                )
            }

            uiState.isFinished -> {
                TestingResultContent(
                    uiState = uiState,
                    onRestartClick = restartTesting,
                    onBackClick = onBackClick,
                    onShowAnswers = showAnswers,
                    paddingValues = paddingValues
                )
            }

            else -> {
                QuestionContent(
                    uiState = uiState,
                    onAnswerChanged = onAnswerChanged,
                    onSubmitAnswer = onSubmitAnswer,
                    paddingValues = paddingValues
                )
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(
    onBackClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
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

@Composable
private fun ChoosingModeContent(
    onTermsSelected: () -> Unit,
    onDefinitionsSelected: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.testing_choose_title),
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(32.dp))

        GlassContainer(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            onClick = onTermsSelected,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = stringResource(R.string.testing_terms_button),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(16.dp))

        GlassContainer(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            onClick = onDefinitionsSelected,
            containerColor = MaterialTheme.colorScheme.secondary
        ) {
            Text(
                text = stringResource(R.string.testing_definitions_button),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun QuestionContent(
    uiState: TestingModeState,
    onAnswerChanged: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    paddingValues: PaddingValues
) {
    val currentCard = uiState.currentCard ?: return

    val questionText =
        if (uiState.isTermChecked)
            currentCard.definition
        else
            currentCard.term
    val questionLabel =
        if (uiState.isTermChecked)
            stringResource(R.string.definition_text)
        else
            stringResource(R.string.term_text)
    val answerLabel =
        if (uiState.isTermChecked)
            stringResource(R.string.term_text)
        else
            stringResource(R.string.definition_text)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp,
                start = 24.dp,
                end = 24.dp
            )
    ) {
        Text(
            text = questionLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        GlassContainer(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = questionText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.userAnswer,
                onValueChange = onAnswerChanged,
                label = {
                    Text(
                        text = answerLabel,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.testing_answer_hint),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                modifier = Modifier.weight(1f),
                minLines = 12,
                maxLines = 12,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                shape = RoundedCornerShape(18.dp)
            )

            Spacer(Modifier.width(12.dp))

            GlassContainer(
                modifier = Modifier
                    .size(56.dp),
                onClick = onSubmitAnswer,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = stringResource(R.string.testing_next_button),
                    modifier = Modifier
                        .graphicsLayer { scaleX = -1f },
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
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

@Composable
private fun TestingResultContent(
    uiState: TestingModeState,
    onBackClick: () -> Unit,
    onRestartClick: () -> Unit,
    onShowAnswers: () -> Unit,
    paddingValues: PaddingValues
) {
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
            CircularProgressIndicator(
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
                    text = stringResource(R.string.testing_restart),
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
            onClick = onShowAnswers,
            containerColor = MaterialTheme.colorScheme.secondary
        ) {
            Text(
                text = stringResource(R.string.testing_show_answers),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(Modifier.height(16.dp))

        GlassContainer(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            onClick = onBackClick,
            containerColor = MaterialTheme.colorScheme.tertiary
        ) {
            Text(
                text = stringResource(R.string.go_back_to_set_text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnswersReviewContent(
    uiState: TestingModeState,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .shadow(elevation = 8.dp),
                title = {
                    Text(
                        text = stringResource(R.string.testing_show_answers),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding(),
                start = 16.dp,
                end = 16.dp
            )
        ) {
            itemsIndexed(uiState.userAnswers) { index, testAnswer ->
                val questionText = if (uiState.isTermChecked)
                    testAnswer.card.definition
                else
                    testAnswer.card.term

                val correctAnswer = if (uiState.isTermChecked) {
                    testAnswer.card.term
                } else {
                    buildList {
                        add(testAnswer.card.definition)
                        addAll(testAnswer.card.definitionVariants)
                    }.joinToString(separator = "\n")
                }

                GlassContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    containerColor =
                        if (testAnswer.isCorrect)
                            Color.Green
                        else
                            Color.Red
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "${index + 1}. $questionText",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(8.dp))

                        HorizontalDivider(
                            color = if (testAnswer.isCorrect)
                                Color.Green.copy(alpha = 0.5f)
                            else
                                Color.Red.copy(alpha = 0.5f)
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.testing_your_answer_label),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = testAnswer.userAnswer.ifEmpty { "—" },
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (!testAnswer.isCorrect) {
                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = stringResource(R.string.testing_correct_answer_label),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = correctAnswer,
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(12.dp))

                GlassContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    onClick = onBackClick,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = stringResource(R.string.go_back_to_set_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}