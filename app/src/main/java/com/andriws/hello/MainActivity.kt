package com.andriws.hello

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val welcomeTextView = findViewById<TextView>(R.id.welcomeTextView)
        val profileImageView = findViewById<ImageView>(R.id.profileImageView)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val editProfileButton = findViewById<Button>(R.id.editProfileButton)

        loadUserProfile(welcomeTextView, profileImageView)

        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
    }

    private fun loadUserProfile(welcomeTextView: TextView, profileImageView: ImageView) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("perfiles").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombre = document.getString("nombre") ?: "Usuario"
                    val apellido = document.getString("apellido") ?: ""
                    welcomeTextView.text = "Bienvenido, $nombre $apellido"

                    val imageUrl = document.getString("profileImageUrl")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(imageUrl).into(profileImageView)
                    }
                }
            }
    }
}
