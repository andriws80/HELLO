package com.andriws.hello

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot

class MatchViewModel : ViewModel() {

    //  Aquí irán las funciones para calcular la compatibilidad y encontrar matches

    fun calculateCompatibilityScore(user1: DocumentSnapshot, user2: DocumentSnapshot, isPremium: Boolean): Double {
        var score = 0.0

        // Criterios gratuitos
        if (user1.getString("ciudad") == user2.getString("ciudad")) score += 0.05
        if (user1.getString("pais") == user2.getString("pais")) score += 0.05
        if (isInPreferredAgeRange(user2, user1) && isInPreferredAgeRange(user1, user2)) score += 0.1
        if (user1.getString("tipoRelacion") == user2.getString("tipoRelacion")) score += 0.05
        val nacionalidadDeseada = user2.getString("nacionalidadDeseada") ?: "" // Provide default empty string
        val nacionalidad = user1.getString("nacionalidad") ?: "" // Provide default empty string
        if (nacionalidadDeseada.contains(nacionalidad)) score += 0.05
        score += calculateSharedPreferencesScore(user1, user2)

        if (isPremium) {
            // Criterios premium
            if (user1.get("nivelAcademico") == user2.get("nivelAcademico")) score += 0.1
            if (areMBTICompatible(user1, user2)) score += 0.1
            if (desiredChildrenMatch(user1, user2)) score += 0.1
            if (user1.getBoolean("aceptaMascotas") == user2.getBoolean("tieneMascotas")) score += 0.05
            if (user1.getString("dieta") == user2.getString("dieta")) score += 0.05
            if (user1.getString("ideologiaPolitica") == user2.getString("ideologiaPolitica")) score += 0.1
        }

        return score.coerceIn(0.0, 1.0)
    }

    private fun isInPreferredAgeRange(user: DocumentSnapshot, otherUser: DocumentSnapshot): Boolean {
        val preferredAgeMin = otherUser.getLong("edadMinimaPreferida")?.toInt() ?: 18
        val preferredAgeMax = otherUser.getLong("edadMaximaPreferida")?.toInt() ?: 100
        val userAge = user.getLong("edad")?.toInt() ?: return false
        return userAge in preferredAgeMin..preferredAgeMax
    }

    private fun calculateSharedPreferencesScore(user1: DocumentSnapshot, user2: DocumentSnapshot): Double {
        var score = 0.0

        val sharedReligion = user1.getString("religion") == user2.getString("religion")
        val sharedAlcohol = user1.getString("consumoAlcohol") == user2.getString("consumoAlcohol")
        val sharedTabaco = user1.getString("consumoTabaco") == user2.getString("consumoTabaco")

        if (sharedReligion) score += 0.01
        if (sharedAlcohol) score += 0.01
        if (sharedTabaco) score += 0.01

        val hobbies1 = user1.get("hobbies") as? List<String> ?: emptyList()
        val hobbies2 = user2.get("hobbies") as? List<String> ?: emptyList()
        val sharedHobbies = hobbies1.intersect(hobbies2.toSet()).size
        score += (sharedHobbies * 0.01).coerceAtMost(0.05)

        val deportes1 = user1.get("deportes") as? List<String> ?: emptyList()
        val deportes2 = user2.get("deportes") as? List<String> ?: emptyList()
        val sharedDeportes = deportes1.intersect(deportes2.toSet()).size
        score += (sharedDeportes * 0.01).coerceAtMost(0.05)

        return score
    }

    private fun areMBTICompatible(user1: DocumentSnapshot, user2: DocumentSnapshot): Boolean {
        val personality1 = user1.get("personalidad") as? List<String> ?: emptyList()
        val personality2 = user2.get("personalidad") as? List<String> ?: emptyList()

        return personality1.any { it in personality2 }
    }

    private fun desiredChildrenMatch(user1: DocumentSnapshot, user2: DocumentSnapshot): Boolean {
        val desiredChildren1 = user1.getLong("numeroHijosDeseado")?.toInt() ?: -1
        val desiredChildren2 = user2.getLong("numeroHijosDeseado")?.toInt() ?: -1
        val actualChildren2 = user2.getLong("numeroHijos")?.toInt() ?: 0

        return if (desiredChildren1 == -1 || desiredChildren2 == -1) {
            true
        } else {
            desiredChildren1 == actualChildren2
        }
    }

    //  Suponiendo que tienes una clase de datos Match definida en otro archivo:
    // data class Match(val user1Id: String, val user2Id: String, val score: Double, val isPremiumMatch: Boolean)

    fun findMatches(currentUserProfile: DocumentSnapshot, allUserProfiles: List<DocumentSnapshot>, isPremium: Boolean): List<Match> {
        val matches = mutableListOf<Match>()
        val currentUserId = currentUserProfile.id

        for (otherProfile in allUserProfiles) {
            if (otherProfile.id != currentUserId) {
                val score = calculateCompatibilityScore(currentUserProfile, otherProfile, isPremium)
                if (score > 0.0) {
                    val match = Match(
                        user1Id = currentUserId,
                        user2Id = otherProfile.id,
                        score = score,
                        isPremiumMatch = isPremium
                    )
                    matches.add(match)
                }
            }
        }
        return matches.sortedByDescending { it.score }
    }
}