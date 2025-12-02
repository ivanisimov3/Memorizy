package com.example.memorizy.ui.screens.addstudyset

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.memorizy.ui.utils.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudySetScreen(
    onSetCreatedClick: () -> Unit,
    onBackClick: () -> Unit,
    uiState: AddStudySetState,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onIconSelected: (Int) -> Unit,
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
                        style = MaterialTheme.typography.displayMedium
                    )
                },
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
    ) { paddingValues ->
        AddStudySetScreenBody(
            modifier = Modifier
                .padding(paddingValues),
            onBackClick = onBackClick,
            uiState = uiState,
            onNameChanged = onNameChanged,
            onDescriptionChanged = onDescriptionChanged,
            onIconSelected = onIconSelected,
            onCreateButtonClicked = onCreateButtonClicked
        )
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
    onCreateButtonClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        if (uiState.isNameEmptyError) {
            Text(
                text = stringResource(R.string.set_name_warning),
                style = MaterialTheme.typography.labelSmall
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onBackClick) { // Навигация
                Text(
                    text = stringResource(R.string.cancel_text),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Button(onClick = onCreateButtonClicked) {
                Text(
                    text = stringResource(R.string.create_set_text),
                    style = MaterialTheme.typography.labelSmall
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