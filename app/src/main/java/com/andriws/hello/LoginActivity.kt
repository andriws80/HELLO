package com.andriws.hello

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager  // Importante
//import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
//import androidx.credentials.exceptions.ClearCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser // Importante
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var credentialManager: CredentialManager // Importante
    private val tag = "LoginActivity" // Buena práctica

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        credentialManager = CredentialManager.create(this)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerTextView = findViewById<TextView>(R.id.registerTextView)
        val googleSignInButton = findViewById<Button>(R.id.googleSignInButton) // Asumo que tienes este botón
        // Instantiate a Google sign-in request
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false) // <-- Permitir elegir cualquier cuenta
            .setAutoSelectEnabled(false) // <-- Importante: evita intentos silenciosos
            .build()
        // Create the Credential Manager request
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        googleSignInButton.setOnClickListener {  // Listener para el botón de Google
            getGoogleCredential(request)
        }
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(tag, "signInWithEmailAndPassword:success")
                            updateUI(auth.currentUser)
                            //  startActivity(Intent(this, CreateProfileActivity::class.java))  // Descomenta si necesitas navegar
                            //  finish()
                        } else {
                            Log.w(tag, "signInWithEmailAndPassword:failure", task.exception)
                            Toast.makeText(this, "Error al iniciar sesión: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            updateUI(null)
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor, ingresa correo y contraseña.", Toast.LENGTH_SHORT).show()
            }
        }
        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        val currentUser = auth.currentUser  //  Muevo esta lógica aquí
        if (currentUser != null) {
            Log.d(tag, "Usuario autenticado. UID: ${currentUser.uid}") // Agrega este log
            // No hacemos nada más aquí.  Dejamos que el usuario continúe en la Activity actual
            // (que, en un flujo correcto, sería RegisterActivity).
        } else {
            // No hay usuario autenticado localmente
            Log.d(tag, "No hay usuario autenticado localmente, mostrando pantalla de inicio de sesión.")
            updateUI(null)
            // Los listeners ya están configurados arriba, así que no es necesario repetirlos
        }
    }

    private fun getGoogleCredential(request: GetCredentialRequest) {
        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(this@LoginActivity, request)
                handleSignIn(result.credential)
            } catch (e: Exception) { //  Usar Exception, no GetCredentialException (más genérico)
                Log.e(tag, "Error getting credential: ${e.message}")
                Toast.makeText(this@LoginActivity, "Error al obtener credenciales de Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun handleSignIn(credential: Credential) {
        if (credential is GoogleIdTokenCredential) {
            val idToken = credential.idToken
            if (idToken.isNotEmpty()) { //  Comprobación simplificada
                Log.d(tag, "✅ ID Token recibido: $idToken")
                firebaseAuthWithGoogle(idToken)
            } else {
                Log.e(tag, "❌ ID Token está vacío")  // Mensaje actualizado
                Toast.makeText(this, "Error: el ID token está vacío.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(tag, "❌ Credential no es de tipo GoogleIdTokenCredential (esperado): ${credential.javaClass.name}")
            Toast.makeText(this, "No se obtuvo una cuenta válida de Google.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(tag, "signInWithCredential:success - Usuario autenticado con Google en Firebase.")  //  Log añadido
                    updateUI(auth.currentUser)
                } else {
                    Log.w(tag, "signInWithCredential:failure - Error al autenticar con Google en Firebase: ${task.exception?.message}")  // Log añadido
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(this, "Inicio de sesión exitoso como: ${user.email ?: "usuario"}", Toast.LENGTH_SHORT).show()
            // Guardar información del usuario en Firestore (sin cambios)
            //saveUserToFirestore(user)
            // Modificación: Pasar el extra "auth_method" con valor "google"
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("auth_method", "google")
            startActivity(intent)
        } else {
            Toast.makeText(this, "Cierre de sesión o inicio de sesión fallido", Toast.LENGTH_SHORT).show()
            // Modificación:  Pasar el extra "auth_method" con valor "email" (o cualquier otro valor que uses para el registro normal)
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("auth_method", "email") //  Asegúrate de usar el mismo valor en el `else` de `RegisterActivity`
            startActivity(intent)
        }
    }
    private fun saveUserToFirestore(user: FirebaseUser) {
        val userMap = hashMapOf(
            "uid" to user.uid,
            "email" to user.email,
            //  Añade otros campos que quieras guardar, por ejemplo:
            // "displayName" to user.displayName,
            // "photoUrl" to user.photoUrl?.toString()  (Asegúrate de manejar el caso nulo)
        )
        Log.d(tag, "Datos a guardar en Firestore: $userMap") // Agrega este log

        firestore.collection("users")
            .document(user.uid)
            .set(userMap)
            .addOnSuccessListener {
                Log.d(tag, "Información del usuario guardada en Firestore: ${user.uid}")
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Error al guardar información del usuario en Firestore: ${e.message}", e)
                Toast.makeText(this, "Error al guardar información del usuario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }





}