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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.memorizy.R
import com.example.memorizy.data.studyset.StudySet
import com.example.memorizy.data.StudySetWithCardNumber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySetsScreen(
    viewModel: StudySetsViewModel = hiltViewModel(),
    onAddSetClick: () -> Unit,
    onSetClick: (Int) -> Unit
){
    val state by viewModel.uiState.collectAsState()
    var setToDelete by remember { mutableStateOf<StudySet?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memorizy") },
                actions = {
                    IconButton(
                        onClick = { }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = "Поиск"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddSetClick }
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
            isLoading = state.isLoading,
            sets = state.studySets,
            onSetClick = onSetClick,
            onDeleteSetRequest = {
                studySet -> setToDelete = studySet
            }
        )
    }
}

@Composable
fun UserSetsScreenBody(
    modifier: Modifier,
    isLoading: Boolean,
    sets: List<StudySetWithCardNumber>,
    onSetClick: (Int) -> Unit,
    onDeleteSetRequest: (StudySet) -> Unit
){
    if (isLoading){
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
            items(items = sets, key = { it.studySet.id} ) { studySetWithCount ->
                StudySetItem(
                    studySet = studySetWithCount.studySet,
                    cardNumber = studySetWithCount.cardNumber,
                    onClick = { onSetClick(studySetWithCount.studySet.id) },
                    onLongClick = { onDeleteSetRequest(studySetWithCount.studySet) }
                )
            }
        }
    }
}

@Composable
fun StudySetItem(
    studySet: StudySet,
    cardNumber: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
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