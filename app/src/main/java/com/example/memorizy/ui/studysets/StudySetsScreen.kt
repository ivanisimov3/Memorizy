package com.example.memorizy.ui.studysets

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.data.studyset.StudySet

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
            UserSetsTopAppBar(
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
                    contentDescription = "Добавить набор"
                )
            }
        }
    ) { paddingValues ->
        UserSetsScreenBody(
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
            studySet = setToDelete!!,
            onConfirmDelete = {
                onDeleteSet(it)
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
fun UserSetsTopAppBar(
    isSearchActive: Boolean,
    onSearchClicked: () -> Unit,
    onSearchDismissed: () -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit
){
    TopAppBar(
        title = {
            if (isSearchActive) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                )
            } else {
                Text("Memorizy")
            }
        },
        navigationIcon = {
            if (isSearchActive) {
                IconButton(onClick = onSearchDismissed) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Закрыть поиск"
                    )
                }
            }
        },
        actions = {
            if (!isSearchActive) {
                IconButton(onClick = onSearchClicked) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = "Поиск"
                    )
                }
            }
        }
    )
}

@Composable
fun UserSetsScreenBody(
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
    onLongClick: () -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onSetClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.random_ic),
                contentDescription = "Иконка набора"
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = studySet.name
                )
                Text(
                    text = "Карточек: $cardNumber"
                )
                if (studySet.description != null){
                    Text(
                        text = studySet.description,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteSetDialog(
    studySet: StudySet,
    onConfirmDelete: (StudySet) -> Unit,
    onDismiss: () -> Unit
){
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить набор?") },
        text = { Text("Вы уверены что хотите удалить набор? Это действие нельзя откатить назад.")},
        confirmButton = {
            TextButton(
                onClick = { onConfirmDelete(studySet) }
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