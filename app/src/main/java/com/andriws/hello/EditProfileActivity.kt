package com.andriws.hello

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null

    private lateinit var nombreEditText: EditText
    private lateinit var apellidoEditText: EditText
    private lateinit var nacionalidadSpinner: Spinner
    private lateinit var profileImageView: ImageView
    private lateinit var changeImageButton: Button
    private lateinit var saveChangesButton: Button

    private val pickImageLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) { // Corregido: se eliminó "Activity."
            result.data?.data?.let { uri ->
                imageUri = uri
                profileImageView.setImageURI(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        nombreEditText = findViewById(R.id.nombreEditText)
        apellidoEditText = findViewById(R.id.apellidoEditText)
        nacionalidadSpinner = findViewById(R.id.nacionalidadSpinner)
        profileImageView = findViewById(R.id.profileImageView)
        changeImageButton = findViewById(R.id.changeImageButton)
        saveChangesButton = findViewById(R.id.guardarCambiosButton)

        loadUserProfile()

        changeImageButton.setOnClickListener {
            pickImageFromGallery()
        }

        saveChangesButton.setOnClickListener {
            val nombre = nombreEditText.text.toString().trim()
            val apellido = apellidoEditText.text.toString().trim()
            val nacionalidad = nacionalidadSpinner.selectedItem?.toString() ?: ""

            if (nombre.isEmpty() || apellido.isEmpty()) {
                Toast.makeText(this, "Ingrese su nombre y apellido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (imageUri != null) {
                uploadImageAndSaveProfile(nombre, apellido, nacionalidad)
            } else {
                updateProfileInFirestore(nombre, apellido, nacionalidad, null)
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun loadUserProfile() {
        auth.currentUser?.uid?.let { userId ->
            firestore.collection("perfiles").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nombre = document.getString("nombres") ?: "Usuario"
                        val apellido = document.getString("apellido") ?: "No especificado"
                        val nacionalidad = document.getString("nacionalidad") ?: "No especificado"

                        Log.d("Firestore", "Nombre: $nombre, Apellido: $apellido, Nacionalidad: $nacionalidad")

                        nombreEditText.setText(nombre)
                        apellidoEditText.setText(apellido)

                        val adapter = nacionalidadSpinner.adapter as? ArrayAdapter<String>
                        adapter?.getPosition(nacionalidad)?.let { index ->
                            if (index >= 0) nacionalidadSpinner.setSelection(index)
                        }

                        document.getString("profileImageUrl")?.takeIf { it.isNotEmpty() }?.let { imageUrl ->
                            Glide.with(this).load(imageUrl).into(profileImageView)
                        }
                    } else {
                        Log.e("Firestore", "El documento no existe en Firestore")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error al obtener el perfil", e)
                }
        }
    }

    private fun uploadImageAndSaveProfile(nombre: String, apellido: String, nacionalidad: String) {
        auth.currentUser?.uid?.let { userId ->
            val storageRef = storage.reference.child("profile_images/$userId.jpg")

            getBitmapFromUri(imageUri)?.let { bitmap ->
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                val imageData = baos.toByteArray()

                storageRef.putBytes(imageData)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            updateProfileInFirestore(nombre, apellido, nacionalidad, uri.toString())
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("EditProfile", "Error al subir imagen", e)
                        Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                    }
            } ?: Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getBitmapFromUri(uri: Uri?): Bitmap? {
        return try {
            uri?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(contentResolver, it)
                }
            }
        } catch (e: Exception) {
            Log.e("EditProfile", "Error al obtener el bitmap", e)
            null
        }
    }

    private fun updateProfileInFirestore(nombre: String, apellido: String, nacionalidad: String, imageUrl: String?) {
        auth.currentUser?.uid?.let { userId ->
            val profileUpdates = mutableMapOf(
                "nombres" to nombre,
                "apellido" to apellido,
                "nacionalidad" to nacionalidad
            )

            imageUrl?.let {
                profileUpdates["profileImageUrl"] = it
            }

            firestore.collection("perfiles").document(userId)
                .set(profileUpdates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
                    navigateNext()
                }
                .addOnFailureListener { e ->
                    Log.e("EditProfile", "Error al actualizar perfil", e)
                    Toast.makeText(this, "Error al actualizar perfil", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun navigateNext() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
