package com.sample.aijobassistant.ui.screens.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onAnalysisComplete: (recordId: Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputModeIndex by remember { mutableIntStateOf(0) } // 0 = paste, 1 = PDF

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.onPdfSelected(uriString = it.toString(), fileName = it.lastPathSegment ?: "resume.pdf")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Job Assistant") },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Filled.History, contentDescription = "History")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Paste a job description and your resume to get an instant match analysis.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Text("Job description", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = uiState.jobDescription,
                onValueChange = viewModel::onJobDescriptionChanged,
                placeholder = { Text("Paste the full job posting here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            Text("Your resume", style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = inputModeIndex == 0,
                    onClick = { inputModeIndex = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("Paste text") }

                SegmentedButton(
                    selected = inputModeIndex == 1,
                    onClick = { inputModeIndex = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("Upload PDF") }
            }

            if (inputModeIndex == 0) {
                OutlinedTextField(
                    value = (uiState.resumeSource as? ResumeSource.PastedText)?.text ?: "",
                    onValueChange = viewModel::onResumeTextChanged,
                    placeholder = { Text("Paste your resume text here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when {
                            uiState.isExtractingPdf -> {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                    Text("Reading PDF...")
                                }
                            }
                            uiState.resumeSource is ResumeSource.UploadedPdf -> {
                                val pdf = uiState.resumeSource as ResumeSource.UploadedPdf
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.Description, contentDescription = null)
                                    Text(pdf.fileName, style = MaterialTheme.typography.bodyMedium)
                                }
                                OutlinedButton(onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }) {
                                    Text("Choose a different file")
                                }
                            }
                            else -> {
                                OutlinedButton(
                                    onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.UploadFile, contentDescription = null)
                                    Text("  Select PDF")
                                }
                            }
                        }
                    }
                }
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (uiState.needsApiKey) {
                    OutlinedButton(onClick = onNavigateToSettings) {
                        Text("Go to Settings")
                    }
                }
            }

            Button(
                onClick = viewModel::analyze,
                enabled = !uiState.isAnalyzing && !uiState.isExtractingPdf,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Analyze match")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { id ->
            onAnalysisComplete(id)
        }
    }
}
