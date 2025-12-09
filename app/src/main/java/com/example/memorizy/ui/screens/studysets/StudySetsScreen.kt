package com.example.memorizy.ui.screens.studysets

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.data.source.local.room.entity.StudySet
import com.example.memorizy.ui.utils.AppIcon
import com.example.memorizy.ui.utils.AppIcons.getIconResById
import com.example.memorizy.ui.utils.GlassContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySetsScreen(
    onSettingsClick: () -> Unit,
    onAddSetClick: () -> Unit,
    onSetClick: (Long) -> Unit,
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
                uiState = uiState,
                onProfileClick = onSettingsClick,
                isSearchActive = isSearchActive,
                onSearchClicked = {isSearchActive = true},
                onSearchDismissed = {
                    isSearchActive = false
                    onSearchQueryChanged("")
                },
                onSearchQueryChanged = onSearchQueryChanged
            )
        },
        floatingActionButton = {
            GlassContainer(
                containerColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .clickable(onClick = onAddSetClick)
                    .size(56.dp),
            ) {
                AppIcon(
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
private fun StudySetsTopAppBar(
    uiState: StudySetsState,
    onProfileClick: () -> Unit,
    isSearchActive: Boolean,
    onSearchClicked: () -> Unit,
    onSearchDismissed: () -> Unit,
    onSearchQueryChanged: (String) -> Unit
){
    TopAppBar(
        title = {
            if (isSearchActive) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    shape = RoundedCornerShape(50),
                    textStyle = MaterialTheme.typography.bodySmall,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            } else {
                Text(
                    text = "Memorizy",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        navigationIcon = {
            if (isSearchActive) {
                IconButton(onClick = onSearchDismissed) {
                    AppIcon(
                        modifier = Modifier
                            .size(28.dp),
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = stringResource(R.string.close_search)
                    )
                }
            }
        },
        actions = {
            if (!isSearchActive) {
                IconButton(onClick = onSearchClicked) {
                    AppIcon(
                        modifier = Modifier
                            .size(28.dp),
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = stringResource(R.string.search)
                    )
                }
                IconButton(onClick = onProfileClick) {
                    AppIcon(
                        modifier = Modifier
                            .size(28.dp),
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = stringResource(R.string.settings_ic)
                    )
                }
            }
        }
    )
}

@Composable
private fun StudySetsScreenBody(
    modifier: Modifier,
    uiState: StudySetsState,
    onSetClick: (Long) -> Unit,
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
private fun StudySetItem(
    studySet: StudySet,
    cardNumber: Long,
    onSetClick: () -> Unit,
    onLongClick: () -> Unit,
    getIconRes: (Int) -> (Int) = { getIconResById(it) }
){
    GlassContainer(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onSetClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 8.dp),
    ) {
        Column (
            modifier = Modifier
                .padding(16.dp),
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(
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
                        style = MaterialTheme.typography.displayMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!studySet.description.isNullOrEmpty()) {
                        Text(
                            text = studySet.description,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }else{
                        Text(
                            text = "Нет описания",
                            style = MaterialTheme.typography.bodySmall,
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
private fun DeleteSetDialog(
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
){
    AlertDialog(
        shape = RoundedCornerShape(18.dp),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.delete_set_question),
                style = MaterialTheme.typography.displayMedium
            ) },
        text = {
            Text(
                text = stringResource(R.string.delete_set_warning),
                style = MaterialTheme.typography.bodySmall
            )},
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