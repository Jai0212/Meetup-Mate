package com.example.meetupmate

data class User(
    val email: String,
    val username: String,
    val password: String,
    var profileImage: String
) {
    constructor() : this("", "", "", "")
}

