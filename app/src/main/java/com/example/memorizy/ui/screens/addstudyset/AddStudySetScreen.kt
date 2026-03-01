package com.example.memorizy.ui.screens.addstudyset

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.ui.utils.AppIcon
import com.example.memorizy.ui.utils.AppIcons
import com.example.memorizy.ui.utils.GlassContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudySetScreen(
    onSetCreatedClick: () -> Unit,
    onBackClick: () -> Unit,
    uiState: AddStudySetState,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onIconSelected: (Int) -> Unit,
    onTargetDateChanged: (Long?) -> Unit,
    onCreateButtonClicked: () -> Unit
){

    LaunchedEffect(uiState.isSetCreated) {    // уходим с экрана только когда набор сохранился в БД
        if (uiState.isSetCreated){
            onSetCreatedClick() // Навигация
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.new_set_text),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        AppIcon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.back_button),
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        AddStudySetScreenBody(
            modifier = Modifier
                .padding(paddingValues),
            onBackClick = onBackClick,
            uiState = uiState,
            onNameChanged = onNameChanged,
            onDescriptionChanged = onDescriptionChanged,
            onIconSelected = onIconSelected,
            onTargetDateChanged = onTargetDateChanged,
            onCreateButtonClicked = onCreateButtonClicked
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TargetDateEditorForCreate(
    currentTargetDate: Long?,
    onTargetDateChanged: (Long?) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (currentTargetDate != null) {
            Text(
                text = stringResource(R.string.target_date_display, dateFormat.format(Date(currentTargetDate))),
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
private fun AddStudySetScreenBody(
    modifier: Modifier,
    onBackClick: () -> Unit,
    uiState: AddStudySetState,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onIconSelected: (Int) -> Unit,
    onTargetDateChanged: (Long?) -> Unit,
    onCreateButtonClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        IconSelector(
            selectedIconId = uiState.selectedIconId,
            onIconSelected = onIconSelected
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChanged,
            label = {
                Text(
                    text = stringResource(R.string.set_name_field),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.isNameEmptyError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = RoundedCornerShape(18.dp)
        )
        if (uiState.isNameEmptyError) {
            Text(
                text = stringResource(R.string.set_name_warning),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChanged,
            label = {
                Text(
                    text = stringResource(R.string.set_description),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
            maxLines = 20,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
            shape = RoundedCornerShape(18.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Дедлайн
        TargetDateEditorForCreate(
            currentTargetDate = uiState.targetDate,
            onTargetDateChanged = onTargetDateChanged
        )

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            GlassContainer(
                modifier = Modifier
                    .clickable(onClick = onBackClick)
                    .height(40.dp)
                    .width(120.dp),
                containerColor = MaterialTheme.colorScheme.onSurface
            ) { // Навигация
                Text(
                    text = stringResource(R.string.cancel_text),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.size(8.dp))

            GlassContainer (
                modifier = Modifier
                    .clickable(onClick = onCreateButtonClicked)
                    .height(40.dp)
                    .width(100.dp),
            ) {
                Text(
                    text = stringResource(R.string.create_set_text),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun IconSelector(
    selectedIconId: Int,
    onIconSelected: (Int) -> Unit,
    availableIcons: Map<Int, Int> = AppIcons.allIcons
) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier.fillMaxWidth()
    ) {
        availableIcons.forEach { (id, iconResId) ->
            Icon(
                painter = painterResource(iconResId),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onIconSelected(id) },
                tint = if (id == selectedIconId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}