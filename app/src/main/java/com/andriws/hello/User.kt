package com.andriws.hello

data class User(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val nationality: String = "",
    val race: String = "",
    val age: Int = 0,
    val personalityType: String = "",
    val educationLevel: String = "",
    val country: String = "",
    val city: String = "",
    val relationshipType: String = "",
    val children: Int = 0,
    val pets: String = "",
    val religion: String = "",
    val politicalIdeology: String = "",
    val alcoholConsumption: String = "",
    val tobaccoConsumption: String = "",
    val sports: List<String> = emptyList(),
    val hobbies: List<String> = emptyList(),
    val diet: String = "",
    val musicGenres: List<String> = emptyList(),
    val profileImageUrl: String = ""
)