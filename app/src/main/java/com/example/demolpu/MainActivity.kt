package com.example.demolpu

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val Navy = Color(0xFF0D1728)
private val Mint = Color(0xFF70E4C0)
private val Muted = Color(0xFFAFBED1)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("NotesLifecycle", "onCreate")
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Mint, onPrimary = Navy, background = Navy, surface = Color(0xFF17263B))) {
                NotesScreen()
            }
        }
    }
    override fun onStart() { super.onStart(); Log.d("NotesLifecycle", "onStart") }
    override fun onResume() { super.onResume(); Log.d("NotesLifecycle", "onResume") }
    override fun onPause() { Log.d("NotesLifecycle", "onPause"); super.onPause() }
    override fun onStop() { Log.d("NotesLifecycle", "onStop"); super.onStop() }
    override fun onRestart() { super.onRestart(); Log.d("NotesLifecycle", "onRestart") }
    override fun onDestroy() { Log.d("NotesLifecycle", "onDestroy"); super.onDestroy() }
}

@Preview
@Composable
fun NotesScreen(vm: NotesViewModel = viewModel()) {
    val state = vm.state
    val context = LocalContext.current
    val scroll = rememberScrollState()
    var copied by remember(state.result) { mutableStateOf(false) }
    LaunchedEffect(state.result) { scroll.scrollTo(0) }
    BackHandler(enabled = state.result != null) { vm.edit() }
    Scaffold(containerColor = Navy) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(scroll).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("ANDROID + OPENAI", color = Mint, fontSize = 12.sp, letterSpacing = 2.sp)
            Text("Notes, simplified.", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Text("Make a difficult concept easier to understand.", color = Muted)
            val result = state.result
            if (result == null) {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("What are you learning?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Paste your notes or enter a topic. Get a simple explanation, four key points and an example.")
                        OutlinedTextField(value = state.notes, onValueChange = vm::updateNotes, modifier = Modifier.fillMaxWidth(), label = { Text("Notes or topic") }, placeholder = { Text("Explain deadlock in operating systems…") }, minLines = 6, maxLines = 10, enabled = !state.loading, supportingText = { Text("${state.notes.length} / 10,000 characters") })
                        TextButton(onClick = vm::loadExample, enabled = !state.loading) { Text("Use example notes") }
                        Button(onClick = vm::simplify, enabled = !state.loading && state.notes.isNotBlank(), modifier = Modifier.fillMaxWidth().height(56.dp)) {
                            if (state.loading) { CircularProgressIndicator(Modifier.size(20.dp), color = Navy, strokeWidth = 2.dp); Spacer(Modifier.width(12.dp)) }
                            Text(if (state.loading) "Simplifying…" else if (state.error != null) "Retry" else "Simplify notes", fontSize = 17.sp)
                        }
                        Text("Tapping Simplify sends this text to OpenAI. Share only notes you want to upload.", style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                }
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (BuildConfig.OPENAI_API_KEY.isBlank()) Text("Live mode needs OPENAI_API_KEY in local.properties and a rebuilt debug app.", color = Muted, style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = vm::showSample, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) { Text("View offline sample") }
                Text("Offline sample shows a saved deadlock explanation, without an AI request.", color = Muted, style = MaterialTheme.typography.bodySmall)
            } else {
                if (state.sample) Text("OFFLINE SAMPLE • saved explanation", color = Mint, fontWeight = FontWeight.Bold)
                Text(result.title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Section("Simple explanation") { Text(result.explanation, style = MaterialTheme.typography.bodyLarge) }
                Section("Key points") {
                    result.keyPoints.forEachIndexed { i, point -> Text("${i + 1}. $point", style = MaterialTheme.typography.bodyLarge) }
                }
                Section("A concrete example") { Text(result.example, style = MaterialTheme.typography.bodyLarge) }
                OutlinedButton(onClick = {
                    val text = "${result.title}\n\n${result.explanation}\n\n${result.keyPoints.joinToString("\n")}\n\n${result.example}"
                    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Simplified notes", text))
                    copied = true
                }, modifier = Modifier.fillMaxWidth()) { Text(if (copied) "Copied" else "Copy explanation") }
                Button(onClick = vm::edit, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("Edit notes") }
                Text("AI can make mistakes. Check important details against your course material.", style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            Text("Beyond Chatbots • Kartik Setia", color = Muted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun Section(title: String, body: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, color = Mint, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SelectionContainer { Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = body) }
        }
    }
}
