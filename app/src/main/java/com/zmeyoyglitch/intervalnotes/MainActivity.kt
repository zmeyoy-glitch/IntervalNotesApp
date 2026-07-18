package com.zmeyoyglitch.intervalnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zmeyoyglitch.intervalnotes.ui.theme.IntervalNotesAppTheme

class MainActivity : ComponentActivity() {
    
    private val viewModel: NotesViewModel by viewModel()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            IntervalNotesAppTheme {
                MaterialTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NotesScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun NotesScreen(viewModel: NotesViewModel) {
    var selectedTimeSlot by remember { mutableStateOf<String?>(null) }
    var noteContent by remember { mutableStateOf("") }
    
    // Generate time slots from 17:00 to 20:00 with 15 min step
    val timeSlots = remember {
        (17..20).flatMap { hour ->
            (hour * 60)..((if (hour == 20) 1200 else (hour + 1)) * 60 - 15)
                .step(15)
                .map { minute -> "${String.format("%02d", minute / 60)}:${String.format("%02d", minute % 60)}" }
        }.sorted()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Interval Notes",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Выберите временной интервал:",
            style = MaterialTheme.typography.bodyMedium
        )
        
        // Time slot selection grid
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(timeSlots.size) { index ->
                val timeSlot = timeSlots[index]
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedTimeSlot == timeSlot) 
                            MaterialTheme.colorScheme.primaryContainer else 
                            MaterialTheme.colorScheme.surface
                    ),
                    onClick = { selectedTimeSlot = timeSlot }
                ) {
                    Text(
                        text = timeSlot,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (selectedTimeSlot == timeSlot) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        
        // Note content input
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ваша заметка:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Введите текст заметки...") },
                    minLines = 3,
                    maxLines = 5
                )
            }
        }
        
        // Save button (only enabled if time slot is selected)
        Button(
            onClick = {
                if (!selectedTimeSlot.isNullOrEmpty() && noteContent.isNotEmpty()) {
                    viewModel.saveNote(selectedTimeSlot!!, noteContent)
                    noteContent = ""
                    selectedTimeSlot = null
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !selectedTimeSlot.isNullOrEmpty() && noteContent.isNotEmpty()
        ) {
            Text("Сохранить заметку")
        }
        
        // Display saved notes count
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Сохранено заметок: ${viewModel.getNotesCount()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}