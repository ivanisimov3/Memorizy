package com.example.memorizy.ui.screens.addcard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.domain.importer.model.ParseResult
import com.example.memorizy.ui.utils.GlassContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    onCardCreatedClick: () -> Unit,
    onBackClick: () -> Unit,
    uiState: AddCardState,
    onTermChanged: (String) -> Unit,
    onDefinitionChanged: (String) -> Unit,
    onDefinitionVariantAdd: () -> Unit,
    onDefinitionVariantUpdate: (Int, String) -> Unit,
    onDefinitionVariantRemove: (Int) -> Unit,
    onCreateButtonClicked: () -> Unit,
    onFileSelected: (Uri?) -> Unit,
    onDismissImportSummary: () -> Unit,
    onConfirmImport: () -> Unit
){
    val launcher = rememberLauncherForActivityResult(   // Запомнить состояние пока человек вышел из приложения
        contract = ActivityResultContracts.GetContent() // Открыть файловый менеджер устройства
    ) { uri ->
        onFileSelected(uri)
    }

    LaunchedEffect(uiState.isCardCreated) {
        if (uiState.isCardCreated){
            onCardCreatedClick()
        }
    }

    if (uiState.showImportSummaryDialog && uiState.importSummary != null) {
        ImportSummaryDialog(
            importSummary = uiState.importSummary,
            onDismissRequest = onDismissImportSummary,
            onConfirmImport = onConfirmImport
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .shadow(elevation = 8.dp),
                title = {
                    Text(
                        text = stringResource(R.string.new_card_text),
                        style = MaterialTheme.typography.labelMedium
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
                },
                actions = {
                    IconButton(
                        onClick = { launcher.launch("text/comma-separated-values") }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_import_cards),
                            contentDescription = stringResource(R.string.import_csv)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
    ) { paddingValues ->
        AddCardScreenBody(
            paddingValues = paddingValues,
            onBackClick = onBackClick,
            uiState = uiState,
            onTermChanged = onTermChanged,
            onDefinitionChanged = onDefinitionChanged,
            onAddDefinitionVariant = onDefinitionVariantAdd,
            onUpdateDefinitionVariant = onDefinitionVariantUpdate,
            onRemoveDefinitionVariant = onDefinitionVariantRemove,
            onCreateButtonClicked = onCreateButtonClicked
        )
    }
}

@Composable
private fun ImportSummaryDialog(
    importSummary: ParseResult,
    onDismissRequest: () -> Unit,
    onConfirmImport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(R.string.import_result_title),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(
                        R.string.import_success_count,
                        importSummary.successfulCards.size
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (importSummary.errors.isNotEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.import_error_count,
                            importSummary.errors.size
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(modifier = Modifier.height(100.dp)) {
                        items(importSummary.errors) { error ->
                            Text(
                                text = if (error.lineNumber > 0)
                                    stringResource(R.string.import_error_details, error.lineNumber, error.reason)
                                else
                                    error.reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (importSummary.successfulCards.isNotEmpty()) {
                FilledTonalButton(onClick = onConfirmImport) {
                    Text(
                        text = stringResource(R.string.import_confirm),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text(
                    text = stringResource(R.string.cancel_text),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@Composable
private fun AddCardScreenBody(
    paddingValues: PaddingValues,
    onBackClick: () -> Unit,
    uiState: AddCardState,
    onTermChanged: (String) -> Unit,
    onDefinitionChanged: (String) -> Unit,
    onAddDefinitionVariant: () -> Unit,
    onUpdateDefinitionVariant: (Int, String) -> Unit,
    onRemoveDefinitionVariant: (Int) -> Unit,
    onCreateButtonClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding(),
                start = 16.dp,
                end = 16.dp
            )
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
            minLines = 5,
            maxLines = 13,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
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
            minLines = 5,
            maxLines = 13,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
            shape = RoundedCornerShape(18.dp)
        )
        if (uiState.isDefinitionEmptyError) {
            Text(
                text = stringResource(R.string.definition_text_warning),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(16.dp))

        AdditionalDefinitionsEditor(
            definitionVariants = uiState.definitionVariants,
            onAddDefinitionVariant = onAddDefinitionVariant,
            onUpdateDefinitionVariant = onUpdateDefinitionVariant,
            onRemoveDefinitionVariant = onRemoveDefinitionVariant
        )

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            GlassContainer(
                modifier = Modifier
                    .height(40.dp)
                    .width(120.dp),
                onClick = onBackClick,
                containerColor = MaterialTheme.colorScheme.secondary
            ) { // Навигация
                Text(
                    text = stringResource(R.string.cancel_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(Modifier.size(8.dp))

            GlassContainer(
                modifier = Modifier
                    .height(40.dp)
                    .width(100.dp),
                onClick = onCreateButtonClicked,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = stringResource(R.string.create_set_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AdditionalDefinitionsEditor(
    definitionVariants: List<String>,
    onAddDefinitionVariant: () -> Unit,
    onUpdateDefinitionVariant: (Int, String) -> Unit,
    onRemoveDefinitionVariant: (Int) -> Unit
) {
    Text(
        text = stringResource(R.string.additional_definitions_title),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(Modifier.height(8.dp))

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        definitionVariants.forEachIndexed { index, variant ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = variant,
                    onValueChange = { onUpdateDefinitionVariant(index, it) },
                    label = {
                        Text(
                            text = stringResource(R.string.definition_variant_label, index + 1),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.weight(1f),
                    minLines = 3,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                    shape = RoundedCornerShape(18.dp)
                )

                Spacer(Modifier.width(8.dp))

                IconButton(onClick = { onRemoveDefinitionVariant(index) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.remove_definition_variant)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        OutlinedButton(onClick = onAddDefinitionVariant) {
            Text(
                text = stringResource(R.string.add_definition_variant),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}