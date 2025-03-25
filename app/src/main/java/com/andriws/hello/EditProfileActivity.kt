package com.andriws.hello

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var changeImageButton: Button
    private lateinit var nombresEditText: EditText
    private lateinit var nacionalidadSpinner: Spinner
    private lateinit var guardarCambiosButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private var imageUri: Uri? = null

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data?.data
                Glide.with(this).load(imageUri).into(profileImageView)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        profileImageView = findViewById(R.id.profileImageView)
        changeImageButton = findViewById(R.id.changeImageButton)
        nombresEditText = findViewById(R.id.nombresEditText)
        nacionalidadSpinner = findViewById(R.id.nacionalidadSpinner)
        guardarCambiosButton = findViewById(R.id.guardarCambiosButton)
        progressBar = findViewById(R.id.progressBar)

        progressBar.visibility = View.GONE

        changeImageButton.setOnClickListener {
            selectImage()
        }

        configurarSpinner()
        cargarPerfil()

        guardarCambiosButton.setOnClickListener {
            validateAndSaveProfile()
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun uploadImage(onSuccess: (String) -> Unit) {
        if (imageUri == null) {
            onSuccess("")
            return
        }

        progressBar.visibility = View.VISIBLE
        val storageRef = storage.reference.child("profile_images/${auth.currentUser?.uid}.jpg")

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                    progressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Snackbar.make(guardarCambiosButton, "Error al subir imagen", Snackbar.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
            }
    }

    private fun cargarPerfil() {
        val usuarioId = auth.currentUser?.uid ?: return

        db.collection("perfiles").document(usuarioId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    nombresEditText.setText(document.getString("nombres"))

                    val nacionalidad = document.getString("nacionalidad")
                    val nacionalidadArray = resources.getStringArray(R.array.nacionalidades)
                    val position = nacionalidadArray.indexOf(nacionalidad)
                    if (position >= 0) {
                        nacionalidadSpinner.setSelection(position)
                    }

                    val imageUrl = document.getString("imagenPerfil")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_error_image)
                            .into(profileImageView)
                    }
                }
            }
            .addOnFailureListener {
                Snackbar.make(guardarCambiosButton, "Error al cargar perfil", Snackbar.LENGTH_LONG).show()
            }
    }

    private fun validateAndSaveProfile() {
        val nombres = nombresEditText.text.toString().trim()
        val nacionalidad = nacionalidadSpinner.selectedItem.toString()

        if (nombres.isEmpty()) {
            nombresEditText.error = "Este campo es obligatorio"
            return
        }

        uploadImage { imageUrl ->
            val usuarioId = auth.currentUser?.uid ?: return@uploadImage
            val perfilActualizado = hashMapOf(
                "nombres" to nombres,
                "nacionalidad" to nacionalidad,
                "imagenPerfil" to imageUrl
            )

            db.collection("perfiles").document(usuarioId)
                .update(perfilActualizado as Map<String, Any>)
                .addOnSuccessListener {
                    Snackbar.make(guardarCambiosButton, "Perfil guardado con éxito", Snackbar.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Snackbar.make(guardarCambiosButton, "Error al guardar el perfil", Snackbar.LENGTH_LONG).show()
                }
        }
    }

    private fun configurarSpinner() {
        val nacionalidades = resources.getStringArray(R.array.nacionalidades)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nacionalidades)
        nacionalidadSpinner.adapter = adapter
    }
}


