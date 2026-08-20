package com.sample.aijobassistant.ui.screens.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.aijobassistant.domain.model.AnalysisRecord
import com.sample.aijobassistant.domain.usecase.GetAnalysisRecordByIdUseCase
import com.sample.aijobassistant.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultUiState(
    val isLoading: Boolean = true,
    val record: AnalysisRecord? = null,
    val notFound: Boolean = false
)

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val getAnalysisRecordByIdUseCase: GetAnalysisRecordByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recordId: Long =
        savedStateHandle.get<Long>(Screen.Result.ARG_RECORD_ID) ?: -1L

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAnalysisRecordByIdUseCase(recordId).collect { record ->
                _uiState.value = if (record != null) {
                    ResultUiState(isLoading = false, record = record)
                } else {
                    ResultUiState(isLoading = false, notFound = true)
                }
            }
        }
    }
}
