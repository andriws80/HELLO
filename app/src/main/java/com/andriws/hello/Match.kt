
package com.andriws.hello

data class Match(
    val user1Id: String = "",
    val user2Id: String = "",
    val score: Double = 0.0,
    val isPremiumMatch: Boolean = false,
    val timestamp: Long = System.currentTimeMillis() // Opcional
)