package com.andriws.hello

import android.Manifest
//import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var photoAdapter: PhotoAdapter
    private val selectedImageUris: MutableList<Uri> = mutableListOf()
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var progressBar: ProgressBar
    private lateinit var registerButton: Button
    private lateinit var nameField: EditText
    private lateinit var lastnameField: EditText
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var name2Field: EditText
    private lateinit var lastname2Field: EditText


    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize views
        nameField = findViewById(R.id.nameField)
        lastnameField = findViewById(R.id.lastnameField)
        emailField = findViewById(R.id.emailField)
        passwordField = findViewById(R.id.passwordField)
        name2Field = findViewById(R.id.name2Field)
        lastname2Field = findViewById(R.id.lastname2Field)
        val nacionalidadSpinner = findViewById<Spinner>(R.id.nacionalidadSpinner)
        val razaSpinner = findViewById<Spinner>(R.id.razaSpinner)
        val edadSpinner = findViewById<Spinner>(R.id.edadSpinner)
        val personalidadSpinner = findViewById<Spinner>(R.id.personalidadSpinner)
        val nacionalidad2Spinner = findViewById<Spinner>(R.id.nacionalidad2Spinner)
        val raza2Spinner = findViewById<Spinner>(R.id.raza2Spinner)
        val edad2Spinner = findViewById<Spinner>(R.id.edad2Spinner)
        val personalidad2Spinner = findViewById<Spinner>(R.id.personalidad2Spinner)
        registerButton = findViewById(R.id.registerButton)
        photoRecyclerView = findViewById(R.id.photoRecyclerView)
        progressBar = findViewById(R.id.progressBar)

        // Configure Spinners (no changes here)
        setupSpinners(nacionalidadSpinner, R.array.nacionalidades)
        setupSpinners(razaSpinner, R.array.razas)
        setupSpinners(edadSpinner, R.array.edades)
        setupSpinners(personalidadSpinner, R.array.personalidades)
        setupSpinners(nacionalidad2Spinner, R.array.nacionalidades)
        setupSpinners(raza2Spinner, R.array.razas)
        setupSpinners(edad2Spinner, R.array.edades)
        setupSpinners(personalidad2Spinner, R.array.personalidades)

        // Configure RecyclerView for photos (no changes here)
        photoAdapter = PhotoAdapter(selectedImageUris) { position ->
            selectedImageUris.removeAt(position)
            photoAdapter.notifyItemRemoved(position)
        }
        photoRecyclerView.layoutManager = GridLayoutManager(this, 2)
        photoRecyclerView.adapter = photoAdapter

        // Initialize ActivityResultLauncher for image selection
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                if (data?.clipData != null) {
                    val clipData = data.clipData
                    val count = clipData!!.itemCount
                    val startPosition = selectedImageUris.size
                    for (i in 0 until count) {
                        val imageUri = clipData.getItemAt(i).uri
                        if (selectedImageUris.size < 4) {
                            selectedImageUris.add(imageUri)
                        } else {
                            Toast.makeText(this, getString(R.string.max_photos_allowed), Toast.LENGTH_SHORT).show()
                            break
                        }
                    }
                    val insertedCount = selectedImageUris.size - startPosition
                    if (insertedCount > 0) {
                        photoAdapter.notifyItemRangeInserted(startPosition, insertedCount)
                    }
                } else if (data?.data != null) {
                    val imageUri = data.data!!
                    if (selectedImageUris.size < 4) {
                        val startPosition = selectedImageUris.size
                        selectedImageUris.add(imageUri)
                        photoAdapter.notifyItemInserted(startPosition)
                    } else {
                        Toast.makeText(this, getString(R.string.max_photos_allowed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Add photo button (with permission check)
        val addPhotoButton: Button = findViewById(R.id.addPhotoButton)
        addPhotoButton.setOnClickListener {
            checkPermissionAndPickImage()
        }

        registerButton.setOnClickListener {
            if (validateFields()) {
                registerUser()
            }
        }
    }

    private fun checkPermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
            pickImage()
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        activityResultLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult( requestCode: Int,
                                             permissions: Array<out String>,
                                             grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage()
            } else {
                Toast.makeText(
                    this,
                    "Permiso de acceso a la galería denegado.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    // Validation and other methods (no changes needed in these methods for this issue)
    private fun validateFields(): Boolean {
        val name = nameField.text.toString().trim()
        val lastname = lastnameField.text.toString().trim()
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString().trim()
        val name2 = name2Field.text.toString().trim()
        val lastname2 = lastname2Field.text.toString().trim()

        return when {
            name.isEmpty() -> {
                showError(R.id.nameField, R.string.name_required)
                false
            }
            lastname.isEmpty() -> {
                showError(R.id.lastnameField, R.string.lastname_required)
                false
            }
            email.isEmpty() -> {
                showError(R.id.emailField, R.string.email_required)
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError(R.id.emailField, R.string.invalid_email)
                false
            }
            password.isEmpty() -> {
                showError(R.id.passwordField, R.string.password_required)
                false
            }
            password.length < 8 -> {
                showError(R.id.passwordField, R.string.password_min_length)
                false
            }
            !password.any{ it.isDigit() } -> {
                showError(R.id.passwordField, R.string.password_digit_required)
                false
            }
            !password.any { it.isUpperCase() } -> {
                showError(R.id.passwordField, R.string.password_uppercase_required)
                false
            }
            name2.isEmpty() -> {
                showError(R.id.name2Field, R.string.name2_required)
                false
            }
            lastname2.isEmpty() -> {
                showError(R.id.lastname2Field, R.string.lastname2_required)
                false
            }
            selectedImageUris.isEmpty() -> {
                Toast.makeText(this, getString(R.string.photos_required), Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun showError(fieldId: Int, messageResId: Int) {
        val field = findViewById<EditText>(fieldId)
        field.error = getString(messageResId)
        field.requestFocus()
    }

    private fun registerUser() {
        val name = nameField.text.toString().trim()
        val lastname = lastnameField.text.toString().trim()
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString().trim()
        val name2 = name2Field.text.toString().trim()
        val lastname2 = lastname2Field.text.toString().trim()
        val nacionalidad = findViewById<Spinner>(R.id.nacionalidadSpinner).selectedItem.toString()
        val raza = findViewById<Spinner>(R.id.razaSpinner).selectedItem.toString()
        val edad = findViewById<Spinner>(R.id.edadSpinner).selectedItem.toString()
        val personalidad = findViewById<Spinner>(R.id.personalidadSpinner).selectedItem.toString()
        val nacionalidad2 = findViewById<Spinner>(R.id.nacionalidad2Spinner).selectedItem.toString()
        val raza2 = findViewById<Spinner>(R.id.raza2Spinner).selectedItem.toString()
        val edad2 = findViewById<Spinner>(R.id.edad2Spinner).selectedItem.toString()
        val personalidad2 = findViewById<Spinner>(R.id.personalidad2Spinner).selectedItem.toString()

        // Disable button and show progress bar
        registerButton.isEnabled = false
        progressBar.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { registrationTask ->
                if (registrationTask.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user!!.uid

                    uploadImages(userId) { imageUrls ->
                        if (imageUrls.isNotEmpty()) {
                            val userData = hashMapOf(
                                "person1" to hashMapOf(
                                    "name" to name,
                                    "lastname" to lastname,
                                    "nacionalidad" to nacionalidad,
                                    "raza" to raza,
                                    "edad" to edad,
                                    "personalidad" to personalidad
                                ),
                                "person2" to hashMapOf(
                                    "name" to name2,
                                    "lastname" to lastname2,
                                    "nacionalidad" to nacionalidad2,
                                    "raza" to raza2,
                                    "edad" to edad2,
                                    "personalidad" to personalidad2
                                ),
                                "profilePhotos" to imageUrls,
                                "email" to email
                            )

                            db.collection("profiles").document(userId)
                                .set(userData)
                                .addOnSuccessListener {
                                    // Enable button and hide progress bar
                                    registerButton.isEnabled = true
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(this, getString(R.string.registration_successful), Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, CreateProfileActivity::class.java)) // Consider changing to the next screen after registration.
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    handleRegistrationError(e, getString(R.string.error_saving_data, e.localizedMessage))
                                }
                        } else {
                            handleRegistrationError(null, getString(R.string.error_uploading_photos))
                        }
                    }
                } else {
                    handleRegistrationError(registrationTask.exception, getString(R.string.registration_error, registrationTask.exception?.localizedMessage))
                }
            }
    }

    private fun uploadImages(userId: String, onComplete: (List<String>) -> Unit) {
        if (selectedImageUris.isEmpty()) {
            onComplete(emptyList())
            return
        }

        val storageRef = storage.reference.child("profile_images/$userId")
        val imageUrls = mutableListOf<String>()
        var uploadsCompleted = 0

        for (uri in selectedImageUris) {
            val imageRef = storageRef.child("${UUID.randomUUID()}.jpg")
            imageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        imageUrls.add(downloadUri.toString())
                        uploadsCompleted++
                        if (uploadsCompleted == selectedImageUris.size) {
                            onComplete(imageUrls)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("RegisterActivity", "Error uploading image", e)
                    uploadsCompleted++
                    if (uploadsCompleted == selectedImageUris.size) {
                        onComplete(emptyList()) // Return empty list to indicate failure
                    }
                }
        }
    }

    private fun handleRegistrationError(exception: Exception?, message: String) {
        // Enable button and hide progress bar
        registerButton.isEnabled = true
        progressBar.visibility = View.GONE

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        exception?.let { Log.e("RegisterActivity", "Error during registration", it) }

        // Attempt to delete the user account if registration or upload fails:
        auth.currentUser?.delete()?.addOnCompleteListener { deletionTask ->
            if (deletionTask.isSuccessful) {
                Log.d("RegisterActivity", "User account deleted after failure.")
            } else {
                Log.e("RegisterActivity", "Failed to delete user account after failure.", deletionTask.exception)
                // Consider logging this to your error reporting system, as it indicates a potential issue with Firebase.
            }
        }
    }


    private fun setupSpinners(spinner: Spinner, arrayResId: Int) {
        ArrayAdapter.createFromResource(
            this,
            arrayResId,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }
}