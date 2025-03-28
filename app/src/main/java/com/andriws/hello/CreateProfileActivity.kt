package com.andriws.hello

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class CreateProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                imageUri = data?.data
                findViewById<ImageView>(R.id.profileImageView).setImageURI(imageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val nombresEditText = findViewById<EditText>(R.id.nombresEditText)
        val paisResidenciaSpinner = findViewById<Spinner>(R.id.paisResidenciaSpinner)
        val selectImageButton = findViewById<Button>(R.id.selectImageButton)
        val uploadImageButton = findViewById<Button>(R.id.uploadImageButton)
        val guardarPerfilButton = findViewById<Button>(R.id.guardarPerfilButton)

        selectImageButton.setOnClickListener {
            pickImageFromGallery()
        }

        uploadImageButton.setOnClickListener {
            if (imageUri != null) {
                uploadImageToFirebase { imageUrl ->
                    Toast.makeText(this, "Imagen subida correctamente", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Selecciona una imagen primero", Toast.LENGTH_SHORT).show()
            }
        }

        guardarPerfilButton.setOnClickListener {
            val nombres = nombresEditText.text.toString().trim()
            val pais = paisResidenciaSpinner.selectedItem.toString()

            if (nombres.isEmpty()) {
                Toast.makeText(this, "Ingrese los nombres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveProfileToFirestore(nombres, pais)
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun uploadImageToFirebase(onSuccess: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("profile_pictures/$userId.jpg")

        try {
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, imageUri!!)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            }

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
            val imageData = byteArrayOutputStream.toByteArray()

            storageRef.putBytes(imageData)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        firestore.collection("perfiles").document(userId)
                            .update("profileImageUrl", imageUrl)
                            .addOnSuccessListener {
                                onSuccess(imageUrl)
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProfileToFirestore(nombres: String, pais: String) {
        val userId = auth.currentUser?.uid ?: return
        val profileData = hashMapOf(
            "nombres" to nombres,
            "paisResidencia" to pais,
            "userId" to userId
        )

        firestore.collection("perfiles").document(userId)
            .set(profileData)
            .addOnSuccessListener {
                Toast.makeText(this, "Perfil guardado con éxito", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar el perfil", Toast.LENGTH_SHORT).show()
            }
    }
}
