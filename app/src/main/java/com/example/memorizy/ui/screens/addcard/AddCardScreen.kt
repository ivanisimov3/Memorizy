package com.example.memorizy.ui.screens.addcard

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.ui.utils.AppIcon
import com.example.memorizy.ui.utils.GlassContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    onCardCreatedClick: () -> Unit,
    onBackClick: () -> Unit,
    uiState: AddCardState,
    onTermChanged: (String) -> Unit,
    onDefinitionChanged: (String) -> Unit,
    onCreateButtonClicked: () -> Unit
){

    LaunchedEffect(uiState.isCardCreated) {    // уходим с экрана только когда карточка сохранилась в БД
        if (uiState.isCardCreated){
            onCardCreatedClick() // Навигация
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.new_card_text),
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
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        AddCardScreenBody(
            modifier = Modifier
                .padding(paddingValues),
            onBackClick = onBackClick,
            uiState = uiState,
            onTermChanged = onTermChanged,
            onDefinitionChanged = onDefinitionChanged,
            onCreateButtonClicked = onCreateButtonClicked
        )
    }
}

@Composable
private fun AddCardScreenBody(
    modifier: Modifier,
    onBackClick: () -> Unit,
    uiState: AddCardState,
    onTermChanged: (String) -> Unit,
    onDefinitionChanged: (String) -> Unit,
    onCreateButtonClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = uiState.term,
            onValueChange = onTermChanged,
            label = {
                Text(
                    text = stringResource(R.string.term_text),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.isTermEmptyError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = RoundedCornerShape(18.dp)
        )
        if (uiState.isTermEmptyError) {
            Text(
                text = stringResource(R.string.term_text_warning),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.definition,
            onValueChange = onDefinitionChanged,
            label = {
                Text(
                    text = stringResource(R.string.definition_text),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            isError = uiState.isDefinitionEmptyError,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            shape = RoundedCornerShape(18.dp)
        )
        if (uiState.isDefinitionEmptyError) {
            Text(
                text = stringResource(R.string.definition_text_warning),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
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

            GlassContainer(
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