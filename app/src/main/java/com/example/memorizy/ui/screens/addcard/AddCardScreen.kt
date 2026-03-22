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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.domain.importer.model.ParseResult
import com.example.memorizy.domain.importer.model.ParsedCard
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

    var showImportDialog by remember { mutableStateOf(false) }
    val launchImportPicker = {
        showImportDialog = false
        launcher.launch("*/*")
    }

    LaunchedEffect(uiState.isCardCreated) {
        if (uiState.isCardCreated){
            onCardCreatedClick()
        }
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
                        onClick = { showImportDialog = true }
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

    if (showImportDialog) {
        ImportInfoDialog(
            isImporting = uiState.isImporting,
            onDismissRequest = { showImportDialog = false },
            onImportClick = launchImportPicker
        )
    }

    if (uiState.showImportSummaryDialog && uiState.importSummary != null) {
        ImportSummaryDialog(
            importSummary = uiState.importSummary,
            onDismissRequest = onDismissImportSummary,
            onConfirmImport = onConfirmImport
        )
    }
}

@Composable
private fun ImportInfoDialog(
    isImporting: Boolean,
    onDismissRequest: () -> Unit,
    onImportClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(R.string.import_csv),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.import_dialog_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.import_dialog_columns_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.import_dialog_columns_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                GlassContainer(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.import_dialog_example_title),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = stringResource(R.string.import_dialog_example_line),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onImportClick,
                enabled = !isImporting
            ) {
                Text(
                    text = if (isImporting) {
                        stringResource(R.string.loading_text)
                    } else {
                        stringResource(R.string.import_cards_select_file)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            FilledTonalButton(onClick = onDismissRequest) {
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

                if (importSummary.successfulCards.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.import_preview_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        importSummary.successfulCards
                            .take(2)
                            .forEach { card ->
                                ImportPreviewCard(card = card)
                            }
                    }
                }

                if (importSummary.errors.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))

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
                OutlinedButton(onClick = onConfirmImport) {
                    Text(
                        text = stringResource(R.string.import_confirm),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        dismissButton = {
            FilledTonalButton(onClick = onDismissRequest) {
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
private fun ImportPreviewCard(
    card: ParsedCard
) {
    val additionalDefinitionsText = if (card.definitionVariants.isEmpty()) {
        stringResource(R.string.import_preview_empty_additional)
    } else {
        card.definitionVariants.joinToString("; ")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ImportPreviewLine(
                label = stringResource(R.string.import_preview_term_label),
                value = card.term
            )

            ImportPreviewLine(
                label = stringResource(R.string.import_preview_definition_label),
                value = card.definition
            )

            ImportPreviewLine(
                label = stringResource(R.string.import_preview_additional_label),
                value = additionalDefinitionsText
            )
        }
    }
}

@Composable
private fun ImportPreviewLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
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