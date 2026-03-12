package com.example.firstapplication


data class PianoResponse(
    val notes: List<PianoNote>,
    val chords: List<Chord>,
    val tempo: Int,
    val key: String,
    val timeSignature: String
)

data class PianoNote(
    val time: Float,
    val note: String,
    val duration: Float,
    val velocity: Int
)

data class Chord(
    val time: Double,
    val name: String,
    val notes: List<String>
)


data class Link_Post(
    val link: String
)