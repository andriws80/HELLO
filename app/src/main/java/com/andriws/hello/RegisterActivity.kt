package com.andriws.hello

import android.Manifest
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
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch



class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var nameField: EditText
    private lateinit var lastnameField: EditText
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var name2Field: EditText
    private lateinit var lastname2Field: EditText
    private lateinit var registerButton: Button
    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private val selectedImageUris: MutableList<Uri> = mutableListOf()
    private var authMethod: String = ""

    // CheckBox variables for Nivel Academico (Person 1)
    private lateinit var primariaCheckBox: CheckBox
    private lateinit var secundariaCheckBox: CheckBox
    private lateinit var tecnicoCheckBox: CheckBox
    private lateinit var tecnologoCheckBox: CheckBox
    private lateinit var universitarioCheckBox: CheckBox
    private lateinit var posgradoCheckBox: CheckBox
    private lateinit var doctoradoCheckBox: CheckBox
    private lateinit var posdoctoradoCheckBox: CheckBox

    // CheckBox variables for Nivel Academico (Person 2)
    private lateinit var primaria2CheckBox: CheckBox
    private lateinit var secundaria2CheckBox: CheckBox
    private lateinit var tecnico2CheckBox: CheckBox
    private lateinit var tecnologo2CheckBox: CheckBox
    private lateinit var universitario2CheckBox: CheckBox
    private lateinit var posgrado2CheckBox: CheckBox
    private lateinit var doctorado2CheckBox: CheckBox
    private lateinit var posdoctorado2CheckBox: CheckBox

    // CheckBox variables for Personalidad (Person 1)
    private lateinit var extrovertidoCheckBox: CheckBox
    private lateinit var introvertidoCheckBox: CheckBox
    private lateinit var aventureroCheckBox: CheckBox
    private lateinit var creativoCheckBox: CheckBox
    private lateinit var analiticoCheckBox: CheckBox
    private lateinit var emocionalCheckBox: CheckBox
    private lateinit var racionalCheckBox: CheckBox
    private lateinit var perfeccionistaCheckBox: CheckBox
    private lateinit var sonadorCheckBox: CheckBox
    private lateinit var pragmaticoCheckBox: CheckBox
    private lateinit var carismaticoCheckBox: CheckBox
    private lateinit var reflexivoCheckBox: CheckBox

    // CheckBox variables for Personalidad (Person 2)
    private lateinit var extrovertido2CheckBox: CheckBox
    private lateinit var introvertido2CheckBox: CheckBox
    private lateinit var aventurero2CheckBox: CheckBox
    private lateinit var creativo2CheckBox: CheckBox
    private lateinit var analitico2CheckBox: CheckBox
    private lateinit var emocional2CheckBox: CheckBox
    private lateinit var racional2CheckBox: CheckBox
    private lateinit var perfeccionista2CheckBox: CheckBox
    private lateinit var sonador2CheckBox: CheckBox
    private lateinit var pragmatico2CheckBox: CheckBox
    private lateinit var carismatico2CheckBox: CheckBox
    private lateinit var reflexivo2CheckBox: CheckBox

    private val credentialManager = CredentialManager.create(this)

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
        name2Field = findViewById(R.id.name2Field)
        lastname2Field = findViewById(R.id.lastname2Field)
        emailField = findViewById(R.id.emailField)
        passwordField = findViewById(R.id.passwordField)
        registerButton = findViewById(R.id.registerButton)
        photoRecyclerView = findViewById(R.id.photoRecyclerView)
        progressBar = findViewById(R.id.progressBar)

        val nacionalidadSpinner = findViewById<Spinner>(R.id.nacionalidadSpinner)
        val razaSpinner = findViewById<Spinner>(R.id.razaSpinner)
        val edadSpinner = findViewById<Spinner>(R.id.edadSpinner)
        val nacionalidad2Spinner = findViewById<Spinner>(R.id.nacionalidad2Spinner)
        val raza2Spinner = findViewById<Spinner>(R.id.raza2Spinner)
        val edad2Spinner = findViewById<Spinner>(R.id.edad2Spinner)

        // Get authentication method from Intent
        val authMethodExtra: String? = intent.getStringExtra("auth_method")
        authMethod = authMethodExtra ?: "email"

        if (authMethod == "google") {
            // Disable and clear email and password fields for Google auth
            emailField.isEnabled = false
            emailField.setText("")
            passwordField.visibility = View.GONE
        }

        registerButton.setOnClickListener {
            registerButton.isEnabled = false
            progressBar.visibility = View.VISIBLE
            val emailText = if (authMethod == "google") "" else emailField.text.toString().trim()
            if (validateFields(authMethod, emailText)) {
                if (authMethod == "google") {
                    signInWithGoogle()  // Llama a la nueva función
                } else {
                    registerUser(authMethod)
                }
            } else {
                Toast.makeText(
                    this,
                    "Por favor, complete todos los campos correctamente.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Configure Spinners
        setupSpinners(nacionalidadSpinner, R.array.nacionalidades)
        setupSpinners(razaSpinner, R.array.razas)
        setupSpinners(edadSpinner, R.array.edades)
        setupSpinners(nacionalidad2Spinner, R.array.nacionalidades)
        setupSpinners(raza2Spinner, R.array.razas)
        setupSpinners(edad2Spinner, R.array.edades)

        // Configure RecyclerView for photos
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
                when {
                    data?.clipData != null -> {
                        val clipData = data.clipData
                        val count = clipData!!.itemCount
                        val startPosition = selectedImageUris.size
                        for (i in 0 until count) {
                            val imageUri = clipData.getItemAt(i).uri
                            if (selectedImageUris.size < 4) {
                                selectedImageUris.add(imageUri)
                            } else {
                                Toast.makeText(
                                    this,
                                    getString(R.string.max_photos_allowed),
                                    Toast.LENGTH_SHORT
                                ).show()
                                break
                            }
                        }
                        val insertedCount = selectedImageUris.size - startPosition
                        if (insertedCount > 0) {
                            photoAdapter.notifyItemRangeInserted(startPosition, insertedCount)
                        }
                    }

                    data?.data != null -> {
                        val imageUri = data.data!!
                        if (selectedImageUris.size < 4) {
                            val startPosition = selectedImageUris.size
                            selectedImageUris.add(imageUri)
                            photoAdapter.notifyItemInserted(startPosition)
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.max_photos_allowed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        // Add photo button
        val addPhotoButton: Button = findViewById(R.id.addPhotoButton)
        addPhotoButton.setOnClickListener {
            checkPermissionAndPickImage()
        }

        // Initialize Nivel Academico CheckBoxes for Person 1
        primariaCheckBox = findViewById(R.id.primariaCheckBox)
        secundariaCheckBox = findViewById(R.id.secundariaCheckBox)
        tecnicoCheckBox = findViewById(R.id.tecnicoCheckBox)
        tecnologoCheckBox = findViewById(R.id.tecnologoCheckBox)
        universitarioCheckBox = findViewById(R.id.universitarioCheckBox)
        posgradoCheckBox = findViewById(R.id.posgradoCheckBox)
        doctoradoCheckBox = findViewById(R.id.doctoradoCheckBox)
        posdoctoradoCheckBox = findViewById(R.id.posdoctoradoCheckBox)

        // Initialize Nivel Academico CheckBoxes for Person 2
        primaria2CheckBox = findViewById(R.id.primaria2CheckBox)
        secundaria2CheckBox = findViewById(R.id.secundaria2CheckBox)
        tecnico2CheckBox = findViewById(R.id.tecnico2CheckBox)
        tecnologo2CheckBox = findViewById(R.id.tecnologo2CheckBox)
        universitario2CheckBox = findViewById(R.id.universitario2CheckBox)
        posgrado2CheckBox = findViewById(R.id.posgrado2CheckBox)
        doctorado2CheckBox = findViewById(R.id.doctorado2CheckBox)
        posdoctorado2CheckBox = findViewById(R.id.posdoctorado2CheckBox)

        // Initialize Personalidad CheckBoxes for Person 1
        extrovertidoCheckBox = findViewById(R.id.extrovertidoCheckBox)
        introvertidoCheckBox = findViewById(R.id.introvertidoCheckBox)
        aventureroCheckBox = findViewById(R.id.aventureroCheckBox)
        creativoCheckBox = findViewById(R.id.creativoCheckBox)
        analiticoCheckBox = findViewById(R.id.analiticoCheckBox)
        emocionalCheckBox = findViewById(R.id.emocionalCheckBox)
        racionalCheckBox = findViewById(R.id.racionalCheckBox)
        perfeccionistaCheckBox = findViewById(R.id.perfeccionistaCheckBox)
        sonadorCheckBox = findViewById(R.id.sonadorCheckBox)
        pragmaticoCheckBox = findViewById(R.id.pragmaticoCheckBox)
        carismaticoCheckBox = findViewById(R.id.carismaticoCheckBox)
        reflexivoCheckBox = findViewById(R.id.reflexivoCheckBox)

        // Initialize Personalidad CheckBoxes for Person 2
        extrovertido2CheckBox = findViewById(R.id.extrovertido2CheckBox)
        introvertido2CheckBox = findViewById(R.id.introvertido2CheckBox)
        aventurero2CheckBox = findViewById(R.id.aventurero2CheckBox)
        creativo2CheckBox = findViewById(R.id.creativo2CheckBox)
        analitico2CheckBox = findViewById(R.id.analitico2CheckBox)
        emocional2CheckBox = findViewById(R.id.emocional2CheckBox)
        racional2CheckBox = findViewById(R.id.racional2CheckBox)
        perfeccionista2CheckBox = findViewById(R.id.perfeccionista2CheckBox)
        sonador2CheckBox = findViewById(R.id.sonador2CheckBox)
        pragmatico2CheckBox = findViewById(R.id.pragmatico2CheckBox)
        carismatico2CheckBox = findViewById(R.id.carismatico2CheckBox)
        reflexivo2CheckBox = findViewById(R.id.reflexivo2CheckBox)

    }
    private fun signInWithGoogle() {
        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    this@RegisterActivity, // Pasa el Context de la Activity
                    GetCredentialRequest(
                        listOf(
                            GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(getString(R.string.default_web_client_id))
                                .build()
                        )
                    )
                )
                handleSignInResult(result)
            } catch (e: GetCredentialException) {
                handleSignInError(e)
            }
        }
    }
    private fun handleSignInResult(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is GoogleIdTokenCredential) {
            val idToken = credential.idToken
            firebaseAuthWithGoogle(idToken)
        } else {
            handleRegistrationError(null, "Error: Credencial inesperada.")
        }
    }

    private fun handleSignInError(e: GetCredentialException) {
        Log.e("RegisterActivity", "Error al obtener credenciales: ${e.message}", e)
        val errorMessage = when (e) {
            is androidx.credentials.exceptions.NoCredentialException -> {
                "No hay credenciales disponibles. Verifica tu cuenta de Google en el dispositivo o intenta añadir una."
            }
            is androidx.credentials.exceptions.GetCredentialCancellationException -> {
                "Inicio de sesión cancelado."
            }
            else -> "Error desconocido al iniciar sesión con Google: ${e.message}"
        }
        handleRegistrationError(e, errorMessage)
    }





    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                handleFirebaseAuthResult(
                    task,
                    "google",
                    nameField.text.toString().trim(),
                    lastnameField.text.toString().trim(),
                    findViewById<Spinner>(R.id.nacionalidadSpinner).selectedItem.toString(),
                    findViewById<Spinner>(R.id.razaSpinner).selectedItem.toString(),
                    findViewById<Spinner>(R.id.edadSpinner).selectedItem.toString(),
                    getNivelAcademico(1),
                    getPersonalidad(1),
                    name2Field.text.toString().trim(),
                    lastname2Field.text.toString().trim(),
                    findViewById<Spinner>(R.id.nacionalidad2Spinner).selectedItem.toString(),
                    findViewById<Spinner>(R.id.raza2Spinner).selectedItem.toString(),
                    findViewById<Spinner>(R.id.edad2Spinner).selectedItem.toString(),
                    getNivelAcademico(2),
                    getPersonalidad(2),
                    null
                )
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
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

    private fun validateFields(authMethod: String, email: String): Boolean {
        val name = nameField.text.toString().trim()
        val lastname = lastnameField.text.toString().trim()
        val password = passwordField.text.toString().trim()
        val name2 = name2Field.text.toString().trim()
        val lastname2 = lastname2Field.text.toString().trim()

        // Regular expression to allow letters, spaces, numbers, and some common symbols
        val nameRegex = Regex("^[a-zA-Z0-9áéíóúüñÁÉÍÓÚÜÑ\\s.,'-]+$")

        // Spinner selections and their respective arrays
        val nacionalidadSpinner = findViewById<Spinner>(R.id.nacionalidadSpinner)
        val nacionalidad = nacionalidadSpinner.selectedItem.toString()
        val nacionalidadesArray = resources.getStringArray(R.array.nacionalidades)

        val razaSpinner = findViewById<Spinner>(R.id.razaSpinner)
        val raza = razaSpinner.selectedItem.toString()
        val razasArray = resources.getStringArray(R.array.razas)

        val edadSpinner = findViewById<Spinner>(R.id.edadSpinner)
        val edad = edadSpinner.selectedItem.toString()
        val edadesArray = resources.getStringArray(R.array.edades)

       // Spinner selections for Person 2 (using the same arrays as Person 1)

        val nacionalidad2Spinner = findViewById<Spinner>(R.id.nacionalidad2Spinner)
        val nacionalidad2 = nacionalidad2Spinner.selectedItem.toString()

        val raza2Spinner = findViewById<Spinner>(R.id.raza2Spinner)
        val raza2 = raza2Spinner.selectedItem.toString()

        val edad2Spinner = findViewById<Spinner>(R.id.edad2Spinner)
        val edad2 = edad2Spinner.selectedItem.toString()

        // CheckBox selections for Person 1
        val nivelAcademicoPerson1 = mutableListOf<String>()
        if (primariaCheckBox.isChecked) nivelAcademicoPerson1.add("Primaria")
        if (secundariaCheckBox.isChecked) nivelAcademicoPerson1.add("Secundaria")
        if (tecnicoCheckBox.isChecked) nivelAcademicoPerson1.add("Técnico")
        if (tecnologoCheckBox.isChecked) nivelAcademicoPerson1.add("Tecnólogo")
        if (universitarioCheckBox.isChecked) nivelAcademicoPerson1.add("Universitario")
        if (posgradoCheckBox.isChecked) nivelAcademicoPerson1.add("Posgrado")
        if (doctoradoCheckBox.isChecked) nivelAcademicoPerson1.add("Doctorado")
        if (posdoctoradoCheckBox.isChecked) nivelAcademicoPerson1.add("Posdoctorado")

        val personalidadPerson1 = mutableListOf<String>()
        if (extrovertidoCheckBox.isChecked) personalidadPerson1.add("Extrovertido")
        if (introvertidoCheckBox.isChecked) personalidadPerson1.add("Introvertido")
        if (aventureroCheckBox.isChecked) personalidadPerson1.add("Aventurero")
        if (creativoCheckBox.isChecked) personalidadPerson1.add("Creativo")
        if (analiticoCheckBox.isChecked) personalidadPerson1.add("Analítico")
        if (emocionalCheckBox.isChecked) personalidadPerson1.add("Emocional")
        if (racionalCheckBox.isChecked) personalidadPerson1.add("Racional")
        if (perfeccionistaCheckBox.isChecked) personalidadPerson1.add("Perfeccionista")
        if (sonadorCheckBox.isChecked) personalidadPerson1.add("Soñador")
        if (pragmaticoCheckBox.isChecked) personalidadPerson1.add("Pragmático")
        if (carismaticoCheckBox.isChecked) personalidadPerson1.add("Carismático")
        if (reflexivoCheckBox.isChecked) personalidadPerson1.add("Reflexivo")

        // CheckBox selections for Person 2
        val nivelAcademicoPerson2 = mutableListOf<String>()
        if (primaria2CheckBox.isChecked) nivelAcademicoPerson2.add("Primaria")
        if (secundaria2CheckBox.isChecked) nivelAcademicoPerson2.add("Secundaria")
        if (tecnico2CheckBox.isChecked) nivelAcademicoPerson2.add("Técnico")
        if (tecnologo2CheckBox.isChecked) nivelAcademicoPerson2.add("Tecnólogo")
        if (universitario2CheckBox.isChecked) nivelAcademicoPerson2.add("Universitario")
        if (posgrado2CheckBox.isChecked) nivelAcademicoPerson2.add("Posgrado")
        if (doctorado2CheckBox.isChecked) nivelAcademicoPerson2.add("Doctorado")
        if (posdoctorado2CheckBox.isChecked) nivelAcademicoPerson2.add("Posdoctorado")

        val personalidadPerson2 = mutableListOf<String>()
        if (extrovertido2CheckBox.isChecked) personalidadPerson2.add("Extrovertido")
        if (introvertido2CheckBox.isChecked) personalidadPerson2.add("Introvertido")
        if (aventurero2CheckBox.isChecked) personalidadPerson2.add("Aventurero")
        if (creativo2CheckBox.isChecked) personalidadPerson2.add("Creativo")
        if (analitico2CheckBox.isChecked) personalidadPerson2.add("Analítico")
        if (emocional2CheckBox.isChecked) personalidadPerson2.add("Emocional")
        if (racional2CheckBox.isChecked) personalidadPerson2.add("Racional")
        if (perfeccionista2CheckBox.isChecked) personalidadPerson2.add("Perfeccionista")
        if (sonador2CheckBox.isChecked) personalidadPerson2.add("Soñador")
        if (pragmatico2CheckBox.isChecked) personalidadPerson2.add("Pragmático")
        if (carismatico2CheckBox.isChecked) personalidadPerson2.add("Carismático")
        if (reflexivo2CheckBox.isChecked) personalidadPerson2.add("Reflexivo")

        return when (authMethod) {
            "google" -> {
                name.isNotBlank() && name.matches(nameRegex) &&
                        lastname.isNotBlank() && lastname.matches(nameRegex) &&
                        nacionalidad in nacionalidadesArray &&
                        raza in razasArray &&
                        edad in edadesArray &&
                        name2.isNotBlank() && name2.matches(nameRegex) &&
                        lastname2.isNotBlank() && lastname2.matches(nameRegex) &&
                        nacionalidad2 in nacionalidadesArray &&
                        raza2 in razasArray &&
                        edad2 in edadesArray &&
                        nivelAcademicoPerson1.isNotEmpty() &&
                        personalidadPerson1.isNotEmpty() &&
                        nivelAcademicoPerson2.isNotEmpty() &&
                        personalidadPerson2.isNotEmpty()
            }

            else -> { // "email"
                name.isNotBlank() && name.matches(nameRegex) &&
                        lastname.isNotBlank() && lastname.matches(nameRegex) &&
                        email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                        password.isNotBlank() && password.length >= 6 &&
                        nacionalidad in nacionalidadesArray &&
                        raza in razasArray &&
                        edad in edadesArray &&
                        name2.isNotBlank() && name2.matches(nameRegex) &&
                        lastname2.isNotBlank() && lastname2.matches(nameRegex) &&
                        nacionalidad2 in nacionalidadesArray &&
                        raza2 in razasArray &&
                        edad2 in edadesArray &&
                        nivelAcademicoPerson1.isNotEmpty() &&
                        personalidadPerson1.isNotEmpty() &&
                        nivelAcademicoPerson2.isNotEmpty() &&
                        personalidadPerson2.isNotEmpty()
            }
        }
    }


    private fun registerUser(authMethod: String) {
        progressBar.visibility = View.VISIBLE

        val name = nameField.text.toString().trim()
        val lastname = lastnameField.text.toString().trim()
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString().trim()
        val nacionalidad = findViewById<Spinner>(R.id.nacionalidadSpinner).selectedItem.toString()
        val raza = findViewById<Spinner>(R.id.razaSpinner).selectedItem.toString()
        val edad = findViewById<Spinner>(R.id.edadSpinner).selectedItem.toString()
        val nivelAcademicoPerson1 = getNivelAcademico(1)
        val personalidadPerson1 = getPersonalidad(1)

        val name2 = name2Field.text.toString().trim()
        val lastname2 = lastname2Field.text.toString().trim()
        val nacionalidad2 = findViewById<Spinner>(R.id.nacionalidad2Spinner).selectedItem.toString()
        val raza2 = findViewById<Spinner>(R.id.raza2Spinner).selectedItem.toString()
        val edad2 = findViewById<Spinner>(R.id.edad2Spinner).selectedItem.toString()
        val nivelAcademicoPerson2 = getNivelAcademico(2)
        val personalidadPerson2 = getPersonalidad(2)

        if (authMethod == "email") {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    handleFirebaseAuthResult(
                        task,
                        authMethod,
                        name,
                        lastname,
                        nacionalidad,
                        raza,
                        edad,
                        nivelAcademicoPerson1,
                        personalidadPerson1,
                        name2,
                        lastname2,
                        nacionalidad2,
                        raza2,
                        edad2,
                        nivelAcademicoPerson2,
                        personalidadPerson2,
                        email
                    )
                }
        }
    }

    private fun handleFirebaseAuthResult(
        task: Task<AuthResult>,
        authMethod: String,
        name: String,
        lastname: String,
        nacionalidad: String,
        raza: String,
        edad: String,
        nivelAcademicoPerson1: List<String>,
        personalidadPerson1: List<String>,
        name2: String,
        lastname2: String,
        nacionalidad2: String,
        raza2: String,
        edad2: String,
        nivelAcademicoPerson2: List<String>,
        personalidadPerson2: List<String>,
        email: String?
    ) {
        if (task.isSuccessful) {
            val user = auth.currentUser
            if (user != null) {
                // If email is null, use the user's email from Firebase
                val userEmail = email ?: user.email ?: ""
                uploadImagesAndSaveData(
                    user.uid,
                    authMethod,
                    name,
                    lastname,
                    userEmail,
                    nacionalidad,
                    raza,
                    edad,
                    nivelAcademicoPerson1,
                    personalidadPerson1,
                    name2,
                    lastname2,
                    nacionalidad2,
                    raza2,
                    edad2,
                    nivelAcademicoPerson2,
                    personalidadPerson2
                )
            } else {
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this,
                    "Error: No se pudo obtener el usuario autenticado.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            progressBar.visibility = View.GONE
            val errorMessage = if (task.exception is FirebaseAuthUserCollisionException) {
                "Ya existe una cuenta con este correo electrónico."
            } else {
                "Error en el registro: ${task.exception?.message}"
            }
            handleRegistrationError(task.exception, errorMessage)
        }
    }


    private fun uploadImagesAndSaveData(
        userId: String,
        authMethod: String,
        name: String,
        lastname: String,
        email: String,
        nacionalidad: String,
        raza: String,
        edad: String,
        nivelAcademicoPerson1: List<String>,
        personalidadPerson1: List<String>,
        name2: String,
        lastname2: String,
        nacionalidad2: String,
        raza2: String,
        edad2: String,
        nivelAcademicoPerson2: List<String>,
        personalidadPerson2: List<String>
    ) {
        if (selectedImageUris.isNotEmpty()) {
            val storageRef = storage.reference.child("users/$userId/photos")
            val uploadedImageUrls = mutableListOf<String>()
            var uploadCount = 0

            for (uri in selectedImageUris) {
                val imageRef = storageRef.child("${System.currentTimeMillis()}_${uploadCount}.jpg")
                imageRef.putFile(uri)
                    .addOnSuccessListener { taskSnapshot ->
                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            uploadedImageUrls.add(downloadUri.toString())
                            uploadCount++
                            if (uploadCount == selectedImageUris.size) {
                                // All images uploaded, now save user data
                                saveUserData(
                                    userId,
                                    authMethod,
                                    name,
                                    lastname,
                                    email,
                                    nacionalidad,
                                    raza,
                                    edad,
                                    nivelAcademicoPerson1,
                                    personalidadPerson1,
                                    name2,
                                    lastname2,
                                    nacionalidad2,
                                    raza2,
                                    edad2,
                                    nivelAcademicoPerson2,
                                    personalidadPerson2,
                                    uploadedImageUrls
                                )
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this,
                            "Error de red al subir la imagen: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        } else {
            // No images selected, save user data directly
            saveUserData(
                userId,
                authMethod,
                name,
                lastname,
                email,
                nacionalidad,
                raza,
                edad,
                nivelAcademicoPerson1,
                personalidadPerson1,
                name2,
                lastname2,
                nacionalidad2,
                raza2,
                edad2,
                nivelAcademicoPerson2,
                personalidadPerson2,
                emptyList()
            )
        }
    }


    private fun saveUserData(
        userId: String,
        authMethod: String,
        name: String,
        lastname: String,
        email: String,
        nacionalidad: String,
        raza: String,
        edad: String,
        nivelAcademicoPerson1: List<String>,
        personalidadPerson1: List<String>,
        name2: String,
        lastname2: String,
        nacionalidad2: String,
        raza2: String,
        edad2: String,
        nivelAcademicoPerson2: List<String>,
        personalidadPerson2: List<String>,
        imageUrls: List<String>
    ) {
        val userMap = hashMapOf(
            "authMethod" to authMethod,
            "name" to name,
            "lastname" to lastname,
            "email" to email,
            "nacionalidad" to nacionalidad,
            "raza" to raza,
            "edad" to edad,
            "nivelAcademicoPerson1" to nivelAcademicoPerson1,
            "personalidadPerson1" to personalidadPerson1,
            "name2" to name2,
            "lastname2" to lastname2,
            "nacionalidad2" to nacionalidad2,
            "raza2" to raza2,
            "edad2" to edad2,
            "nivelAcademicoPerson2" to nivelAcademicoPerson2,
            "personalidadPerson2" to personalidadPerson2,
            "imageUrls" to imageUrls
        )

        db.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this,
                    "Registro exitoso!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()  // Close the registration activity
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this,
                    "Error al guardar datos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
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

    private fun getNivelAcademico(person: Int): List<String> {
        val nivelAcademico = mutableListOf<String>()
        if (person == 1) {
            if (primariaCheckBox.isChecked) nivelAcademico.add("Primaria")
            if (secundariaCheckBox.isChecked) nivelAcademico.add("Secundaria")
            if (tecnicoCheckBox.isChecked) nivelAcademico.add("Técnico")
            if (tecnologoCheckBox.isChecked) nivelAcademico.add("Tecnólogo")
            if (universitarioCheckBox.isChecked) nivelAcademico.add("Universitario")
            if (posgradoCheckBox.isChecked) nivelAcademico.add("Posgrado")
            if (doctoradoCheckBox.isChecked) nivelAcademico.add("Doctorado")
            if (posdoctoradoCheckBox.isChecked) nivelAcademico.add("Posdoctorado")
        } else { // person == 2
            if (primaria2CheckBox.isChecked) nivelAcademico.add("Primaria")
            if (secundaria2CheckBox.isChecked) nivelAcademico.add("Secundaria")
            if (tecnico2CheckBox.isChecked) nivelAcademico.add("Técnico")
            if (tecnologo2CheckBox.isChecked) nivelAcademico.add("Tecnólogo")
            if (universitario2CheckBox.isChecked) nivelAcademico.add("Universitario")
            if (posgrado2CheckBox.isChecked) nivelAcademico.add("Posgrado")
            if (doctorado2CheckBox.isChecked) nivelAcademico.add("Doctorado")
            if (posdoctorado2CheckBox.isChecked) nivelAcademico.add("Posdoctorado")
        }
        return nivelAcademico
    }

    private fun getPersonalidad(person: Int): List<String> {
        val personalidad = mutableListOf<String>()
        if (person == 1) {
            if (extrovertidoCheckBox.isChecked) personalidad.add("Extrovertido")
            if (introvertidoCheckBox.isChecked) personalidad.add("Introvertido")
            if (aventureroCheckBox.isChecked) personalidad.add("Aventurero")
            if (creativoCheckBox.isChecked) personalidad.add("Creativo")
            if (analiticoCheckBox.isChecked) personalidad.add("Analítico")
            if (emocionalCheckBox.isChecked) personalidad.add("Emocional")
            if (racionalCheckBox.isChecked) personalidad.add("Racional")
            if (perfeccionistaCheckBox.isChecked) personalidad.add("Perfeccionista")
            if (sonadorCheckBox.isChecked) personalidad.add("Soñador")
            if (pragmaticoCheckBox.isChecked) personalidad.add("Pragmático")
            if (carismaticoCheckBox.isChecked) personalidad.add("Carismático")
            if (reflexivoCheckBox.isChecked) personalidad.add("Reflexivo")
        } else { // person == 2
            if (extrovertido2CheckBox.isChecked) personalidad.add("Extrovertido")
            if (introvertido2CheckBox.isChecked) personalidad.add("Introvertido")
            if (aventurero2CheckBox.isChecked) personalidad.add("Aventurero")
            if (creativo2CheckBox.isChecked) personalidad.add("Creativo")
            if (analitico2CheckBox.isChecked) personalidad.add("Analítico")
            if (emocional2CheckBox.isChecked) personalidad.add("Emocional")
            if (racional2CheckBox.isChecked) personalidad.add("Racional")
            if (perfeccionista2CheckBox.isChecked) personalidad.add("Perfeccionista")
            if (sonador2CheckBox.isChecked) personalidad.add("Soñador")
            if (pragmatico2CheckBox.isChecked) personalidad.add("Pragmático")
            if (carismatico2CheckBox.isChecked) personalidad.add("Carismático")
            if (reflexivo2CheckBox.isChecked) personalidad.add("Reflexivo")
        }
        return personalidad
    }


    private fun handleRegistrationError(exception: Exception?, message: String) {
        Log.e("RegisterActivity", "Registration Error: $message", exception)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}