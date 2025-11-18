package com.example.memorizy.ui.setdetails

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.data.source.local.card.Card
import com.example.memorizy.ui.utils.AppIcons.getIconResById


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetDetailsScreen(
    onAddCardClick: () -> Unit,
    onBackClick: () -> Unit,
    uiState: SetDetailsState,
    onDeleteCard: (Card) -> Unit
){
    // Переменные для логики интерфейса
    var cardToDelete by remember { mutableStateOf<Card?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.set_detalization_text) ?: stringResource(R.string.loading_text)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCardClick // Навигация
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.add_card_button)
                )
            }
        }
    ) { paddingValues ->
        SetDetailsScreenBody(
            modifier = Modifier
                .padding(paddingValues),
            uiState = uiState,
            onCardToDelete = { studySet ->
                cardToDelete = studySet
            }
        )
    }

    if (cardToDelete != null){
        DeleteCardDialog(
            onConfirmDelete = {
                onDeleteCard(cardToDelete!!)
                cardToDelete = null
            },
            onDismiss = {
                cardToDelete = null
            }
        )
    }
}

@Composable
fun SetDetailsScreenBody(
    modifier: Modifier,
    uiState: SetDetailsState,
    onCardToDelete: (Card) -> Unit,
    getIconRes: (Int) -> (Int) = { getIconResById(it) }
){
    if (uiState.isLoading || uiState.studySet == null){
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            CircularProgressIndicator()
        }
    }
    else{
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    painter = painterResource(getIconRes(uiState.studySet.iconId)),
                    contentDescription = stringResource(R.string.set_icon),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = uiState.studySet.name
                )
            }

            if (!uiState.studySet.description.isNullOrBlank()) {

                Spacer(Modifier.height(8.dp))

                Text(
                    text = uiState.studySet.description,
                )
            }

            Spacer(Modifier.height(16.dp))

            Button( // Кнопка заучивание
                onClick = { /* Пока неактивна */ },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.learning_mode))
            }

            HorizontalDivider()

            LazyColumn(
                modifier = Modifier
                    .weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                // используем перебор с ключем, так как в случае удаления карточки LazyColumn поймет
                // какой именно элемент пропал и что именно нужно перерисовать
                items(items = uiState.cards, key = { it.id } ) { card ->
                    CardItem(
                        card = card,
                        onLongClick = { onCardToDelete(card) }  // обозначить этот набор для удаления
                    )
                }
            }
        }
    }
}

@Composable
fun CardItem(
    card: Card,
    onLongClick: () -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = onLongClick
            )
    ) {
        Column {
            Text(
                text = card.term
            )

            Spacer(Modifier.height(4.dp))

            HorizontalDivider()

            Spacer(Modifier.height(4.dp))

            Text(
                text = card.definition
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteCardDialog(
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
){
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_card_question)) },
        text = { Text(stringResource(R.string.delete_card_warning))},
        confirmButton = {
            TextButton(
                onClick = onConfirmDelete
            ) {
                Text(stringResource(R.string.delete_text))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel_text))
            }
        }
    )
}