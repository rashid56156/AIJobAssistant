package com.sample.aijobassistant.ui.screens.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sample.aijobassistant.domain.model.MatchAnalysis
import com.sample.aijobassistant.ui.theme.ScoreModerate
import com.sample.aijobassistant.ui.theme.ScoreStrong
import com.sample.aijobassistant.ui.theme.ScoreWeak

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    onBack: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match analysis") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.notFound -> Text("This analysis could not be found.")
                uiState.record != null -> ResultContent(
                    jobTitle = uiState.record!!.jobTitle,
                    analysis = uiState.record!!.analysis
                )
            }
        }
    }
}

@Composable
private fun ResultContent(jobTitle: String, analysis: MatchAnalysis) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = jobTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        ScoreBadge(score = analysis.matchScore)

        Text(
            text = analysis.summary,
            style = MaterialTheme.typography.bodyLarge
        )

        if (analysis.strengths.isNotEmpty()) {
            SectionList(
                title = "Strengths",
                items = analysis.strengths,
                icon = Icons.Filled.CheckCircle,
                tint = ScoreStrong
            )
        }

        if (analysis.gaps.isNotEmpty()) {
            SectionList(
                title = "Gaps",
                items = analysis.gaps,
                icon = Icons.Filled.Warning,
                tint = ScoreWeak
            )
        }

        if (analysis.suggestions.isNotEmpty()) {
            SectionList(
                title = "Suggestions",
                items = analysis.suggestions,
                icon = Icons.Filled.Lightbulb,
                tint = ScoreModerate
            )
        }
    }
}

@Composable
private fun ScoreBadge(score: Int) {
    val color = when {
        score >= 75 -> ScoreStrong
        score >= 50 -> ScoreModerate
        else -> ScoreWeak
    }

    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "match",
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
        }
    }
}

@Composable
private fun SectionList(
    title: String,
    items: List<String>,
    icon: ImageVector,
    tint: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        items.forEach { item ->
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(top = 2.dp, end = 8.dp)
                )
                Text(text = item, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
