package com.sample.aijobassistant.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.aijobassistant.domain.usecase.ClearApiKeyUseCase
import com.sample.aijobassistant.domain.usecase.GetApiKeyStatusUseCase
import com.sample.aijobassistant.domain.usecase.SaveApiKeyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val hasKeySaved: Boolean = false,
    val inputValue: String = "",
    val isSaving: Boolean = false,
    val saveSucceeded: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val saveApiKeyUseCase: SaveApiKeyUseCase,
    private val getApiKeyStatusUseCase: GetApiKeyStatusUseCase,
    private val clearApiKeyUseCase: ClearApiKeyUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refreshKeyStatus()
    }

    private fun refreshKeyStatus() {
        viewModelScope.launch {
            val hasKey = getApiKeyStatusUseCase()
            _uiState.value = _uiState.value.copy(hasKeySaved = hasKey)
        }
    }

    fun onInputChanged(value: String) {
        _uiState.value = _uiState.value.copy(inputValue = value, errorMessage = null, saveSucceeded = false)
    }

    fun saveKey() {
        val current = _uiState.value
        if (current.inputValue.isBlank()) {
            _uiState.value = current.copy(errorMessage = "API key cannot be empty.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                saveApiKeyUseCase(current.inputValue)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSucceeded = true,
                    hasKeySaved = true,
                    inputValue = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Failed to save the API key."
                )
            }
        }
    }

    fun clearKey() {
        viewModelScope.launch {
            clearApiKeyUseCase()
            _uiState.value = SettingsUiState(hasKeySaved = false)
        }
    }
}
