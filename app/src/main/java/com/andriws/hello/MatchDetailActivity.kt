package com.andriws.hello

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.andriws.hello.databinding.ActivityMatchDetailBinding
import com.bumptech.glide.Glide
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import androidx.compose.ui.semantics.text

class MatchDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMatchDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val searchResult = intent.getParcelableExtra<SearchResult>("searchResult")

        if (searchResult != null) {
            displayMatchDetails(searchResult)
        } else {
            // Manejar el caso en que no se recibieron datos (mostrar mensaje de error, cerrar actividad)
            finish()
        }
    }

    private fun displayMatchDetails(match: SearchResult) {
        binding.textViewName.text = match.name
        binding.textViewAge.text = match.age.toString()
        binding.textViewGender.text = match.gender
        binding.textViewNationality.text = match.nationality
        binding.textViewCity.text = match.city
        binding.textViewLanguages.text = match.languages.joinToString(", ")
        binding.textViewInterests.text = match.interests.joinToString(", ")

        if (match.profileImageUrl != null) {
            Glide.with(this)
                .load(match.profileImageUrl)
                .placeholder(R.drawable.ic_profile_placeholder) // Reemplaza con tu placeholder
                .into(binding.imageViewProfile)
        } else {
            binding.imageViewProfile.setImageResource(R.drawable.ic_profile_placeholder) // Reemplaza con tu placeholder
        }
    }
}

// Asegúrate de que SearchResult implementa Parcelable y tiene la anotación @Parcelize
@Parcelize
class SearchResult(
    var name: String = "",
    var age: Int = 0,
    var gender: String = "",
    var nationality: String = "",
    var city: String = "",
    var languages: List<String> = emptyList(),
    var interests: List<String> = emptyList(),
    var profileImageUrl: String? = null
) : Parcelable