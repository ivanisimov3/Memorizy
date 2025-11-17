package com.example.memorizy.ui.addset

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
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memorizy.R
import com.example.memorizy.data.studyset.StudySet
import com.example.memorizy.ui.studysets.StudySetsViewModel
import com.example.memorizy.ui.studysets.UserSetsScreenBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudySetScreen(
    onSetCreatedClick: () -> Unit,
    onBackClick: () -> Unit,
    uiState: AddSetScreenUIState,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onIconSelected: (Int) -> Unit,
    onCreateButtonClicked: () -> Unit
){

    LaunchedEffect(uiState.isSetCreated) {    // запуск корутин при появлении или изменении key
        if (uiState.isSetCreated){
            onSetCreatedClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новый набор") },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        AddStudySetScreenBody(
            modifier = Modifier
                .padding(paddingValues),
            onSetCreatedClick = onSetCreatedClick,
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
fun AddStudySetScreenBody(
    modifier: Modifier,
    onSetCreatedClick: () -> Unit,
    onBackClick: () -> Unit,
    uiState: AddSetScreenUIState,
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
            label = { Text("Название*") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.isNameEmptyError,
            singleLine = true
        )
        if (uiState.isNameEmptyError) {
            Text("Название не может быть пустым")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChanged,
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onBackClick) {
                Text("Отмена")
            }
            Button(onClick = onCreateButtonClicked) {
                Text("Создать")
            }
        }
    }
}

@Composable
fun IconSelector(
    selectedIconId: Int,
    onIconSelected: (Int) -> Unit
) {
    val icons = listOf(
        1 to R.drawable.random_ic,
        2 to R.drawable.random_ic,
        3 to R.drawable.random_ic,
        4 to R.drawable.random_ic,
        5 to R.drawable.random_ic
    )

    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier.fillMaxWidth()
    ) {
        icons.forEach { (id, iconId) ->
            Icon(
                painter = painterResource(iconId),
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