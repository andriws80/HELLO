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
import androidx.core.view.children
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class CreateProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null

    // Datos para elementos dinámicos (puedes moverlos a strings.xml si prefieres)
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
        val apellidoEditText = findViewById<EditText>(R.id.apellidoEditText) // Added
        val paisResidenciaSpinner = findViewById<Spinner>(R.id.paisResidenciaSpinner)
        val nacionalidadSpinner = findViewById<Spinner>(R.id.nacionalidadSpinner) // Added
        val selectImageButton = findViewById<Button>(R.id.selectImageButton)
        val uploadImageButton = findViewById<Button>(R.id.uploadImageButton)
        val guardarPerfilButton = findViewById<Button>(R.id.guardarPerfilButton)

        //  AÑADIR ESTE CÓDIGO PARA CHECKBOXES Y RADIOBUTTONS DINÁMICOS
        setupCheckBoxes(personalidades, R.id.personalidadCheckBoxes)
        setupCheckBoxes(nivelesAcademicos, R.id.nivelAcademicoCheckBoxes)
        setupCheckBoxes(tiposPersonalidad, R.id.tipoPersonalidadCheckBoxes)
        setupCheckBoxes(mascotas, R.id.mascotasCheckBoxes)
        setupCheckBoxes(religiones, R.id.religionCheckBoxes)
        setupRadioButtons(ideologiasPoliticas, R.id.ideologiaPoliticaRadioGroup)
        setupRadioButtons(consumos, R.id.consumoAlcoholRadioGroup) // Reutilizamos para alcohol
        setupRadioButtons(consumos, R.id.consumoTabacoRadioGroup)   // y tabaco
        setupCheckBoxes(deportes, R.id.deportesCheckBoxes)
        setupCheckBoxes(hobbies, R.id.hobbiesCheckBoxes)
        setupCheckBoxes(generosMusicales, R.id.generoMusicalCheckBoxes)


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
            val apellido = apellidoEditText.text.toString().trim() // Added
            val pais = paisResidenciaSpinner.selectedItem.toString()
            val nacionalidad = nacionalidadSpinner.selectedItem.toString() // Added

            if (nombres.isEmpty() || apellido.isEmpty()) {
                Toast.makeText(this, "Ingrese los nombres y el apellido", Toast.LENGTH_SHORT).show() // Modified
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

            saveProfileToFirestore(
                nombres,
                apellido, // Added
                pais,
                nacionalidad, // Added
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

    private fun saveProfileToFirestore(
        nombres: String,
        apellido: String,
        pais: String,
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
        val userId = auth.currentUser?.uid ?: return
        val profileData = hashMapOf(
            "nombres" to nombres,
            "apellido" to apellido,
            "paisResidencia" to pais,
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
            "consumoTabaco" to consumoTabaco,
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