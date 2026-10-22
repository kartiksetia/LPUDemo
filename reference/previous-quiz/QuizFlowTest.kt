package com.example.demolpu

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.*
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.json.JSONObject

class QuizFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun validatesAiAnswerIndexesAndChoices() {
        fun quiz(index: Any = 1, options: List<String> = listOf("Stack", "Queue", "Tree", "Graph")): JSONObject {
            val questions = org.json.JSONArray()
            repeat(3) { questions.put(JSONObject().put("prompt", "Which uses FIFO?").put("options", org.json.JSONArray(options)).put("correctIndex", index).put("explanation", "A queue uses FIFO.")) }
            return JSONObject().put("questions", questions)
        }
        assertEquals(3, QuizRepository.parseQuestions(quiz()).size)
        for (index in listOf(-1, 4, true, 1.5)) {
            assertThrows(IllegalArgumentException::class.java) { QuizRepository.parseQuestions(quiz(index)) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            QuizRepository.parseQuestions(quiz(options = listOf("Queue", "queue", "Tree", "Graph")))
        }
    }

    @Test fun sampleQuizScoresAnswersAndCanRestart() {
        compose.onNodeWithText("Try sample quiz • no network").performScrollTo().performClick()
        compose.onNodeWithText("SAMPLE MODE • fixed quiz, no AI request").assertExists()
        compose.onNodeWithText("B. Queue").performScrollTo().performClick()
        compose.onNodeWithText("Correct!").assertExists()
        compose.onNodeWithText("Next question").performScrollTo().performClick()
        compose.onNodeWithText("C. The most recently added item").performScrollTo().performClick()
        compose.onNodeWithText("Next question").performScrollTo().performClick()
        compose.onNodeWithText("D. Tree").performScrollTo().performClick()
        compose.onNodeWithText("See results").performScrollTo().performClick()
        compose.onNodeWithText("3 / 3").assertExists()
        compose.onNodeWithText("Practice again").performScrollTo().performClick()
        compose.onNodeWithText("QUESTION 1 OF 3").assertExists()
    }
}
