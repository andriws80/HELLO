package com.andriws.hello

import android.app.Activity.RESULT_OK
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
import androidx.core.view.children
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
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

    // Datos para elementos dinámicos (puedes moverlos a strings.xml)
    private val personalidades = listOf("Introvertido", "Extrovertido", "Ambivertido")
    private val nivelesAcademicos = listOf("Primaria", "Secundaria", "Universidad", "Posgrado")
    private val tiposPersonalidad = listOf("Optimista", "Pesimista", "Realista")
    private val mascotas = listOf("Perro", "Gato", "Pájaro", "Otro")
    private val religiones = listOf("Católico", "Cristiano", "Judío", "Musulmán", "Otro", "Agnóstico", "Ateo")
    private val deportes = listOf("Fútbol", "Baloncesto", "Tenis", "Natación", "Otro")
    private val hobbies = listOf("Leer", "Viajar", "Cocinar", "Música", "Otro")
    private val generosMusicales = listOf("Rock", "Pop", "Clásica", "Electrónica", "Otro")
    private val ideologiasPoliticas = listOf("Izquierda", "Centro", "Derecha", "Otro")
    private val consumos = listOf("Nunca", "Ocasional", "Frecuente") // Para alcohol y tabaco

    private val pickImageLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
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

       nombreEditText = findViewById(R.id.nombres)
        apellidoEditText = findViewById(R.id.apellidoEditText)
        nacionalidadSpinner = findViewById(R.id.nacionalidadSpinner)
        profileImageView = findViewById(R.id.profileImageView)
        changeImageButton = findViewById(R.id.changeImageButton)
        saveChangesButton = findViewById(R.id.guardarCambiosButton)

        // Configurar elementos dinámicos
        setupCheckBoxes(personalidades, R.id.personalidadCheckBoxes)
        setupCheckBoxes(nivelesAcademicos, R.id.nivelAcademicoCheckBoxes)
        setupCheckBoxes(tiposPersonalidad, R.id.tipoPersonalidadCheckBoxes)
        setupCheckBoxes(mascotas, R.id.mascotasCheckBoxes)
        setupCheckBoxes(religiones, R.id.religionCheckBoxes)
        setupRadioButtons(ideologiasPoliticas, R.id.ideologiaPoliticaRadioGroup)
        setupRadioButtons(consumos, R.id.consumoAlcoholRadioGroup)
        setupRadioButtons(consumos, R.id.consumoTabacoRadioGroup)
        setupCheckBoxes(deportes, R.id.deportesCheckBoxes)
        setupCheckBoxes(hobbies, R.id.hobbiesCheckBoxes)
        setupCheckBoxes(generosMusicales, R.id.generoMusicalCheckBoxes)


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

            // Recoger datos de CheckBoxes
            val personalidad = getSelectedCheckBoxes(R.id.personalidadCheckBoxes)
            val nivelAcademico = getSelectedCheckBoxes(R.id.nivelAcademicoCheckBoxes)
            val tipoPersonalidad = getSelectedCheckBoxes(R.id.tipoPersonalidadCheckBoxes)
            val mascotasSeleccionadas = getSelectedCheckBoxes(R.id.mascotasCheckBoxes)
            val religionesSeleccionadas = getSelectedCheckBoxes(R.id.religionCheckBoxes)
            val deportesSeleccionados = getSelectedCheckBoxes(R.id.deportesCheckBoxes)
            val hobbiesSeleccionados = getSelectedCheckBoxes(R.id.hobbiesCheckBoxes)
            val generosMusicalesSeleccionados = getSelectedCheckBoxes(R.id.generoMusicalCheckBoxes)

            // Recoger datos de RadioButtons
            val ideologiaPolitica = getSelectedRadioButton(R.id.ideologiaPoliticaRadioGroup)
            val consumoAlcohol = getSelectedRadioButton(R.id.consumoAlcoholRadioGroup)
            val consumoTabaco = getSelectedRadioButton(R.id.consumoTabacoRadioGroup)


            if (imageUri != null) {
                uploadImageAndSaveProfile(
                    nombre,
                    apellido,
                    nacionalidad,
                    personalidad,
                    nivelAcademico,
                    tipoPersonalidad,
                    mascotasSeleccionadas,
                    religionesSeleccionadas,
                    deportesSeleccionados,
                    hobbiesSeleccionados,
                    generosMusicalesSeleccionados,
                    ideologiaPolitica,
                    consumoAlcohol,
                    consumoTabaco
                )
            } else {
                updateProfileInFirestore(
                    nombre,
                    apellido,
                    nacionalidad,
                    null,
                    personalidad,
                    nivelAcademico,
                    tipoPersonalidad,
                    mascotasSeleccionadas,
                    religionesSeleccionadas,
                    deportesSeleccionados,
                    hobbiesSeleccionados,
                    generosMusicalesSeleccionados,
                    ideologiaPolitica,
                    consumoAlcohol,
                    consumoTabaco
                )
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
                        prefillProfileData(document)
                    } else {
                        Log.e("Firestore", "El documento no existe en Firestore")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error al obtener el perfil", e)
                }
        }
    }

    private fun prefillProfileData(document: DocumentSnapshot) {
        val nombre = document.getString("nombres") ?: ""
        val apellido = document.getString("apellido") ?: ""
        val nacionalidad = document.getString("nacionalidad") ?: ""
        nombreEditText.setText(nombre)
        apellidoEditText.setText(apellido)
        (nacionalidadSpinner.adapter as? ArrayAdapter<String>)?.getPosition(nacionalidad)?.takeIf { it >= 0 }?.let { nacionalidadSpinner.setSelection(it) }
        document.getString("profileImageUrl")?.takeIf { it.isNotEmpty() }?.let { Glide.with(this).load(it).into(profileImageView) }

        // Prefill CheckBoxes
        prefillCheckBoxes(document, "personalidad", R.id.personalidadCheckBoxes)
        prefillCheckBoxes(document, "nivelAcademico", R.id.nivelAcademicoCheckBoxes)
        prefillCheckBoxes(document, "tipoPersonalidad", R.id.tipoPersonalidadCheckBoxes)
        prefillCheckBoxes(document, "mascotas", R.id.mascotasCheckBoxes)
        prefillCheckBoxes(document, "religiones", R.id.religionCheckBoxes)
        prefillCheckBoxes(document, "deportes", R.id.deportesCheckBoxes)
        prefillCheckBoxes(document, "hobbies", R.id.hobbiesCheckBoxes)
        prefillCheckBoxes(document, "generosMusicales", R.id.generoMusicalCheckBoxes)

        // Prefill RadioButtons
        prefillRadioButtons(document, "ideologiaPolitica", R.id.ideologiaPoliticaRadioGroup)
        prefillRadioButtons(document, "consumoAlcohol", R.id.consumoAlcoholRadioGroup)
        prefillRadioButtons(document, "consumoTabaco", R.id.consumoTabacoRadioGroup)

    }


    private fun prefillCheckBoxes(document: DocumentSnapshot, field: String, containerId: Int) {
        val selectedValues = document.get(field) as? List<String> ?: emptyList()
        val container = findViewById<LinearLayout>(containerId)
        container.children.filterIsInstance<CheckBox>().forEach { checkBox ->
            checkBox.isChecked = selectedValues.contains(checkBox.text.toString())
        }
    }

    private fun prefillRadioButtons(document: DocumentSnapshot, field: String, radioGroupId: Int) {
        val selectedValue = document.getString(field)
        val radioGroup = findViewById<RadioGroup>(radioGroupId)
        radioGroup.children.filterIsInstance<RadioButton>().forEach { radioButton ->
            radioButton.isChecked = radioButton.text.toString() == selectedValue
        }
    }


    private fun uploadImageAndSaveProfile(
        nombre: String,
        apellido: String,
        nacionalidad: String,
        personalidad: List<String>,
        nivelAcademico: List<String>,
        tipoPersonalidad: List<String>,
        mascotas: List<String>,
        religiones: List<String>,
        deportes: List<String>,
        hobbies: List<String>,
        generosMusicales: List<String>,
        ideologiaPolitica: String?,
        consumoAlcohol: String?,
        consumoTabaco: String?

    ) {
        auth.currentUser?.uid?.let { userId ->
            val storageRef = storage.reference.child("profile_images/$userId.jpg")

            getBitmapFromUri(imageUri)?.let { bitmap ->
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                val imageData = baos.toByteArray()

                storageRef.putBytes(imageData)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            updateProfileInFirestore(
                                nombre,
                                apellido,
                                nacionalidad,
                                uri.toString(),
                                personalidad,
                                nivelAcademico,
                                tipoPersonalidad,
                                mascotas,
                                religiones,
                                deportes,
                                hobbies,
                                generosMusicales,
                                ideologiaPolitica,
                                consumoAlcohol,
                                consumoTabaco
                            )
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

    private fun updateProfileInFirestore(
        nombre: String,
        apellido: String,
        nacionalidad: String,
        imageUrl: String?,
        personalidad: List<String>,
        nivelAcademico: List<String>,
        tipoPersonalidad: List<String>,
        mascotas: List<String>,
        religiones: List<String>,
        deportes: List<String>,
        hobbies: List<String>,
        generosMusicales: List<String>,
        ideologiaPolitica: String?,
        consumoAlcohol: String?,
        consumoTabaco: String?
    ) {
        auth.currentUser?.uid?.let { userId ->
            val profileUpdates = mutableMapOf(
                "nombres" to nombre,
                "apellido" to apellido,
                "nacionalidad" to nacionalidad,
                "personalidad" to personalidad,
                "nivelAcademico" to nivelAcademico,
                "tipoPersonalidad" to tipoPersonalidad,
                "mascotas" to mascotas,
                "religiones" to religiones,
                "deportes" to deportes,
                "hobbies" to hobbies,
                "generosMusicales" to generosMusicales,
                "ideologiaPolitica" to ideologiaPolitica,
                "consumoAlcohol" to consumoAlcohol,
                "consumoTabaco" to consumoTabaco
            )

            imageUrl?.let {
                profileUpdates["profileImageUrl"] = it
            }

            firestore.collection("perfiles").document(userId)
                .update(profileUpdates as Map<String, Any>)  // Use update instead of set
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

    private fun setupCheckBoxes(opciones: List<String>, containerId: Int) {
        val container = findViewById<LinearLayout>(containerId)
        opciones.forEach { opcion ->
            val checkBox = CheckBox(this)
            checkBox.text = opcion
            container.addView(checkBox)
        }
    }

    private fun setupRadioButtons(opciones: List<String>, radioGroupId: Int) {
        val radioGroup = findViewById<RadioGroup>(radioGroupId)
        opciones.forEach { opcion ->
            val radioButton = RadioButton(this)
            radioButton.text = opcion
            radioGroup.addView(radioButton)
        }
    }

    private fun getSelectedCheckBoxes(containerId: Int): List<String> {
        val container = findViewById<LinearLayout>(containerId)
        return container.children.filterIsInstance<CheckBox>()
            .filter { it.isChecked }
            .map { it.text.toString() }
            .toList()
    }

    private fun getSelectedRadioButton(radioGroupId: Int): String? {
        val radioGroup = findViewById<RadioGroup>(radioGroupId)
        val selectedId = radioGroup.checkedRadioButtonId
        return if (selectedId != -1) {
            val radioButton = radioGroup.findViewById<RadioButton>(selectedId)
            radioButton?.text?.toString()
        } else {
            null
        }
    }
}