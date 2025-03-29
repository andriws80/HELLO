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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import com.bumptech.glide.Glide

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null

    // Declaración de vistas (corregida e inicializada tardíamente)
    private lateinit var nombreEditText: EditText
    private lateinit var apellidoEditText: EditText
    private lateinit var nacionalidadSpinner: Spinner
    private lateinit var profileImageView: ImageView
    private lateinit var changeImageButton: Button
    private lateinit var saveChangesButton: Button

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                imageUri = data?.data
                profileImageView.setImageURI(imageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Inicialización de vistas (asegúrate de que los IDs coincidan con activity_edit_profile.xml)
        nombreEditText = findViewById(R.id.nombres)  //  Corregido:  R.id.nombres
        apellidoEditText = findViewById(R.id.apellidoEditText)
        nacionalidadSpinner = findViewById(R.id.nacionalidadSpinner)
        profileImageView = findViewById(R.id.profileImageView)
        changeImageButton = findViewById(R.id.changeImageButton)
        saveChangesButton = findViewById(R.id.guardarCambiosButton) //  Corregido:  R.id.guardarCambiosButton


        changeImageButton.setOnClickListener { pickImage() }
        saveChangesButton.setOnClickListener { saveChanges() }

        loadUserProfile()
    }


    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val nombre = document.getString("nombre") ?: ""
                    val apellido = document.getString("apellido") ?: ""
                    val nacionalidad = document.getString("nacionalidad") ?: ""
                    val profileImageUrl = document.getString("profileImageUrl")

                    nombreEditText.setText(nombre)
                    apellidoEditText.setText(apellido)
                    //  Configurar el Spinner con la nacionalidad (necesitas un adaptador)
                    //  Ejemplo (debes adaptarlo a tu lista de nacionalidades):
                    val nacionalidades = resources.getStringArray(R.array.nacionalidades) //  Asegúrate de tener este array en res/values/strings.xml
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nacionalidades)
                    nacionalidadSpinner.adapter = adapter
                    val position = nacionalidades.indexOf(nacionalidad)
                    if (position != -1) {
                        nacionalidadSpinner.setSelection(position)
                    }

                    // Cargar la imagen de perfil si existe
                    if (profileImageUrl != null && profileImageUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_profile) //  Asegúrate de tener una imagen de placeholder
                            .into(profileImageView)
                    } else {
                        profileImageView.setImageResource(R.drawable.ic_profile)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("EditProfile", "Error cargando el perfil", e)
                Toast.makeText(this, "Error al cargar el perfil", Toast.LENGTH_SHORT).show()
            }
    }



    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun saveChanges() {
        val nombre = nombreEditText.text.toString()
        val apellido = apellidoEditText.text.toString()
        val nacionalidad = nacionalidadSpinner.selectedItem.toString()

        if (nombre.isBlank() || apellido.isBlank() || nacionalidad.isBlank()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val userData = hashMapOf(
            "nombre" to nombre,
            "apellido" to apellido,
            "nacionalidad" to nacionalidad
        )


        if (imageUri != null) {
            uploadImageToFirebase(userId, userData)
        } else {
            updateUserProfile(userId, userData, null)
        }
    }


    private fun uploadImageToFirebase(userId: String, userData: HashMap<String, String>, ) {
        val storageRef = storage.reference.child("profile_images/$userId.jpg")

        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, imageUri!!)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            }

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)  //  Reducir la calidad para un menor tamaño
            val data = baos.toByteArray()

            storageRef.putBytes(data)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        userData["profileImageUrl"] = downloadUri.toString()  //  Añadir la URL de la imagen
                        updateUserProfile(userId, userData, downloadUri)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EditProfile", "Error al subir la imagen", e)
                    Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.e("EditProfile", "Error al procesar la imagen", e)
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
        }
    }



    private fun updateUserProfile(userId: String, userData: HashMap<String, String>, imageUri: Uri?) {
        firestore.collection("users").document(userId)
            .update(userData as Map<String, Any>)  //  Necesario para que funcione con el "update"
            .addOnSuccessListener {
                Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                //  Opcional:  Navegar a otra actividad o simplemente mostrar un mensaje
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("EditProfile", "Error al actualizar el perfil", e)
                Toast.makeText(this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show()
            }
    }
}