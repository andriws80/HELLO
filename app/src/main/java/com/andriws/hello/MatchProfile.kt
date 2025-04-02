package com.andriws.hello

import android.os.Parcelable
import kotlinx.parcelize.Parcelize // Añadir esta importación

@Parcelize
data class MatchProfile(
    val name: String = "",
    val age: Int = 0,
    val city: String = "",
    val profileImageUrl: String? = null,
    val gender: String = "", //  Añadido para mostrar en los detalles (si lo necesitas)
    val nationality: String = "", // Añadido para mostrar en los detalles (si lo necesitas)
    val languages: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    // ... otros campos relevantes que quieras mostrar en la lista o en los detalles ...
) : Parcelable