package com.example.memorizy.ui.screens.setdetails

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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.ui.utils.AppIcons
import com.example.memorizy.ui.utils.AppIcons.getIconResById
import kotlin.collections.component2
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.memorizy.ui.utils.AppIcon
import com.example.memorizy.ui.utils.GlassContainer
import com.example.memorizy.ui.utils.DateUtils

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
    updateDraftTargetDate: (Long?) -> Unit,
    updateDraftCard: (Int, String, String) -> Unit,
    onCancelEditing: () -> Unit,
    onSaveChanges: () -> Unit,
    onLearningModeClick: () -> Unit,
    onTestingModeClick: () -> Unit
){
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
                GlassContainer(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .clickable(onClick = onAddCardClick)
                        .size(56.dp),
                ) {
                    AppIcon(
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
            updateDraftTargetDate = updateDraftTargetDate,
            updateDraftCard = updateDraftCard,
            onIconClick = {
                if (uiState.isEditing) showIconDialog = true
            },
            onLearningModeClick = onLearningModeClick,
            onTestingModeClick = onTestingModeClick
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
private fun SetDetailsTopBar(
    onBackClick: () -> Unit,
    onCancelEditing: () -> Unit,
    onSaveChanges: () -> Unit,
    onStartEditing: () -> Unit,
    isEditing: Boolean
){
    TopAppBar(
        title = {
            Text(
                text =
                    if (isEditing)
                        stringResource(R.string.set_edit_mode)
                    else
                        stringResource(R.string.set_detalization_text),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        },
        navigationIcon = {
            if (!isEditing){
                IconButton(onClick = onBackClick) {
                    AppIcon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        },
        actions = {
            if (isEditing) {
                IconButton(onClick = onCancelEditing) {
                    AppIcon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.cancel_edit_mode)
                    )
                }
                IconButton(onClick = onSaveChanges) {
                    AppIcon(
                        painter = painterResource(R.drawable.ic_confirm),
                        contentDescription = stringResource(R.string.confirm_edit)
                    )
                }
            } else {
                IconButton(onClick = onStartEditing) {
                    AppIcon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = stringResource(R.string.start_edit_mode)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetDetailsScreenBody(
    modifier: Modifier,
    uiState: SetDetailsState,
    onCardToDelete: (Card) -> Unit,
    getIconRes: (Int) -> (Int) = { getIconResById(it) },
    updateDraftName: (String) -> Unit,
    updateDraftDescription: (String) -> Unit,
    updateDraftTargetDate: (Long?) -> Unit,
    updateDraftCard: (Int, String, String) -> Unit,
    onIconClick: () -> Unit,
    onLearningModeClick: () -> Unit,
    onTestingModeClick: () -> Unit
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
        LazyColumn(
            modifier = modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Column{
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(
                                getIconRes(
                                    if (editMode)
                                        uiState.draftSet.iconId
                                    else
                                        uiState.studySet.iconId
                                )
                            ),
                            contentDescription = stringResource(R.string.set_icon),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .then(  // Объединяет этот модификатор с другим
                                    if (editMode) Modifier
                                        .clip(CircleShape)
                                        .clickable { onIconClick() }
                                        .size(48.dp)
                                    else Modifier
                                        .size(48.dp)
                                )
                        )
                        Spacer(Modifier.width(16.dp))
                        if (editMode) {
                            OutlinedTextField(
                                value = uiState.draftSet.name,
                                onValueChange = updateDraftName,
                                label = {
                                    Text(
                                        text = stringResource(R.string.set_name_field),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                isError = uiState.draftSet.name.isEmpty(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )
                        } else {
                            Text(
                                text = uiState.studySet.name,
                                style = MaterialTheme.typography.displayMedium
                            )
                        }
                    }

                    if (editMode) {
                        OutlinedTextField(
                            value = uiState.draftSet.description ?: "",
                            onValueChange = updateDraftDescription,
                            label = {
                                Text(
                                    text = stringResource(R.string.set_description),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 5,
                            maxLines = 5,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )
                    } else {
                        if (!uiState.studySet.description.isNullOrBlank()) {

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = uiState.studySet.description,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    if (editMode) {
                        Spacer(Modifier.height(8.dp))
                        TargetDateEditor(
                            currentTargetDate = uiState.draftSet.targetDate,
                            onTargetDateChanged = updateDraftTargetDate
                        )
                    } else {
                        val targetDate = uiState.studySet.targetDate
                        if (targetDate != null) {
                            Spacer(Modifier.height(8.dp))
                            val dateStr = DateUtils.formatShortDate(targetDate)
                            Text(
                                text = stringResource(R.string.target_date_display, dateStr),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    if (!editMode) {
                        GlassContainer (
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onLearningModeClick)
                                .height(50.dp),
                            containerColor = MaterialTheme.colorScheme.secondary
                        ) {
                            Text(
                                stringResource(R.string.learning_mode),
                                style = MaterialTheme.typography.displayLarge,
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        GlassContainer (
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onTestingModeClick)
                                .height(50.dp),
                            containerColor = MaterialTheme.colorScheme.secondary
                        ) {
                            Text(
                                stringResource(R.string.testing_mode),
                                style = MaterialTheme.typography.displayLarge,
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                    }

                    HorizontalDivider()
                }
            }

            
            if (editMode){
                itemsIndexed(items = uiState.draftCards) { index, draftCard ->
                    EditCardItem(
                        draftCard = draftCard,
                        onTermChange = { updateDraftCard(index, it, draftCard.definition) },
                        onDefChange = { updateDraftCard(index, draftCard.term, it) }
                    )
                }
            } else{
                // Используем перебор с ключем, так как в случае удаления карточки LazyColumn поймет
                // какой именно элемент пропал и что именно нужно перерисовать
                items(items = uiState.cards, key = { it.id } ) { card ->
                    CardItem(
                        card = card,
                        onLongClick = { onCardToDelete(card) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TargetDateEditor(
    currentTargetDate: Long?,
    onTargetDateChanged: (Long?) -> Unit
){
    var showDatePicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (currentTargetDate != null) {
            Text(
                text = stringResource(
                    R.string.target_date_display,
                    DateUtils.formatShortDate(currentTargetDate)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .clickable { showDatePicker = true }
            )
            TextButton(onClick = { onTargetDateChanged(null) }) {
                Text(
                    text = stringResource(R.string.target_date_clear),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            TextButton(onClick = { showDatePicker = true }) {
                Text(
                    text = stringResource(R.string.target_date_set),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentTargetDate ?: (System.currentTimeMillis() + 7 * 86_400_000L)
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onTargetDateChanged(it) }
                    showDatePicker = false
                }) {
                    Text(
                        text = stringResource(R.string.create_set_text),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(
                        text = stringResource(R.string.cancel_text),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun CardItem(
    card: Card,
    onLongClick: () -> Unit
){
    GlassContainer(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = onLongClick
            )
            .padding(vertical = 8.dp),
    ) {
        Column (
            modifier = Modifier
                .padding(12.dp)
        ){
            Text(
                text = card.term,
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(Modifier.height(4.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.inversePrimary
            )

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
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            OutlinedTextField(
                value = draftCard.term,
                onValueChange = onTermChange,
                label = {
                    Text(
                        text = stringResource(R.string.term_text),
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                isError = draftCard.term.isEmpty(),
                minLines = 3,
                maxLines = 5,
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
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                isError = draftCard.definition.isEmpty(),
                minLines = 3,
                maxLines = 5,
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
        shape = RoundedCornerShape(18.dp),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.delete_card_question),
                style = MaterialTheme.typography.displayMedium
            )
        },
        text = {
            Text(
                text = stringResource(R.string.delete_card_warning),
                style = MaterialTheme.typography.bodySmall
            )
       },
        confirmButton = {
            TextButton(
                onClick = onConfirmDelete
            ) {
                Text(
                    text = stringResource(R.string.delete_text),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.cancel_text),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
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
){
    AlertDialog(
        shape = RoundedCornerShape(18.dp),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.select_ic_text),
                style = MaterialTheme.typography.displayMedium
            )
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                availableIcons.forEach { (id, iconResId) ->
                    Icon(
                        painter = painterResource(iconResId),
                        contentDescription = stringResource(R.string.set_icon),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { onIconSelected(id) },
                        tint =
                            if (id == currentIconId)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.cancel_text),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}