package com.andriws.hello

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import androidx.activity.result.IntentSenderRequest

class LoginActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val googleSignInButton = findViewById<Button>(R.id.googleSignInButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Correo y contraseña son obligatorios", Toast.LENGTH_SHORT).show()
            } else {
                signInWithEmail(email, password)
            }
        }
    }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        firebaseAuthWithGoogle(idToken)
                    } else {
                        Log.w(TAG, "ID Token es nulo")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Google sign in failed", e)
                }
            }
        }

    private fun signInWithGoogle() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                googleSignInLauncher.launch(IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error en Google Sign-In", e)
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d(TAG, "Firebase Auth Success: ${user?.displayName}")
                    checkUserInFirestore()
                } else {
                    Log.w(TAG, "Firebase Auth Failed", task.exception)
                }
            }
    }

    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Inicio de sesión exitoso con email")
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    checkUserInFirestore()
                } else {
                    Log.w(TAG, "Error en inicio de sesión con email", task.exception)
                    Toast.makeText(this, task.exception?.localizedMessage ?: "Error en autenticación", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun checkUserInFirestore() {
        val user = auth.currentUser ?: return
        val email = user.email ?: "No disponible"
        val userRef = db.collection("perfiles").document(user.uid)

        userRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val perfil = hashMapOf(
                    "nombres" to (user.displayName ?: "Usuario"),
                    "email" to email,
                    "nacionalidad" to "No especificado"
                )
                userRef.set(perfil)
                    .addOnSuccessListener {
                        Log.d(TAG, "Perfil creado en Firestore")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error al crear perfil en Firestore", e)
                    }
            }
            goToMainScreen()
        }.addOnFailureListener { e ->
            Log.w(TAG, "Error al verificar perfil en Firestore", e)
        }
    }

    private fun goToMainScreen() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if (user != null) {
            goToMainScreen()
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}