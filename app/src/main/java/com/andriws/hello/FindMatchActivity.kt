package com.andriws.hello

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.andriws.hello.databinding.ActivityFindMatchBinding
import com.google.firebase.auth.FirebaseAuth // Importa FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FindMatchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFindMatchBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: MatchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindMatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        firestore = FirebaseFirestore.getInstance()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MatchAdapter(emptyList<SearchResult>())
        binding.recyclerView.adapter = adapter

        fetchMatches()
    }

    private fun fetchMatches() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    val userCity = document.getString("city") ?: ""
                    if (userCity.isNotEmpty()) {
                        queryMatchesByCity(userCity)
                    } else {
                        Log.w("FindMatchActivity", "Ciudad del usuario no encontrada en Firestore.")
                        // TODO: Manejar el caso en que la ciudad del usuario no está definida (mostrar un mensaje al usuario, etc.)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("FindMatchActivity", "Error al obtener la ciudad del usuario: ", exception)
                    // TODO: Manejar el error al obtener la ciudad (mostrar un mensaje al usuario, etc.)
                }
        } else {
            Log.w("FindMatchActivity", "Usuario no autenticado.")
            // TODO: Manejar el caso en que el usuario no está autenticado (quizás redirigir a la pantalla de inicio de sesión)
        }
    }

    private fun queryMatchesByCity(userCity: String) {
        firestore.collection("users") // O "couples"
            .whereEqualTo("city", userCity)
            .get()
            .addOnSuccessListener { documents ->
                val matchList = documents.map { document ->
                    SearchResult().apply {
                        name = document.getString("name") ?: ""
                        age = document.getLong("age")?.toInt() ?: 0
                        gender = document.getString("gender") ?: ""
                        nationality = document.getString("nationality") ?: ""
                        city = document.getString("city") ?: ""
                        languages = document.get("languages") as? List<String> ?: emptyList()
                        interests = document.get("interests") as? List<String> ?: emptyList()
                        profileImageUrl = document.getString("profileImageUrl")
                    }
                }
                val shuffledList = matchList.shuffled()
                adapter.updateData(shuffledList)
            }
            .addOnFailureListener { exception ->
                Log.w("FindMatchActivity", "Error al obtener las parejas: ", exception)
                // TODO: Manejar el error al obtener las parejas (mostrar un mensaje al usuario, etc.)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.find_match_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                //  TODO: Implementar la lógica para cerrar sesión (Firebase Auth)
                //  y navegar a la pantalla de inicio de sesión.
                true
            }
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}