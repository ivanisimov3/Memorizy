package com.example.memorizy.ui.screens.studysets

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.data.source.local.room.entity.StudySet
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
                    .size(56.dp),
                onClick = onAddSetClick
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.add_set),
                    modifier = Modifier,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        StudySetsScreenBody(
            paddingValues = paddingValues,
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
        modifier = Modifier
            .shadow(elevation = 8.dp),
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            } else {
                Text(
                    text = "Memorizy",
                    style = MaterialTheme.typography.labelLarge
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
                        modifier = Modifier
                            .size(28.dp),
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = stringResource(R.string.search)
                    )
                }
                IconButton(onClick = onProfileClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = stringResource(R.string.settings_ic)
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

@Composable
private fun StudySetsScreenBody(
    paddingValues: PaddingValues,
    uiState: StudySetsState,
    onSetClick: (Long) -> Unit,
    onSetToDelete: (StudySet) -> Unit
){
    if (uiState.isLoading){
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            CircularProgressIndicator()
        }
    }
    else{
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
            items(items = uiState.studySetsWithCardNumber, key = { it.studySet.id} ) { studySetWithCardNumber ->
                StudySetItem(
                    studySet = studySetWithCardNumber.studySet,
                    cardNumber = studySetWithCardNumber.cardNumber,
                    reviewCardNumber = uiState.reviewCountBySet[studySetWithCardNumber.studySet.id] ?: 0,
                    onSetClick = { onSetClick(studySetWithCardNumber.studySet.id) },
                    onLongClick = { onSetToDelete(studySetWithCardNumber.studySet) }
                )
            }
        }
    }
}

@Composable
private fun StudySetItem(
    studySet: StudySet,
    cardNumber: Long,
    reviewCardNumber: Long = 0,
    onSetClick: () -> Unit,
    onLongClick: () -> Unit,
    getIconRes: (Int) -> (Int) = { getIconResById(it) }
){
    GlassContainer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        onClick = onSetClick,
        onLongClick = onLongClick
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
                    modifier = Modifier,
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
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (!studySet.description.isNullOrEmpty()) {
                        Text(
                            text = studySet.description,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }else{
                        Text(
                            text = "Нет описания",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.number_of_cards, cardNumber),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(R.string.review_card_count, reviewCardNumber),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (reviewCardNumber > 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
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
        shape = RoundedCornerShape(24.dp),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.delete_set_question),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            ) },
        text = {
            Text(
                text = stringResource(R.string.delete_set_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )},
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