package com.andriws.hello

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
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
//import kotlin.compareTo
//import kotlin.text.contains
import kotlin.text.matches
//import kotlin.text.set
//import kotlin.toString
import androidx.core.view.isGone
//import androidx.core.view.isVisible



class RegisterActivity : AppCompatActivity() {

    // Instancias de Firebase
    private lateinit var auth: FirebaseAuth // Para la autenticación de usuarios (Correo/Contraseña y Google)
    private lateinit var db: FirebaseFirestore // Para almacenar los datos del usuario (información de perfil)
    private lateinit var storage: FirebaseStorage // Para almacenar las imágenes subidas por el usuario

    // Elementos de la interfaz de usuario para la entrada de datos (Persona 1)
    private lateinit var nameField: EditText // Campo de entrada para el nombre de la persona 1
    private lateinit var lastnameField: EditText // Campo de entrada para el apellido de la persona 1
    private lateinit var emailField: EditText // Campo de entrada para el correo electrónico del usuario
    private lateinit var passwordField: EditText // Campo de entrada para la contraseña del usuario

    // Elementos de la interfaz de usuario para la entrada de datos (Persona 2)
    private lateinit var name2Field: EditText // Campo de entrada para el nombre de la persona 2
    private lateinit var lastname2Field: EditText // Campo de entrada para el apellido de la persona 2

    // Elementos principales de la interfaz de usuario y componentes
    private lateinit var registerButton: Button // Botón para iniciar el proceso de registro
    private lateinit var photoRecyclerView: RecyclerView // RecyclerView para mostrar las fotos seleccionadas
    private lateinit var progressBar: ProgressBar // Barra de progreso para mostrar estados de carga
    private lateinit var photoAdapter: PhotoAdapter // Adaptador para el photoRecyclerView
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent> // Lanzador para iniciar una actividad por resultado (ej. seleccionar imágenes)

    //CheckBox variables for Nivel Academico (Person 1)
    private lateinit var primariaCheckBox: CheckBox // Checkbox para el nivel academico "primaria"
    private lateinit var secundariaCheckBox: CheckBox // Checkbox para el nivel academico "secundaria"
    private lateinit var tecnicoCheckBox: CheckBox // Checkbox para el nivel academico "tecnico"
    private lateinit var tecnologoCheckBox: CheckBox // Checkbox para el nivel academico "tecnologo"
    private lateinit var universitarioCheckBox: CheckBox // Checkbox para el nivel academico "universitario"
    private lateinit var posgradoCheckBox: CheckBox // Checkbox para el nivel academico "posgrado"
    private lateinit var doctoradoCheckBox: CheckBox // Checkbox para el nivel academico "doctorado"
    private lateinit var posdoctoradoCheckBox: CheckBox // Checkbox para el nivel academico "posdoctorado"

    // CheckBox variables for Nivel Academico (Person 2)
    private lateinit var primaria2CheckBox: CheckBox // Checkbox para el nivel academico "primaria"
    private lateinit var secundaria2CheckBox: CheckBox // Checkbox para el nivel academico "secundaria"
    private lateinit var tecnico2CheckBox: CheckBox // Checkbox para el nivel academico "tecnico"
    private lateinit var tecnologo2CheckBox: CheckBox // Checkbox para el nivel academico "tecnologo"
    private lateinit var universitario2CheckBox: CheckBox // Checkbox para el nivel academico "universitario"
    private lateinit var posgrado2CheckBox: CheckBox // Checkbox para el nivel academico "posgrado"
    private lateinit var doctorado2CheckBox: CheckBox // Checkbox para el nivel academico "doctorado"
    private lateinit var posdoctorado2CheckBox: CheckBox // Checkbox para el nivel academico "posdoctorado"

    // CheckBox variables for Personalidad (Person 1)
    private lateinit var extrovertidoCheckBox: CheckBox // Checkbox para la personalidad "extrovertido"
    private lateinit var introvertidoCheckBox: CheckBox // Checkbox para la personalidad "introvertido"
    private lateinit var aventureroCheckBox: CheckBox // Checkbox para la personalidad "aventurero"
    private lateinit var creativoCheckBox: CheckBox // Checkbox para la personalidad "creativo"
    private lateinit var analiticoCheckBox: CheckBox // Checkbox para la personalidad "analitico"
    private lateinit var emocionalCheckBox: CheckBox // Checkbox para la personalidad "emocional"
    private lateinit var racionalCheckBox: CheckBox // Checkbox para la personalidad "racional"
    private lateinit var perfeccionistaCheckBox: CheckBox // Checkbox para la personalidad "perfeccionista"
    private lateinit var sonadorCheckBox: CheckBox // Checkbox para la personalidad "soñador"
    private lateinit var pragmaticoCheckBox: CheckBox // Checkbox para la personalidad "pragmatico"
    private lateinit var carismaticoCheckBox: CheckBox // Checkbox para la personalidad "carismatico"
    private lateinit var reflexivoCheckBox: CheckBox // Checkbox para la personalidad "reflexivo"

    // CheckBox variables for Personalidad (Person 2)
    private lateinit var extrovertido2CheckBox: CheckBox // Checkbox para la personalidad "extrovertido"
    private lateinit var introvertido2CheckBox: CheckBox // Checkbox para la personalidad "introvertido"
    private lateinit var aventurero2CheckBox: CheckBox // Checkbox para la personalidad "aventurero"
    private lateinit var creativo2CheckBox: CheckBox // Checkbox para la personalidad "creativo"
    private lateinit var analitico2CheckBox: CheckBox // Checkbox para la personalidad "analitico"
    private lateinit var emocional2CheckBox: CheckBox // Checkbox para la personalidad "emocional"
    private lateinit var racional2CheckBox: CheckBox // Checkbox para la personalidad "racional"
    private lateinit var perfeccionista2CheckBox: CheckBox // Checkbox para la personalidad "perfeccionista"
    private lateinit var sonador2CheckBox: CheckBox // Checkbox para la personalidad "soñador"
    private lateinit var pragmatico2CheckBox: CheckBox // Checkbox para la personalidad "pragmatico"
    private lateinit var carismatico2CheckBox: CheckBox // Checkbox para la personalidad "carismatico"
    private lateinit var reflexivo2CheckBox: CheckBox // Checkbox para la personalidad "reflexivo"


    // Almacenamiento de datos
    private val selectedImageUris: MutableList<Uri> = mutableListOf() // Lista para almacenar las URIs de las imágenes seleccionadas

    // Otras variables
    private var authMethod: String = "" // Variable para almacenar el método de autenticación seleccionado (correo/google)

    //CredentialManager
    private val credentialManager = CredentialManager.create(this)//para obtener credenciales de google

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123 // Código de solicitud para el permiso de acceso a la galería
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()


        // Inicializar Vistas (EditTexts, Button, ProgressBar, RecyclerView)
        nameField = findViewById(R.id.nameField)
        lastnameField = findViewById(R.id.lastnameField)
        name2Field = findViewById(R.id.name2Field)
        lastname2Field = findViewById(R.id.lastname2Field)
        emailField = findViewById(R.id.emailField)
        passwordField = findViewById(R.id.passwordField)
        registerButton = findViewById(R.id.registerButton)
        photoRecyclerView = findViewById(R.id.photoRecyclerView)
        progressBar = findViewById(R.id.progressBar)

        // Inicializar Boton para agregar fotos
        val addPhotoButton: Button = findViewById(R.id.addPhotoButton)

        // Inicializar Spinners
        val nacionalidadSpinner = findViewById<Spinner>(R.id.nacionalidadSpinner)
        val razaSpinner = findViewById<Spinner>(R.id.razaSpinner)
        val edadSpinner = findViewById<Spinner>(R.id.edadSpinner)
        val nacionalidad2Spinner = findViewById<Spinner>(R.id.nacionalidad2Spinner)
        val raza2Spinner = findViewById<Spinner>(R.id.raza2Spinner)
        val edad2Spinner = findViewById<Spinner>(R.id.edad2Spinner)

        // Inicializar CheckBoxes (Nivel Académico - Persona 1)
        primariaCheckBox = findViewById(R.id.primariaCheckBox)
        secundariaCheckBox = findViewById(R.id.secundariaCheckBox)
        tecnicoCheckBox = findViewById(R.id.tecnicoCheckBox)
        tecnologoCheckBox = findViewById(R.id.tecnologoCheckBox)
        universitarioCheckBox = findViewById(R.id.universitarioCheckBox)
        posgradoCheckBox = findViewById(R.id.posgradoCheckBox)
        doctoradoCheckBox = findViewById(R.id.doctoradoCheckBox)
        posdoctoradoCheckBox = findViewById(R.id.posdoctoradoCheckBox)

        // Inicializar CheckBoxes (Nivel Académico - Persona 2)
        primaria2CheckBox = findViewById(R.id.primaria2CheckBox)
        secundaria2CheckBox = findViewById(R.id.secundaria2CheckBox)
        tecnico2CheckBox = findViewById(R.id.tecnico2CheckBox)
        tecnologo2CheckBox = findViewById(R.id.tecnologo2CheckBox)
        universitario2CheckBox = findViewById(R.id.universitario2CheckBox)
        posgrado2CheckBox = findViewById(R.id.posgrado2CheckBox)
        doctorado2CheckBox = findViewById(R.id.doctorado2CheckBox)
        posdoctorado2CheckBox = findViewById(R.id.posdoctorado2CheckBox)

        // Inicializar CheckBoxes (Personalidad - Persona 1)
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

        // Inicializar CheckBoxes (Personalidad - Persona 2)
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

        // Obtener el método de autenticación del Intent
        val authMethodExtra: String? = intent.getStringExtra("auth_method")
        authMethod = authMethodExtra ?: "email"

        // Manejar campos en el registro por google
        if (authMethod == "google") {
            // Deshabilitar y limpiar los campos de email y contraseña para la autenticación con Google
            emailField.isEnabled = false
            emailField.setText("")
            passwordField.visibility = View.GONE
        }

        // Configurar el listener del botón de registro
        registerButton.setOnClickListener {
            registerButton.isEnabled = false
            progressBar.visibility = View.VISIBLE
            val emailText = if (authMethod == "google") "" else emailField.text.toString().trim()
            if (validateFields(authMethod, emailText)) {
                if (authMethod == "google") {
                    signInWithGoogle()
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
        // Configurar Spinners
        setupSpinners(nacionalidadSpinner, R.array.nacionalidades)
        setupSpinners(razaSpinner, R.array.razas)
        setupSpinners(edadSpinner, R.array.edades)
        setupSpinners(nacionalidad2Spinner, R.array.nacionalidades)
        setupSpinners(raza2Spinner, R.array.razas)
        setupSpinners(edad2Spinner, R.array.edades)

        // Configurar el listener del botón de registro
       registerButton.setOnClickListener {
           registerButton.isEnabled = false
           progressBar.visibility = View.VISIBLE
           val emailText = if (authMethod == "google") "" else emailField.text.toString().trim()
           if (validateFields(authMethod, emailText)) {
               if (authMethod == "google") {
                   signInWithGoogle()
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

        // Configurar RecyclerView para las fotos
        photoAdapter = PhotoAdapter(selectedImageUris) { position ->
            selectedImageUris.removeAt(position)
            photoAdapter.notifyItemRemoved(position)
        }
        photoRecyclerView.layoutManager = GridLayoutManager(this, 2)
        photoRecyclerView.adapter = photoAdapter

        // Configurar ActivityResultLauncher para la selección de imágenes
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

            // Configurar el listener del botón para agregar fotos
            addPhotoButton.setOnClickListener {
                checkPermissionAndPickImage()
            }

            // Configurar TextWatchers para validación en tiempo real
            setupTextWatchers()
        }
    }

    private fun setupTextWatchers() {
        // TextWatcher para el campo de nombre
        nameField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No se necesita implementar nada aquí, a menos que se requiera una acción antes de que cambie el texto
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrBlank()) {
                    nameField.error = "Campo obligatorio"
                } else {
                    nameField.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // No se necesita implementar nada aquí, a menos que se requiera una acción después de que cambie el texto
            }
        })

        // TextWatcher para el campo de apellido
        lastnameField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No se necesita implementar nada aquí, a menos que se requiera una acción antes de que cambie el texto
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrBlank()) {
                    lastnameField.error = "Campo obligatorio"
                } else {
                    lastnameField.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // No se necesita implementar nada aquí, a menos que se requiera una acción después de que cambie el texto
            }
        })
        // TextWatcher para el campo de nombre2
        name2Field.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrBlank()) {
                    name2Field.error = "Campo obligatorio"
                } else {
                    name2Field.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        // TextWatcher para el campo de apellido2
        lastname2Field.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrBlank()) {
                    lastname2Field.error = "Campo obligatorio"
                } else {
                    lastname2Field.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        // TextWatcher para el campo de email (solo si no es registro por google)
        if(authMethod != "google"){
            emailField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.isNullOrBlank()) {
                        emailField.error = "Campo obligatorio"
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(s).matches()){
                       emailField.error = "Correo electrónico no válido"
                  } else {
                        emailField.error = null
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
            // TextWatcher para el campo de password (solo si no es registro por google)
            passwordField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.isNullOrBlank()) {
                        passwordField.error = "Campo obligatorio"
                    }else if (s.length < 6){
                        passwordField.error = "La contraseña debe tener al menos 6 caracteres"
                    } else {
                        passwordField.error = null
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
        }
    }


        private fun signInWithGoogle() {
            lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(
                        this@RegisterActivity, // Pasa el Context de la Activity para obtener las credenciales
                        GetCredentialRequest(
                            listOf(
                                GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false) // Indica que no se filtren las cuentas por autorizacion
                                    .setServerClientId(getString(R.string.default_web_client_id)) // Se establece el ID del cliente web
                                    .build()
                            )
                        )
                    )
                    handleSignInResult(result) // Maneja el resultado del inicio de sesion
                } catch (e: GetCredentialException) {
                    handleSignInError(e) // Maneja el error del inicio de sesion
                }
            }
        }

        private fun handleSignInResult(result: GetCredentialResponse) {
            // Se obtiene la credencial del resultado
            val credential = result.credential
            // Se verifica si la credencial es de tipo GoogleIdTokenCredential
            if (credential is GoogleIdTokenCredential) {
                // Se obtiene el token ID
                val idToken = credential.idToken
                // Se llama a la función para autenticar con Firebase usando el token ID de Google
                firebaseAuthWithGoogle(idToken)
            } else {
                // Se maneja el error si la credencial no es del tipo esperado
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
        // Verificar si el permiso de lectura de almacenamiento externo ha sido concedido
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Si el permiso no ha sido concedido, solicitarlo al usuario
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // Si el permiso ya ha sido concedido, proceder a seleccionar la imagen
            pickImage()
        }
    }

    private fun pickImage() {
        // Crear un Intent para seleccionar una imagen de la galería
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        // Permitir la selección de múltiples imágenes
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        // Iniciar la selección de imágenes con el ActivityResultLauncher
        activityResultLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Verificar si el requestCode coincide con el código de solicitud de permisos
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Verificar si el permiso fue concedido
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el permiso fue concedido, proceder a seleccionar la imagen
                pickImage()
            } else {
                // Si el permiso fue denegado, mostrar un mensaje al usuario
                Toast.makeText(
                    this,
                    "Permiso de acceso a la galería denegado.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun validateFields(authMethod: String, email: String): Boolean {
        // Obtener los valores de los campos de texto y eliminando espacios en blanco al principio y al final
        val name = nameField.text.toString().trim()
        val lastname = lastnameField.text.toString().trim()
        val password = passwordField.text.toString().trim()
        val name2 = name2Field.text.toString().trim()
        val lastname2 = lastname2Field.text.toString().trim()

        // Expresión regular para validar los campos de nombre y apellido (permitiendo letras, números, espacios y símbolos comunes)
        val nameRegex = Regex("^[a-zA-Z0-9áéíóúüñÁÉÍÓÚÜÑ\\s.,'-]+$")

        // Obtener las selecciones de los Spinners y sus arrays correspondientes
        val nacionalidadSpinner = findViewById<Spinner>(R.id.nacionalidadSpinner)
        val nacionalidad = nacionalidadSpinner.selectedItem.toString()
        val nacionalidadesArray = resources.getStringArray(R.array.nacionalidades)

        val razaSpinner = findViewById<Spinner>(R.id.razaSpinner)
        val raza = razaSpinner.selectedItem.toString()
        val razasArray = resources.getStringArray(R.array.razas)

        val edadSpinner = findViewById<Spinner>(R.id.edadSpinner)
        val edad = edadSpinner.selectedItem.toString()
        val edadesArray = resources.getStringArray(R.array.edades)

        // Obtener las selecciones de los Spinners para la Persona 2
        val nacionalidad2Spinner = findViewById<Spinner>(R.id.nacionalidad2Spinner)
        val nacionalidad2 = nacionalidad2Spinner.selectedItem.toString()

        val raza2Spinner = findViewById<Spinner>(R.id.raza2Spinner)
        val raza2 = raza2Spinner.selectedItem.toString()

        val edad2Spinner = findViewById<Spinner>(R.id.edad2Spinner)
        val edad2 = edad2Spinner.selectedItem.toString()

        // Obtener las selecciones de los CheckBoxes para el nivel académico de la Persona 1
        val nivelAcademicoPerson1 = mutableListOf<String>()
        if (primariaCheckBox.isChecked) nivelAcademicoPerson1.add("Primaria")
        if (secundariaCheckBox.isChecked) nivelAcademicoPerson1.add("Secundaria")
        if (tecnicoCheckBox.isChecked) nivelAcademicoPerson1.add("Técnico")
        if (tecnologoCheckBox.isChecked) nivelAcademicoPerson1.add("Tecnólogo")
        if (universitarioCheckBox.isChecked) nivelAcademicoPerson1.add("Universitario")
        if (posgradoCheckBox.isChecked) nivelAcademicoPerson1.add("Posgrado")
        if (doctoradoCheckBox.isChecked) nivelAcademicoPerson1.add("Doctorado")
        if (posdoctoradoCheckBox.isChecked) nivelAcademicoPerson1.add("Posdoctorado")

        // Obtener las selecciones de los CheckBoxes para la personalidad de la Persona 1
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

        // Obtener las selecciones de los CheckBoxes para el nivel académico de la Persona 2
        val nivelAcademicoPerson2 = mutableListOf<String>()
        if (primaria2CheckBox.isChecked) nivelAcademicoPerson2.add("Primaria")
        if (secundaria2CheckBox.isChecked) nivelAcademicoPerson2.add("Secundaria")
        if (tecnico2CheckBox.isChecked) nivelAcademicoPerson2.add("Técnico")
        if (tecnologo2CheckBox.isChecked) nivelAcademicoPerson2.add("Tecnólogo")
        if (universitario2CheckBox.isChecked) nivelAcademicoPerson2.add("Universitario")
        if (posgrado2CheckBox.isChecked) nivelAcademicoPerson2.add("Posgrado")
        if (doctorado2CheckBox.isChecked) nivelAcademicoPerson2.add("Doctorado")
        if (posdoctorado2CheckBox.isChecked) nivelAcademicoPerson2.add("Posdoctorado")

        // Obtener las selecciones de los CheckBoxes para la personalidad de la Persona 2
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

        // Validar los campos según el método de autenticación
        return when (authMethod) {
            "google" -> {
                // Validaciones para el método de autenticación con Google
                name.isNotBlank() && name.matches(nameRegex) && // Verificar que el nombre no esté vacío y coincida con el patrón
                        lastname.isNotBlank() && lastname.matches(nameRegex) && // Verificar que el apellido no esté vacío y coincida con el patrón
                        nacionalidad in nacionalidadesArray && // Verificar que la nacionalidad esté en el array de nacionalidades
                        raza in razasArray && // Verificar que la raza esté en el array de razas
                        edad in edadesArray && // Verificar que la edad esté en el array de edades
                        name2.isNotBlank() && name2.matches(nameRegex) && // Verificar que el nombre de la persona 2 no esté vacío y coincida con el patrón
                        lastname2.isNotBlank() && lastname2.matches(nameRegex) && // Verificar que el apellido de la persona 2 no esté vacío y coincida con el patrón
                        nacionalidad2 in nacionalidadesArray && // Verificar que la nacionalidad de la persona 2 esté en el array de nacionalidades
                        raza2 in razasArray && // Verificar que la raza de la persona 2 esté en el array de razas
                        edad2 in edadesArray && // Verificar que la edad de la persona 2 esté en el array de edades
                        nivelAcademicoPerson1.isNotEmpty() && // Verificar que se haya seleccionado al menos un nivel académico para la persona 1
                        personalidadPerson1.isNotEmpty() && // Verificar que se haya seleccionado al menos una personalidad para la persona 1
                        nivelAcademicoPerson2.isNotEmpty() && // Verificar que se haya seleccionado al menos un nivel académico para la persona 2
                        personalidadPerson2.isNotEmpty() // Verificar que se haya seleccionado al menos una personalidad para la persona 2
            }

            else -> { // "email"
                // Validaciones para el método de autenticación con email
                name.isNotBlank() && name.matches(nameRegex) && // Verificar que el nombre no esté vacío y coincida con el patrón
                        lastname.isNotBlank() && lastname.matches(nameRegex) && // Verificar que el apellido no esté vacío y coincida con el patrón
                        email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches() && // Verificar que el email no esté vacío y coincida con el patrón de email
                        password.isNotBlank() && password.length >= 6 && // Verificar que la contraseña no esté vacía y tenga al menos 6 caracteres
                        nacionalidad in nacionalidadesArray && // Verificar que la nacionalidad esté en el array de nacionalidades
                        raza in razasArray && // Verificar que la raza esté en el array de razas
                        edad in edadesArray && // Verificar que la edad esté en el array de edades
                        name2.isNotBlank() && name2.matches(nameRegex) && // Verificar que el nombre de la persona 2 no esté vacío y coincida con el patrón
                        lastname2.isNotBlank() && lastname2.matches(nameRegex) && // Verificar que el apellido de la persona 2 no esté vacío y coincida con el patrón
                        nacionalidad2 in nacionalidadesArray && // Verificar que la nacionalidad de la persona 2 esté en el array de nacionalidades
                        raza2 in razasArray && // Verificar que la raza de la persona 2 esté en el array de razas
                        edad2 in edadesArray && // Verificar que la edad de la persona 2 esté en el array de edades
                        nivelAcademicoPerson1.isNotEmpty() && // Verificar que se haya seleccionado al menos un nivel académico para la persona 1
                        personalidadPerson1.isNotEmpty() && // Verificar que se haya seleccionado al menos una personalidad para la persona 1
                        nivelAcademicoPerson2.isNotEmpty() && // Verificar que se haya seleccionado al menos un nivel académico para la persona 2
                        personalidadPerson2.isNotEmpty() // Verificar que se haya seleccionado al menos una personalidad para la persona 2
            }
        }
    }

    private fun registerUser(authMethod: String) {
        // Mostrar la barra de progreso
        progressBar.visibility = View.VISIBLE

        // Obtener los valores de los campos de texto y Spinners para la Persona 1
        val name = nameField.text.toString().trim()
        val lastname = lastnameField.text.toString().trim()
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString().trim()
        val nacionalidad = findViewById<Spinner>(R.id.nacionalidadSpinner).selectedItem.toString()
        val raza = findViewById<Spinner>(R.id.razaSpinner).selectedItem.toString()
        val edad = findViewById<Spinner>(R.id.edadSpinner).selectedItem.toString()
        val nivelAcademicoPerson1 = getNivelAcademico(1)
        val personalidadPerson1 = getPersonalidad(1)

        // Obtener los valores de los campos de texto y Spinners para la Persona 2
        val name2 = name2Field.text.toString().trim()
        val lastname2 = lastname2Field.text.toString().trim()
        val nacionalidad2 = findViewById<Spinner>(R.id.nacionalidad2Spinner).selectedItem.toString()
        val raza2 = findViewById<Spinner>(R.id.raza2Spinner).selectedItem.toString()
        val edad2 = findViewById<Spinner>(R.id.edad2Spinner).selectedItem.toString()
        val nivelAcademicoPerson2 = getNivelAcademico(2)
        val personalidadPerson2 = getPersonalidad(2)

        // Realizar el registro si el método de autenticación es "email"
        if (authMethod == "email") {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    // Llamar a handleFirebaseAuthResult para manejar el resultado del registro
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
        // Verificar si la tarea de autenticación fue exitosa
        if (task.isSuccessful) {
            // Obtener el usuario actual
            val user = auth.currentUser
            if (user != null) {
                // Si el email es nulo, usar el email del usuario de Firebase
                val userEmail = email ?: user.email ?: ""
                // Llamar a uploadImagesAndSaveData para subir imágenes y guardar datos del usuario
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
                // Si no se puede obtener el usuario, ocultar la barra de progreso y mostrar un mensaje de error
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this,
                    "Error: No se pudo obtener el usuario autenticado.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            // Si la tarea de autenticación falló, ocultar la barra de progreso y manejar el error
            progressBar.visibility = View.GONE
            val errorMessage = if (task.exception is FirebaseAuthUserCollisionException) {
                // Si el error es por colisión de usuario, mostrar un mensaje específico
                "Ya existe una cuenta con este correo electrónico."
            } else {
                // Si el error es otro, mostrar un mensaje de error genérico
                "Error en el registro: ${task.exception?.message}"
            }
            // Llamar a handleRegistrationError para manejar el error de registro
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
        // Mostrar la barra de progreso si estaba oculta
        if (progressBar.isGone) {
            progressBar.isGone = false
}
        // Verificar si hay imágenes seleccionadas
        if (selectedImageUris.isNotEmpty()) {
            // Crear una referencia a la carpeta de almacenamiento donde se guardarán las fotos del usuario
            val storageRef = storage.reference.child("users/$userId/photos")
            // Lista para almacenar las URLs de las imágenes subidas
            val uploadedImageUrls = mutableListOf<String>()
            // variable para verificar el estado de las subidas de las imagenes
            var allImagesUploaded = true

            // Iterar a través de las URIs de las imágenes seleccionadas
            for ((index, uri) in selectedImageUris.withIndex()) {
                // Crear una referencia única para cada imagen basada en el tiempo actual y el contador
                val imageRef = storageRef.child("${System.currentTimeMillis()}_${index}.jpg")
                // Subir la imagen a Firebase Storage
                imageRef.putFile(uri)
                    .addOnSuccessListener { taskSnapshot ->
                        // Si la subida de la imagen es exitosa, obtener la URL de descarga
                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            // Agregar la URL de descarga a la lista
                            uploadedImageUrls.add(downloadUri.toString())
                            // Verificar si todas las imágenes se han subido
                            if (uploadedImageUrls.size == selectedImageUris.size) {
                                // Si todas las imágenes se han subido, guardar los datos del usuario
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
                            .addOnFailureListener { e ->
                                // Si la subida de la imagen falla, ocultar la barra de progreso y mostrar un mensaje de error
                                allImagesUploaded = false
                                progressBar.visibility = View.GONE
                                Toast.makeText(
                                    this,
                                    "Error de red al subir la imagen: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        // Si la subida de la imagen falla, ocultar la barra de progreso y mostrar un mensaje de error
                        allImagesUploaded = false
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this,
                            "Error de red al subir la imagen: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            if (!allImagesUploaded){
                return
            }
        } else {
            // Si no hay imágenes seleccionadas, guardar los datos del usuario directamente
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
        // Crear un mapa de datos del usuario
        val userMap = mapOf(
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

        // Guardar los datos en Firestore
        db.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                // Ocultar la barra de progreso
                progressBar.visibility = View.GONE
                // Mostrar mensaje de éxito
                Toast.makeText(
                    this,
                    "Registro exitoso!",
                    Toast.LENGTH_SHORT
                ).show()
                // Cerrar la actividad
                finish()
            }
            .addOnFailureListener { e ->
                // Ocultar la barra de progreso
                progressBar.visibility = View.GONE
                // Mostrar mensaje de error
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
        val checkBoxes = if (person == 1) {
            listOf(
                primariaCheckBox to "Primaria",
                secundariaCheckBox to "Secundaria",
                tecnicoCheckBox to "Técnico",
                tecnologoCheckBox to "Tecnólogo",
                universitarioCheckBox to "Universitario",
                posgradoCheckBox to "Posgrado",
                doctoradoCheckBox to "Doctorado",
                posdoctoradoCheckBox to "Posdoctorado"
            )
        } else {
            listOf(
                primaria2CheckBox to "Primaria",
                secundaria2CheckBox to "Secundaria",
                tecnico2CheckBox to "Técnico",
                tecnologo2CheckBox to "Tecnólogo",
                universitario2CheckBox to "Universitario",
                posgrado2CheckBox to "Posgrado",
                doctorado2CheckBox to "Doctorado",
                posdoctorado2CheckBox to "Posdoctorado"
            )
        }
        return checkBoxes.filter { it.first.isChecked }.map { it.second }
    }

    private fun getPersonalidad(person: Int): List<String> {
        val checkBoxes = if (person == 1) {
            listOf(
                extrovertidoCheckBox to "Extrovertido",
                introvertidoCheckBox to "Introvertido",
                aventureroCheckBox to "Aventurero",
                creativoCheckBox to "Creativo",
                analiticoCheckBox to "Analítico",
                emocionalCheckBox to "Emocional",
                racionalCheckBox to "Racional",
                perfeccionistaCheckBox to "Perfeccionista",
                sonadorCheckBox to "Soñador",
                pragmaticoCheckBox to "Pragmático",
                carismaticoCheckBox to "Carismático",
                reflexivoCheckBox to "Reflexivo"
            )
        } else {
            listOf(
                extrovertido2CheckBox to "Extrovertido",
                introvertido2CheckBox to "Introvertido",
                aventurero2CheckBox to "Aventurero",
                creativo2CheckBox to "Creativo",
                analitico2CheckBox to "Analítico",
                emocional2CheckBox to "Emocional",
                racional2CheckBox to "Racional",
                perfeccionista2CheckBox to "Perfeccionista",
                sonador2CheckBox to "Soñador",
                pragmatico2CheckBox to "Pragmático",
                carismatico2CheckBox to "Carismático",
                reflexivo2CheckBox to "Reflexivo"
            )
        }
        return checkBoxes.filter { it.first.isChecked }.map { it.second }
    }

    private fun handleRegistrationError(exception: Exception?, message: String) {
        Log.e("RegisterActivity", "Registration Error: $message", exception)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}