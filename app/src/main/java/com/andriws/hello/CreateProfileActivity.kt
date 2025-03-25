package com.andriws.hello

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

class CreateProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var uploadImageButton: Button
    private lateinit var nacionalidadSpinner: Spinner
    private var imageUri: Uri? = null
    private val storageReference = FirebaseStorage.getInstance().reference
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            profileImageView.setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile)

        profileImageView = findViewById(R.id.profileImageView)
        selectImageButton = findViewById(R.id.selectImageButton)
        uploadImageButton = findViewById(R.id.uploadImageButton)
        nacionalidadSpinner = findViewById(R.id.nacionalidadSpinner)

        // Configurar Spinner de Nacionalidades
        val nacionalidades = listOf("Argentina", "Brasil", "Colombia", "México", "España", "Otro")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nacionalidades)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        nacionalidadSpinner.adapter = adapter

        // Seleccionar imagen de la galería
        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        // Subir imagen a Firebase Storage
        uploadImageButton.setOnClickListener {
            imageUri?.let { uri ->
                uploadImageToFirebase(uri)
            } ?: Toast.makeText(this, "Selecciona una imagen primero", Toast.LENGTH_SHORT).show()
        }

        // Cargar imagen actual si existe
        loadUserProfileImage()
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val fileRef = storageReference.child("users/$userId/profile.jpg")

        try {
            val source = ImageDecoder.createSource(contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(source)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val data = baos.toByteArray()

            fileRef.putBytes(data)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        saveImageUrlToFirestore(downloadUri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
            Log.e("UPLOAD_IMAGE", "Error al decodificar imagen", e)
        }
    }

    private fun saveImageUrlToFirestore(imageUrl: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("perfiles").document(userId)
            .update("fotoPerfil", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(this, "Imagen guardada", Toast.LENGTH_SHORT).show()
                Picasso.get().load(imageUrl).into(profileImageView)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar imagen", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserProfileImage() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("perfiles").document(userId).get()
            .addOnSuccessListener { document ->
                val imageUrl = document.getString("fotoPerfil")
                if (!imageUrl.isNullOrEmpty()) {
                    Picasso.get().load(imageUrl).into(profileImageView)
                }

                @Suppress("UNCHECKED_CAST")
                val adapter = nacionalidadSpinner.adapter as? ArrayAdapter<String>
                val nacionalidad = document.getString("nacionalidad")

                if (adapter != null) {
                    val posicion = nacionalidad?.let { adapter.getPosition(it) } ?: -1
                    if (posicion >= 0) {
                        nacionalidadSpinner.setSelection(posicion)
                    }
                } else {
                    Log.e("SPINNER", "El adaptador del Spinner no es un ArrayAdapter<String>")
                }
            }
            .addOnFailureListener {
                Log.e("FIRESTORE", "Error al obtener imagen de perfil")
            }
    }
}
