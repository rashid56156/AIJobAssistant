package com.sample.aijobassistant.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.aijobassistant.domain.model.AnalysisRecord
import com.sample.aijobassistant.domain.usecase.DeleteAnalysisRecordUseCase
import com.sample.aijobassistant.domain.usecase.GetHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getHistoryUseCase: GetHistoryUseCase,
    private val deleteAnalysisRecordUseCase: DeleteAnalysisRecordUseCase
) : ViewModel() {

    // History is a direct reflection of the database, so this exposes the
    // Flow from Room as a StateFlow rather than copying it into a separate
    // mutable UI state — there's no transformation step that justifies the
    // extra indirection here, unlike Home/Result which combine several
    // independent pieces of transient UI state.
    val records: StateFlow<List<AnalysisRecord>> = getHistoryUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            deleteAnalysisRecordUseCase(id)
        }
    }
}
