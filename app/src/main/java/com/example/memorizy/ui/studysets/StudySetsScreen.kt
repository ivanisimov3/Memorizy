package com.example.memorizy.ui.studysets

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.data.source.local.studyset.StudySet
import com.example.memorizy.ui.utils.AppIcons.getIconResById

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySetsScreen(
    onAddSetClick: () -> Unit,
    onSetClick: (Int) -> Unit,
    uiState: StudySetsState,
    onSearchQueryChanged: (String) -> Unit,
    onDeleteSet: (StudySet) -> Unit
){
    // Переменные для логики интерфейса
    var isSearchActive by remember { mutableStateOf(false) }
    var setToDelete by remember { mutableStateOf<StudySet?>(null) }

    Scaffold(
        topBar = {
            StudySetsTopAppBar(
                isSearchActive = isSearchActive,
                onSearchClicked = {isSearchActive = true},
                onSearchDismissed = {
                    isSearchActive = false
                    onSearchQueryChanged("")
                },
                searchQuery = uiState.searchQuery,
                onSearchQueryChanged = onSearchQueryChanged
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSetClick // Навигация
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.add_set)
                )
            }
        }
    ) { paddingValues ->
        StudySetsScreenBody(
            modifier = Modifier
                .padding(paddingValues),
            uiState = uiState,
            onSetClick = onSetClick,
            onSetToDelete = { studySet ->
                setToDelete = studySet
            }
        )
    }

    if (setToDelete != null){
        DeleteSetDialog(
            onConfirmDelete = {
                onDeleteSet(setToDelete!!)
                setToDelete = null
            },
            onDismiss = {
                setToDelete = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySetsTopAppBar(
    isSearchActive: Boolean,
    onSearchClicked: () -> Unit,
    onSearchDismissed: () -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit
){
    TopAppBar(
        title = {
            if (isSearchActive) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    shape = RoundedCornerShape(50),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            } else {
                Text(
                    text = "Memorizy",
                    style = MaterialTheme.typography.displayMedium
                )
            }
        },
        navigationIcon = {
            if (isSearchActive) {
                IconButton(onClick = onSearchDismissed) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = stringResource(R.string.close_search)
                    )
                }
            }
        },
        actions = {
            if (!isSearchActive) {
                IconButton(onClick = onSearchClicked) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = stringResource(R.string.search)
                    )
                }
            }
        }
    )
}

@Composable
fun StudySetsScreenBody(
    modifier: Modifier,
    uiState: StudySetsState,
    onSetClick: (Int) -> Unit,
    onSetToDelete: (StudySet) -> Unit
){
    if (uiState.isLoading){
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
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            // используем перебор с ключем, так как в случае удаления набора LazyColumn поймет
            // какой именно элемент пропал и что именно нужно перерисовать
            items(items = uiState.studySetsWithCardNumber, key = { it.studySet.id} ) { studySetWithCardNumber ->
                StudySetItem(
                    studySet = studySetWithCardNumber.studySet,
                    cardNumber = studySetWithCardNumber.cardNumber,
                    onSetClick = { onSetClick(studySetWithCardNumber.studySet.id) },    // переместиться к этому набору
                    onLongClick = { onSetToDelete(studySetWithCardNumber.studySet) }  // обозначить этот набор для удаления
                )
            }
        }
    }
}

@Composable
fun StudySetItem(
    studySet: StudySet,
    cardNumber: Int,
    onSetClick: () -> Unit,
    onLongClick: () -> Unit,
    getIconRes: (Int) -> (Int) = { getIconResById(it) }
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onSetClick,
                onLongClick = onLongClick
            )
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.secondary),
    ) {
        Column (
            modifier = Modifier
                .padding(16.dp),
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(getIconRes(studySet.iconId)),
                    contentDescription = stringResource(R.string.set_icon),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = studySet.name.uppercase(),
                        style = MaterialTheme.typography.displayLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!studySet.description.isNullOrEmpty()) {
                        Text(
                            text = studySet.description,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }else{
                        Text(
                            text = "нет описания",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.number_of_cards, cardNumber),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteSetDialog(
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
){
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.delete_set_question),
                style = MaterialTheme.typography.displayMedium
            ) },
        text = {
            Text(
                text = stringResource(R.string.delete_set_warning),
                style = MaterialTheme.typography.bodyMedium
            )},
        confirmButton = {
            TextButton(
                onClick = onConfirmDelete
            ) {
                Text(
                    text = stringResource(R.string.delete_text),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.cancel_text),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    )
}