package com.example.memorizy.ui.screens.setdetails

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.ui.utils.AppIcons
import com.example.memorizy.ui.utils.AppIcons.getIconResById
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.shadow
import com.example.memorizy.data.source.local.room.entity.StudySet
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
    addDraftDefinitionVariant: (Int) -> Unit,
    updateDraftDefinitionVariant: (Int, Int, String) -> Unit,
    removeDraftDefinitionVariant: (Int, Int) -> Unit,
    onCancelEditing: () -> Unit,
    onSaveChanges: () -> Unit,
    onLearningModeClick: () -> Unit,
    onTestingModeClick: () -> Unit,
    onStatisticsClick: () -> Unit
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
                onStartEditing = onStartEditing,
                onStatisticsClick = onStatisticsClick
            )
        },
        floatingActionButton = {
            if (!uiState.isEditing){
                GlassContainer(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(56.dp),
                    onClick = onAddCardClick
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(R.string.add_card_button),
                        modifier = Modifier,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    ) { paddingValues ->
        SetDetailsBody(
            paddingValues = paddingValues,
            uiState = uiState,
            onCardToDelete = { studySet ->
                cardToDelete = studySet
            },
            updateDraftName = updateDraftName,
            updateDraftDescription = updateDraftDescription,
            updateDraftTargetDate = updateDraftTargetDate,
            updateDraftCard = updateDraftCard,
            addDraftDefinitionVariant = addDraftDefinitionVariant,
            updateDraftDefinitionVariant = updateDraftDefinitionVariant,
            removeDraftDefinitionVariant = removeDraftDefinitionVariant,
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
    onStatisticsClick: () -> Unit,
    isEditing: Boolean
){
    TopAppBar(
        modifier = Modifier
            .shadow(elevation = 8.dp),
        title = {
            Text(
                text =
                    if (isEditing)
                        stringResource(R.string.set_edit_mode)
                    else
                        stringResource(R.string.set_detalization_text),
                style = MaterialTheme.typography.labelMedium,
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
                        painter = painterResource(R.drawable.ic_close),
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
                IconButton(onClick = onStatisticsClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_statistics),
                        contentDescription = stringResource(R.string.statistics_ic)
                    )
                }
                IconButton(onClick = onStartEditing) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = stringResource(R.string.start_edit_mode)
                    )
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetDetailsBody(
    paddingValues: PaddingValues,
    uiState: SetDetailsState,
    onCardToDelete: (Card) -> Unit,
    getIconRes: (Int) -> (Int) = { getIconResById(it) },
    updateDraftName: (String) -> Unit,
    updateDraftDescription: (String) -> Unit,
    updateDraftTargetDate: (Long?) -> Unit,
    updateDraftCard: (Int, String, String) -> Unit,
    addDraftDefinitionVariant: (Int) -> Unit,
    updateDraftDefinitionVariant: (Int, Int, String) -> Unit,
    removeDraftDefinitionVariant: (Int, Int) -> Unit,
    onIconClick: () -> Unit,
    onLearningModeClick: () -> Unit,
    onTestingModeClick: () -> Unit
){
    val editMode = (uiState.isEditing && uiState.draftSet != null)

    if (uiState.isLoading || uiState.studySet == null){
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            CircularProgressIndicator()
        }
        return
    }

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
        if (!editMode) {
            val deadline = uiState.deadline
            if (deadline != null) {
                item {
                    DeadlineHeroCard(
                        deadline = deadline
                    )
                }
            }

            item {
                SetSummaryCard(
                    studySet = uiState.studySet,
                    overallProgress = uiState.overallProgress,
                    overallProgressPercentage = uiState.overallProgressPercentage,
                    getIconRes = getIconRes
                )
            }
        }

        item {
            Column{
                if (editMode) {
                    TargetDateEditor(
                        currentTargetDate = uiState.draftSet.targetDate,
                        onTargetDateChanged = updateDraftTargetDate
                    )
                }

                if (editMode) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(getIconRes(uiState.draftSet.iconId)),
                            contentDescription = stringResource(R.string.set_icon),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onIconClick() }
                                .size(48.dp)
                        )
                        Spacer(Modifier.width(16.dp))
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
                    }

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
                }

                if (!editMode) {
                    GlassContainer (
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        onClick = onLearningModeClick,
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Text(
                            stringResource(R.string.learning_mode),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    GlassContainer (
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        onClick = onTestingModeClick,
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Text(
                            stringResource(R.string.testing_mode),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }


        if (editMode){
            itemsIndexed(items = uiState.draftCards) { index, draftCard ->
                EditCardItem(
                    draftCard = draftCard,
                    onTermChange = { updateDraftCard(index, it, draftCard.definition) },
                    onDefChange = { updateDraftCard(index, draftCard.term, it) },
                    onDefinitionVariantAdd = { addDraftDefinitionVariant(index) },
                    onDefinitionVariantUpdate = { variantIndex, value ->
                        updateDraftDefinitionVariant(index, variantIndex, value)
                    },
                    onDefinitionVariantRemove = { variantIndex ->
                        removeDraftDefinitionVariant(index, variantIndex)
                    }
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

@Composable
private fun DeadlineHeroCard(
    deadline: DeadlineUiState
) {
    val accentColor = MaterialTheme.colorScheme.secondary

    GlassContainer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = accentColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.deadline_countdown_title),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CountdownUnit(
                    value = deadline.remainingDays,
                    label = stringResource(R.string.deadline_days_short),
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
                CountdownUnit(
                    value = deadline.remainingHours,
                    label = stringResource(R.string.deadline_hours_short),
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CountdownUnit(
    value: Long,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    GlassContainer(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        containerColor = accentColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString().padStart(2, '0'),
                style = MaterialTheme.typography.displayMedium,
                color = accentColor
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SetSummaryCard(
    studySet: StudySet,
    overallProgress: Float,
    overallProgressPercentage: String,
    getIconRes: (Int) -> Int
) {
    GlassContainer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.overall_progress_title),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = overallProgressPercentage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { overallProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
                gapSize = (-15).dp,
                drawStopIndicator = {}
            )

            Spacer(Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(getIconRes(studySet.iconId)),
                    contentDescription = stringResource(R.string.set_icon),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(Modifier.width(16.dp))

                Text(
                    text = studySet.name,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!studySet.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = studySet.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
                color = MaterialTheme.colorScheme.onSurface,
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
                    color = MaterialTheme.colorScheme.onSurface
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
                        color = MaterialTheme.colorScheme.secondary
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
            .padding(vertical = 8.dp),
        onLongClick = onLongClick,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Column (
            modifier = Modifier
                .padding(12.dp)
        ){
            Text(
                text = card.term,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(4.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = card.definition,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (card.definitionVariants.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.additional_definitions_title),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))

                card.definitionVariants.forEach { definitionVariant ->
                    Text(
                        text = "• $definitionVariant",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun EditCardItem(
    onTermChange: (String) -> Unit,
    onDefChange: (String) -> Unit,
    onDefinitionVariantAdd: () -> Unit,
    onDefinitionVariantUpdate: (Int, String) -> Unit,
    onDefinitionVariantRemove: (Int) -> Unit,
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
                        style = MaterialTheme.typography.labelSmall
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
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                isError = draftCard.definition.isEmpty(),
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.additional_definitions_title),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))

            draftCard.definitionVariants.forEachIndexed { index, variant ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = variant,
                        onValueChange = { onDefinitionVariantUpdate(index, it) },
                        label = {
                            Text(
                                text = stringResource(R.string.definition_variant_label, index + 1),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        minLines = 3,
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))

                    IconButton(onClick = { onDefinitionVariantRemove(index) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.remove_definition_variant)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            OutlinedButton(onClick = onDefinitionVariantAdd) {
                Text(
                    text = stringResource(R.string.add_definition_variant),
                    style = MaterialTheme.typography.labelSmall
                )
            }
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
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = stringResource(R.string.delete_card_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
       },
        confirmButton = {
            OutlinedButton(
                onClick = onConfirmDelete
            ) {
                Text(
                    text = stringResource(R.string.delete_text),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            FilledTonalButton(
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
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
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
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
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
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}