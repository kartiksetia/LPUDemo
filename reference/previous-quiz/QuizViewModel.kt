package com.example.demolpu

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch

data class QuizUiState(
    val notes: String = "", val image: NotesImage? = null, val loading: Boolean = false,
    val error: String? = null, val questions: List<Question> = emptyList(),
    val answers: Map<Int, Int> = emptyMap(), val current: Int = 0, val finished: Boolean = false,
    val sample: Boolean = false
)

class QuizViewModel : ViewModel() {
    private val repository = QuizRepository()
    var state by mutableStateOf(QuizUiState())
        private set
    fun setNotes(value: String) { if (!state.loading) state = state.copy(notes = value.take(15000), error = null) }
    fun removeImage() { if (!state.loading) state = state.copy(image = null) }
    fun pickImage(context: Context, uri: Uri) {
        if (state.loading) return
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            try { state = state.copy(image = repository.readImage(context, uri), loading = false) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { state = state.copy(loading = false, error = e.message ?: "Could not open image.") }
        }
    }
    fun generate() {
        if (state.loading) return
        if (state.notes.isBlank() && state.image == null) { state = state.copy(error = "Paste your notes or select an image first."); return }
        state = state.copy(loading = true, error = null)
        val input = state
        viewModelScope.launch {
            try { state = state.copy(loading = false, questions = repository.generateQuiz(input.notes, input.image), answers = emptyMap(), current = 0, finished = false, sample = false) }
            catch (e: TimeoutCancellationException) { state = state.copy(loading = false, error = "AI request timed out. Check your connection and retry.") }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { state = state.copy(loading = false, error = e.message ?: "Could not generate quiz. Check Firebase AI Logic setup and App Check.") }
        }
    }
    fun sample() { if (!state.loading) state = QuizUiState(notes = QuizRepository.sampleNotes, questions = QuizRepository.sampleQuiz, sample = true) }
    fun answer(index: Int) {
        if (state.current !in state.answers && index in 0..3) state = state.copy(answers = state.answers + (state.current to index))
    }
    fun next() {
        if (state.current !in state.answers) return
        state = if (state.current == state.questions.lastIndex) state.copy(finished = true) else state.copy(current = state.current + 1)
    }
    fun editNotes() { state = state.copy(questions = emptyList(), answers = emptyMap(), current = 0, finished = false, error = null, sample = false) }
    fun retryQuiz() { state = state.copy(answers = emptyMap(), current = 0, finished = false) }
}
