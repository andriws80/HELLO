package com.andriws.hello

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.andriws.hello.databinding.ActivityFindMatchBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FindMatchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFindMatchBinding
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val matchViewModel: MatchViewModel by viewModels()
    private lateinit var adapter: MatchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindMatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MatchAdapter(emptyList()) // Adaptador ahora maneja List<Match>
        binding.recyclerView.adapter = adapter

        fetchCurrentUserAndMatches()
    }

    private fun fetchCurrentUserAndMatches() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { currentUserProfile ->
                    if (currentUserProfile != null) {
                        fetchAllUserProfiles(currentUserProfile)
                    } else {
                        Log.w("FindMatchActivity", "Perfil del usuario actual no encontrado.")
                        // TODO: Manejar el caso en que el perfil del usuario no existe
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("FindMatchActivity", "Error al obtener el perfil del usuario: ", exception)
                    // TODO: Manejar el error al obtener el perfil del usuario
                }
        } else {
            Log.w("FindMatchActivity", "Usuario no autenticado.")
            // TODO: Manejar el caso en que el usuario no está autenticado
        }
    }

    private fun fetchAllUserProfiles(currentUserProfile: com.google.firebase.firestore.DocumentSnapshot) {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { allUserProfilesSnapshot ->
                val allUserProfiles = allUserProfilesSnapshot.documents
                if (allUserProfiles.isNotEmpty()) {
                    calculateAndDisplayMatches(currentUserProfile, allUserProfiles)
                } else {
                    Log.w("FindMatchActivity", "No se encontraron otros perfiles de usuario.")
                    // TODO: Manejar el caso en que no hay otros perfiles
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FindMatchActivity", "Error al obtener otros perfiles: ", exception)
                // TODO: Manejar el error al obtener otros perfiles
            }
    }

    private fun calculateAndDisplayMatches(
        currentUserProfile: com.google.firebase.firestore.DocumentSnapshot,
        allUserProfiles: List<com.google.firebase.firestore.DocumentSnapshot>
    ) {
        //  Asume que tienes un mecanismo para determinar si el usuario es premium (ej., en el perfil)
        val isPremiumUser = currentUserProfile.getBoolean("esPremium") ?: false

        val matches = matchViewModel.findMatches(currentUserProfile, allUserProfiles, isPremiumUser)
        displayMatches(matches)
    }

    private fun displayMatches(matches: List<Match>) {
        adapter.updateData(matches)
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