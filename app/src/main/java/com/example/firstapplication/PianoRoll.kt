package com.example.firstapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.example.firstapplication.ui.theme.ActiveFontColor
import com.example.firstapplication.ui.theme.MenuBack
import com.example.firstapplication.ui.theme.MenuVERYBack
import kotlin.math.max
import kotlin.math.min

private val NOTE_RANGE = 21..108

private const val PIXELS_PER_SECOND = 80
private const val NOTE_HEIGHT_DP = 24
private const val OCTAVE_LABEL_WIDTH_DP = 50

private val WHITE_NOTES = listOf("C", "D", "E", "F", "G", "A", "B")

fun noteToMidi(noteName: String): Int {
    val note = noteName.trim()
    val noteLetter = note.filter { it.isLetter() || it == '#' }
    val octave = note.filter { it.isDigit() }.toIntOrNull() ?: 4

    val noteNumber = when (noteLetter.uppercase()) {
        "C" -> 0
        "C#", "DB" -> 1
        "D" -> 2
        "D#", "EB" -> 3
        "E" -> 4
        "F" -> 5
        "F#", "GB" -> 6
        "G" -> 7
        "G#", "AB" -> 8
        "A" -> 9
        "A#", "BB" -> 10
        "B" -> 11
        else -> 0
    }

    return noteNumber + (octave + 1) * 12
}

fun midiToNote(midiNumber: Int): String {
    val octave = (midiNumber / 12) - 1
    val noteNumber = midiNumber % 12

    val noteName = when (noteNumber) {
        0 -> "C"
        1 -> "C#"
        2 -> "D"
        3 -> "D#"
        4 -> "E"
        5 -> "F"
        6 -> "F#"
        7 -> "G"
        8 -> "G#"
        9 -> "A"
        10 -> "A#"
        11 -> "B"
        else -> "C"
    }

    return "$noteName$octave"
}

fun isWhiteKey(noteName: String): Boolean {
    val noteLetter = noteName.filter { it.isLetter() || it == '#' }
    return noteLetter in listOf("C", "D", "E", "F", "G", "A", "B")
}

fun getNoteColor(velocity: Int): Color {
    val normalized = velocity.coerceIn(0, 127) / 127f

    return when {
        normalized >= 0.8f -> Color(0xFF4CAF50)
        normalized >= 0.6f -> Color(0xFF8BC34A)
        normalized >= 0.4f -> Color(0xFF2196F3)
        normalized >= 0.2f -> Color(0xFFFF9800)
        else -> Color(0xFFFFC107)
    }
}

@Composable
fun PianoRollView(
    notes: List<PianoNote>,
    modifier: Modifier = Modifier
) {
    if (notes.isEmpty()) {
        EmptyPianoRoll(modifier = modifier)
        return
    }

    val minNote = notes.minOf { noteToMidi(it.note) }
    val maxNote = notes.maxOf { noteToMidi(it.note) }
    val maxTime = notes.maxOf { it.time + it.duration }

    val noteRangeStart = max(NOTE_RANGE.first, minNote - 2)
    val noteRangeEnd = min(NOTE_RANGE.last, maxNote + 2)
    val totalWidthPx = ((maxTime + 2) * PIXELS_PER_SECOND).toInt()

    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    val density = androidx.compose.ui.platform.LocalDensity.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MenuVERYBack)
    ) {
        PianoRollHeader(notes = notes)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MenuBack),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .width(OCTAVE_LABEL_WIDTH_DP.dp)
                            .fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        0,
                                        -verticalScrollState.value
                                    )
                                }
                        ) {
                            NoteLabelsColumn(
                                noteRangeStart = noteRangeStart,
                                noteRangeEnd = noteRangeEnd,
                                modifier = Modifier.width(OCTAVE_LABEL_WIDTH_DP.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(totalWidthPx.dp)
                                .fillMaxHeight()
                                .verticalScroll(verticalScrollState)
                        ) {
                            PianoRollGrid(
                                notes = notes,
                                noteRangeStart = noteRangeStart,
                                noteRangeEnd = noteRangeEnd,
                                totalWidthPx = totalWidthPx
                            )
                        }
                    }
                }
            }
        }

        TimeAxis(
            maxTime = maxTime,
            scrollState = horizontalScrollState
        )
    }
}

@Composable
private fun EmptyPianoRoll(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(MenuBack, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No notes to display",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "Process an audio file to see notes",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun PianoRollHeader(notes: List<PianoNote>) {
    val totalDuration = notes.maxOfOrNull { it.time + it.duration } ?: 0f
    val noteRange = run {
        val min = notes.minOfOrNull { noteToMidi(it.note) } ?: 0
        val max = notes.maxOfOrNull { noteToMidi(it.note) } ?: 0
        max - min + 1
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatChip(label = "Notes", value = notes.size.toString())
        StatChip(label = "Duration", value = String.format("%.1fs", totalDuration))
        StatChip(label = "Range", value = "$noteRange keys")
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Surface(
        color = ActiveFontColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ActiveFontColor
            )
        }
    }
}

@Composable
private fun NoteLabelsColumn(
    noteRangeStart: Int,
    noteRangeEnd: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MenuBack)
    ) {
        for (midiNote in noteRangeEnd downTo noteRangeStart) {
            val noteName = midiToNote(midiNote)
            val isWhite = isWhiteKey(noteName)
            val isC = noteName.startsWith("C") && !noteName.startsWith("C#")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NOTE_HEIGHT_DP.dp)
                    .background(
                        when {
                            isC -> ActiveFontColor.copy(alpha = 0.1f)
                            isWhite -> Color(0xFFFAFAFA)
                            else -> Color(0xFFE0E0E0)
                        }
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = noteName,
                    fontSize = 10.sp,
                    fontWeight = if (isC) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isC -> ActiveFontColor
                        isWhite -> Color.Black
                        else -> Color.DarkGray
                    },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun PianoRollGrid(
    notes: List<PianoNote>,
    noteRangeStart: Int,
    noteRangeEnd: Int,
    totalWidthPx: Int
) {
    val noteCount = noteRangeEnd - noteRangeStart + 1

    Box(
        modifier = Modifier
            .width(totalWidthPx.dp)
            .height((noteCount * NOTE_HEIGHT_DP).dp)
    ) {
        for (midiNote in noteRangeStart..noteRangeEnd) {
            val noteName = midiToNote(midiNote)
            val isWhite = isWhiteKey(noteName)
            val isC = noteName.startsWith("C") && !noteName.startsWith("C#")

            val yPositionDp = (noteRangeEnd - midiNote) * NOTE_HEIGHT_DP

            Box(
                modifier = Modifier
                    .offset(y = yPositionDp.dp)
                    .width(totalWidthPx.dp)
                    .height(NOTE_HEIGHT_DP.dp)
                    .background(
                        when {
                            isC -> ActiveFontColor.copy(alpha = 0.05f)
                            isWhite -> Color.White
                            else -> Color(0xFFF5F5F5)
                        }
                    )
            )
        }

        val secondsCount = (totalWidthPx / PIXELS_PER_SECOND) + 1
        for (second in 0..secondsCount) {
            val xPositionDp = second * PIXELS_PER_SECOND

            Box(
                modifier = Modifier
                    .offset(x = xPositionDp.dp)
                    .width(1.dp)
                    .height((noteCount * NOTE_HEIGHT_DP).dp)
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )
        }

        notes.forEach { note ->
            val midiNote = noteToMidi(note.note)
            if (midiNote in noteRangeStart..noteRangeEnd) {
                val yPositionDp = (noteRangeEnd - midiNote) * NOTE_HEIGHT_DP
                val xPositionDp = (note.time * PIXELS_PER_SECOND).toInt()
                val noteWidthDp = max(note.duration * PIXELS_PER_SECOND, 5f).toInt()

                Box(
                    modifier = Modifier
                        .offset(x = xPositionDp.dp, y = yPositionDp.dp)
                        .width(noteWidthDp.dp)
                        .height(NOTE_HEIGHT_DP.dp)
                        .padding(1.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    getNoteColor(note.velocity),
                                    getNoteColor(note.velocity).copy(alpha = 0.8f)
                                )
                            )
                        )
                ) {
                    if (note.duration > 0.2f) {
                        Text(
                            text = note.note,
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeAxis(
    maxTime: Float,
    scrollState: ScrollState
) {
    val seconds = (maxTime / 1f).toInt() + 2

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = OCTAVE_LABEL_WIDTH_DP.dp)
    ) {
        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.Start
        ) {
            for (second in 0..seconds) {
                Box(
                    modifier = Modifier.width(PIXELS_PER_SECOND.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${second}s",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}