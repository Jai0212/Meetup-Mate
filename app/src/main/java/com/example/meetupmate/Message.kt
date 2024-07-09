package com.example.meetupmate

data class Message(
    val sender: User = User(),
    val message: String = "",
    val timestamp: Timestamp = Timestamp("", "")
)

data class Timestamp(
    val date: String = "",
    val time: String = ""
)
