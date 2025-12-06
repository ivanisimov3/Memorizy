package com.example.memorizy.ui.screens.setdetails

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.ui.utils.AppIcons
import com.example.memorizy.ui.utils.AppIcons.getIconResById
import kotlin.collections.component2
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.saveable.rememberSaveable


@Composable
fun SetDetailsScreen(
    onAddCardClick: () -> Unit,
    onBackClick: () -> Unit,
    uiState: SetDetailsState,
    onDeleteCard: (Card) -> Unit,
    onStartEditing: () -> Unit,
    updateDraftName: (String) -> Unit,
    updateDraftDescription: (String) -> Unit,
    updateDraftIcon: (Int) -> Unit,
    updateDraftCard: (Int, String, String) -> Unit,
    onCancelEditing: () -> Unit,
    onSaveChanges: () -> Unit
){
    // Переменные для логики интерфейса
    var cardToDelete by remember { mutableStateOf<Card?>(null) }
    var showIconDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SetDetailsTopBar(
                onBackClick = onBackClick,
                isEditing = uiState.isEditing,
                onCancelEditing = onCancelEditing,
                onSaveChanges = onSaveChanges,
                onStartEditing = onStartEditing
            )
        },
        floatingActionButton = {
            if (!uiState.isEditing){
                FloatingActionButton(
                    onClick = onAddCardClick // Навигация
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(R.string.add_card_button)
                    )
                }
            }
        }
    ) { paddingValues ->
        SetDetailsScreenBody(
            modifier = Modifier
                .padding(paddingValues),
            uiState = uiState,
            onCardToDelete = { studySet ->
                cardToDelete = studySet
            },
            updateDraftName = updateDraftName,
            updateDraftDescription = updateDraftDescription,
            updateDraftCard = updateDraftCard,
            onIconClick = {
                if (uiState.isEditing) showIconDialog = true
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

    if (showIconDialog && uiState.draftSet != null) {
        IconSelectionDialog(
            currentIconId = uiState.draftSet.iconId,
            onIconSelected = { newIconId ->
                updateDraftIcon(newIconId)
                showIconDialog = false
            },
            onDismiss = {
                showIconDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetDetailsTopBar(
    onBackClick: () -> Unit,
    onCancelEditing: () -> Unit,
    onSaveChanges: () -> Unit,
    onStartEditing: () -> Unit,
    isEditing: Boolean
) {
    TopAppBar(
        title = {
            Text(
                text =
                    if (isEditing)
                        stringResource(R.string.set_edit_mode)
                    else
                        stringResource(R.string.set_detalization_text),
                style = MaterialTheme.typography.displayMedium
            )
        },
        navigationIcon = {
            if (!isEditing){
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        },
        actions = {
            if (isEditing) {
                IconButton(onClick = onCancelEditing) {
                    Icon(
                        painter = painterResource(R.drawable.ic_cancel),
                        contentDescription = stringResource(R.string.cancel_edit_mode)
                    )
                }
                IconButton(onClick = onSaveChanges) {
                    Icon(
                        painter = painterResource(R.drawable.ic_confirm),
                        contentDescription = stringResource(R.string.confirm_edit)
                    )
                }
            } else {
                IconButton(onClick = onStartEditing) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = stringResource(R.string.start_edit_mode)
                    )
                }
            }
        }
    )
}

@Composable
private fun SetDetailsScreenBody(
    modifier: Modifier,
    uiState: SetDetailsState,
    onCardToDelete: (Card) -> Unit,
    getIconRes: (Int) -> (Int) = { getIconResById(it) },
    updateDraftName: (String) -> Unit,
    updateDraftDescription: (String) -> Unit,
    updateDraftCard: (Int, String, String) -> Unit,
    onIconClick: () -> Unit
){
    val editMode = (uiState.isEditing && uiState.draftSet != null)

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
                    painter = painterResource(getIconRes(
                        if (editMode)
                            uiState.draftSet.iconId
                        else
                            uiState.studySet.iconId)
                    ),
                    contentDescription = stringResource(R.string.set_icon),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .then(  // Returns a Modifier representing this modifier followed by other in sequence.
                            if (editMode) Modifier
                                .clip(CircleShape)
                                .clickable { onIconClick() }
                            else Modifier
                        )
                )
                Spacer(Modifier.width(16.dp))
                if (editMode){
                    OutlinedTextField(
                        value = uiState.draftSet.name,
                        onValueChange = updateDraftName,
                        label = { Text(
                            text = stringResource(R.string.set_name_field),
                            style = MaterialTheme.typography.displayMedium
                        )},
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                } else{
                    Text(
                        text = uiState.studySet.name,
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }

            if (editMode){
                OutlinedTextField(
                    value = uiState.draftSet.description ?: "",
                    onValueChange = updateDraftDescription,
                    label = { Text(
                        text = stringResource(R.string.set_description),
                        style = MaterialTheme.typography.displayMedium
                    )},
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            } else{
                if (!uiState.studySet.description.isNullOrBlank()) {

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = uiState.studySet.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (!editMode){
                Button( // Кнопка заучивание
                    onClick = { /* Пока неактивна */ },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.learning_mode),
                        style = MaterialTheme.typography.displayMedium
                    )
                }

                Spacer(Modifier.height(4.dp))
            }

            HorizontalDivider()


            LazyColumn(
                modifier = Modifier
                    .weight(1f)
            ) {
                if (editMode){
                    itemsIndexed(items = uiState.draftCards) { index, draftCard ->
                        EditCardItem(
                            draftCard = draftCard,
                            onTermChange = { updateDraftCard(index, it, draftCard.definition) },
                            onDefChange = { updateDraftCard(index, draftCard.term, it) }
                        )
                    }
                } else{
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
}

@Composable
private fun CardItem(
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
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.secondary)
    ) {
        Column (
            modifier = Modifier
                .padding(8.dp)
        ){
            Text(
                text = card.term,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            HorizontalDivider()

            Spacer(Modifier.height(4.dp))

            Text(
                text = card.definition,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun EditCardItem(
    onTermChange: (String) -> Unit,
    onDefChange: (String) -> Unit,
    draftCard: Card
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            OutlinedTextField(
                value = draftCard.term,
                onValueChange = onTermChange,
                label = {
                    Text(
                        text = stringResource(R.string.term_text),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = draftCard.definition,
                onValueChange = onDefChange,
                label = {
                    Text(
                        text = stringResource(R.string.definition_text),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                minLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DeleteCardDialog(
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

@Composable
fun IconSelectionDialog(
    currentIconId: Int,
    onIconSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    availableIcons: Map<Int, Int> = AppIcons.allIcons
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_ic_text)) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                availableIcons.forEach { (id, iconResId) ->
                    Icon(
                        painter = painterResource(iconResId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { onIconSelected(id) },
                        tint = if (id == currentIconId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel_text))
            }
        }
    )
}