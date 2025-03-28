package com.andriws.hello

data class Couple(
    val id: String = "",
    val user1: User,
    val user2: User,
    val preferredNationalities: List<String> = emptyList(),
    val preferredRaces: List<String> = emptyList(),
    val preferredAgeRange: Pair<Int, Int> = 18 to 99,
    val preferredEducationLevels: List<String> = emptyList(),
    val preferredPersonalityTypes: List<String> = emptyList(),
    val preferredLocation: Pair<String, String> = "" to "",
    val preferredRelationshipType: String = "",
    val preferredChildren: Int? = null,
    val preferredPets: String = "",
    val preferredReligion: String = "",
    val preferredPoliticalIdeology: String = "",
    val preferredAlcoholConsumption: String = "",
    val preferredTobaccoConsumption: String = "",
    val preferredSports: List<String> = emptyList(),
    val preferredHobbies: List<String> = emptyList(),
    val preferredDiet: String = "",
    val preferredMusicGenres: List<String> = emptyList()
)
