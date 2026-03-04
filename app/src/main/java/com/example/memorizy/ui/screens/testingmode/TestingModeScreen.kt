package com.example.memorizy.ui.screens.testingmode

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.ui.utils.AppIcon
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
                modifier = Modifier.padding(paddingValues),
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
private fun AnswersReviewContent(
    uiState: TestingModeState,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.testing_show_answers),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            itemsIndexed(uiState.userAnswers) { index, testAnswer ->
                val questionText = if (uiState.isTermChecked)
                    testAnswer.card.definition
                else
                    testAnswer.card.term

                val correctAnswer = if (uiState.isTermChecked)
                    testAnswer.card.term
                else
                    testAnswer.card.definition

                GlassContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
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
                            style = MaterialTheme.typography.bodyMedium
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
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (testAnswer.isCorrect)
                                Color.Green.copy(alpha = 0.7f)
                            else
                                Color.Red.copy(alpha = 0.7f)
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
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Green.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(24.dp))

                GlassContainer(
                    modifier = Modifier
                        .clickable(onClick = onBackClick)
                        .fillMaxWidth()
                        .height(50.dp),
                    containerColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Text(
                        text = stringResource(R.string.go_back_to_set_text),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestingModeTopBar(
    uiState: TestingModeState
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(
                    R.string.testing_question_counter,
                    uiState.currentIndex + 1,
                    uiState.cards.size
                ),
                style = MaterialTheme.typography.displayMedium
            )
        }
    )
}

@Composable
private fun TestingModeScreenBody(
    modifier: Modifier,
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
        modifier = modifier
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
                    onShowAnswers = showAnswers
                )
            }

            else -> {
                QuestionContent(
                    modifier = Modifier,
                    uiState = uiState,
                    onAnswerChanged = onAnswerChanged,
                    onSubmitAnswer = onSubmitAnswer
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
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.empty_set_sugg),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        GlassContainer(
            modifier = Modifier
                .clickable(onClick = onBackClick)
                .height(40.dp)
                .width(90.dp),
        ) {
            Text(
                text = stringResource(R.string.back_button),
                style = MaterialTheme.typography.bodySmall
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
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        GlassContainer(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onTermsSelected)
                .height(50.dp),
            containerColor = MaterialTheme.colorScheme.secondary
        ) {
            Text(
                text = stringResource(R.string.testing_terms_button),
                style = MaterialTheme.typography.displayLarge
            )
        }

        Spacer(Modifier.height(16.dp))

        GlassContainer(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDefinitionsSelected)
                .height(50.dp),
            containerColor = MaterialTheme.colorScheme.secondary
        ) {
            Text(
                text = stringResource(R.string.testing_definitions_button),
                style = MaterialTheme.typography.displayLarge
            )
        }
    }
}

@Composable
private fun TestingResultContent(
    uiState: TestingModeState,
    onBackClick: () -> Unit,
    onRestartClick: () -> Unit,
    onShowAnswers: () -> Unit
) {
    val total = uiState.cards.size
    val progress = if (total > 0) uiState.correctCount.toFloat() / total else 0f
    val percentage = (progress * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.Result_text),
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(Modifier.height(32.dp))

        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(200.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 16.dp,
            )
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(200.dp),
                color = if (progress >= 0.85f) Color.Green else MaterialTheme.colorScheme.primary,
                strokeWidth = 16.dp,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(
                        R.string.learning_percentage_text,
                        percentage),
                    style = MaterialTheme.typography.displayMedium
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
                .clickable(onClick = onRestartClick)
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(painterResource(R.drawable.ic_refresh), contentDescription = null)

                Spacer(Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.testing_restart),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        GlassContainer(
            modifier = Modifier
                .clickable(onClick = onShowAnswers)
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = stringResource(R.string.testing_show_answers),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(16.dp))

        GlassContainer(
            modifier = Modifier
                .clickable(onClick = onBackClick)
                .fillMaxWidth()
                .height(50.dp),
            containerColor = MaterialTheme.colorScheme.onSurface
        ) {
            Text(
                text = stringResource(R.string.go_back_to_set_text),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun QuestionContent(
    modifier: Modifier,
    uiState: TestingModeState,
    onAnswerChanged: (String) -> Unit,
    onSubmitAnswer: () -> Unit
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
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = questionLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        GlassContainer(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = questionText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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
                    .size(56.dp)
                    .clickable(onClick = onSubmitAnswer),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                AppIcon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = stringResource(R.string.testing_next_button),
                    modifier = Modifier
                        .graphicsLayer { scaleX = -1f }
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