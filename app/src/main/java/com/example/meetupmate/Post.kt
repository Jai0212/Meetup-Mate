package com.example.meetupmate

data class Post(
    val creatorEmail: String,
    val image: String,
    val title: String,
    val dateAndTime: String,
    val description: String
) {
    constructor() : this("", "", "", "", "")
}