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
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.data.source.local.card.Card


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
                title = { Text(uiState.studySet?.name ?: "Загрузка...") },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Назад"
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
                    contentDescription = "Добавить карточку"
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
    onCardToDelete: (Card) -> Unit
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
                val iconRes = getIconResById(uiState.studySet.iconId)

                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = "Иконка набора"
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
                Text("Заучивание")
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
        Column() {
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
        title = { Text("Удалить карточку?") },
        text = { Text("Вы уверены что хотите удалить карточку? Это действие нельзя откатить назад.")},
        confirmButton = {
            TextButton(
                onClick = onConfirmDelete
            ) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Отменить")
            }
        }
    )
}

// Простая вспомогательная функция (можно положить в конец файла)
fun getIconResById(id: Int): Int {
    return when(id) {
        1 -> R.drawable.random_ic // Замените на ваши реальные иконки
        2 -> R.drawable.random_ic
        3 -> R.drawable.random_ic
        4 -> R.drawable.random_ic
        5 -> R.drawable.random_ic
        else -> R.drawable.random_ic
    }
}