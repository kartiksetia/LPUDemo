package com.example.demolpu

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

// The screen has input, loading, success and retryable error states.
data class NotesUiState(val notes: String = "", val loading: Boolean = false, val result: SimplifiedNotes? = null, val error: String? = null, val sample: Boolean = false)

class NotesViewModel(private val repository: NotesService = OpenAiNotesRepository()) : ViewModel() {
    var state by mutableStateOf(NotesUiState())
        private set
    fun updateNotes(notes: String) { if (!state.loading) state = state.copy(notes = notes.take(10_000), error = null) }
    fun loadExample() { if (!state.loading) state = NotesUiState(notes = OpenAiNotesRepository.sampleInput) }
    fun showSample() { if (!state.loading) state = NotesUiState(notes = OpenAiNotesRepository.sampleInput, result = OpenAiNotesRepository.sampleOutput, sample = true) }
    fun edit() { if (!state.loading) state = state.copy(result = null, sample = false, error = null) }
    fun simplify() {
        if (state.loading) return
        val input = state.notes.trim()
        if (input.isBlank()) { state = state.copy(error = "Paste your notes or enter a topic first."); return }
        state = state.copy(loading = true, error = null, sample = false)
        viewModelScope.launch {
            try { state = state.copy(result = repository.simplify(input), loading = false) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { state = state.copy(loading = false, error = e.message ?: "Could not simplify the notes. Please retry.") }
        }
    }
}
