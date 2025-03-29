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
import kotlin.toString

class CreateProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null

    private val personalidades = listOf("Introvertido", "Extrovertido", "Ambivertido")
    private val nivelesAcademicos = listOf("Primaria", "Secundaria", "Universidad", "Posgrado")
    private val tiposPersonalidad = listOf("Optimista", "Pesimista", "Realista")
    private val mascotas = listOf("Perro", "Gato", "Pájaro", "Otro")
    private val religiones = listOf("Católico", "Cristiano", "Judío", "Musulmán", "Otro", "Agnóstico", "Ateo")
    private val deportes = listOf("Fútbol", "Baloncesto", "Tenis", "Natación", "Otro")
    private val hobbies = listOf("Leer", "Viajar", "Cocinar", "Música", "Otro")
    private val generosMusicales = listOf("Rock", "Pop", "Clásica", "Electrónica", "Otro")
    private val ideologiasPoliticas = listOf("Izquierda", "Centro", "Derecha", "Otro")
    private val consumos = listOf("Nunca", "Ocasional", "Frecuente")

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

        val nombresEditText = findViewById<EditText>(R.id.nombres)  //  Corregido el ID
        val apellidoEditText = findViewById<EditText>(R.id.apellidoEditText)
        val nacionalidadSpinner = findViewById<Spinner>(R.id.nacionalidadSpinner)
        val selectImageButton = findViewById<Button>(R.id.changeImageButton)  // Corregido el ID
        val guardarPerfilButton = findViewById<Button>(R.id.nextButton)  // Corregido el ID

        //  Obtenemos la referencia al AutoCompleteTextView:
        val paisCiudadResidenciaAutoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.paisCiudadResidenciaAutoCompleteTextView)

        //  Adaptador para sugerencias del AutoCompleteTextView:
        val paisesCiudades = resources.getStringArray(R.array.paises_ciudades)  //  Debes crear este array en strings.xml
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, paisesCiudades)
        paisCiudadResidenciaAutoCompleteTextView.setAdapter(adapter)

        //  Opcional: Configurar un listener para cuando se selecciona un elemento de la lista de sugerencias.
        paisCiudadResidenciaAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val seleccion = adapter.getItem(position)
            Toast.makeText(this, "Seleccionaste: $seleccion", Toast.LENGTH_SHORT).show()
            // Puedes hacer algo más con la selección aquí, como guardarla en una variable.
        }

        selectImageButton.setOnClickListener { pickImageFromGallery() }
        // uploadImageButton ya no es necesario, la imagen se sube al guardar el perfil.
        // uploadImageButton.setOnClickListener { /* ... */ }

        guardarPerfilButton.setOnClickListener {
            val nombres = nombresEditText.text.toString().trim()
            val apellido = apellidoEditText.text.toString().trim()
            val paisCiudadResidencia = paisCiudadResidenciaAutoCompleteTextView.text.toString().trim()
            val nacionalidad = nacionalidadSpinner.selectedItem.toString()

            if (nombres.isEmpty() || apellido.isEmpty()) {
                Toast.makeText(this, "Ingrese los nombres y el apellido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //  Aquí deberías recoger los datos de los CheckBoxes y RadioButtons si los estás usando en tu layout.
            //  Como los métodos setupCheckBoxes y setupRadioButtons no están definidos, esta parte se deja como comentario.
            /*
            val personalidad = getSelectedCheckBoxes(R.id.personalidadCheckBoxes)
            val nivelAcademico = getSelectedCheckBoxes(R.id.nivelAcademicoCheckBoxes)
            // ... recoger datos de otros campos ...
            val ideologiaPolitica = getSelectedRadioButton(R.id.ideologiaPoliticaRadioGroup)
            // ...
             */

            val userData: HashMap<String, Any> = hashMapOf(
                "nombre" to nombres,  //  Corregido el nombre del campo
                "apellido" to apellido,
                "paisCiudadResidencia" to paisCiudadResidencia,
                "nacionalidad" to nacionalidad,
                //  Añadir aquí los demás campos recogidos de CheckBoxes y RadioButtons
                /*
                "personalidad" to personalidad,
                "nivelAcademico" to nivelAcademico,
                // ...
                "ideologiaPolitica" to ideologiaPolitica
                // ...
                 */
            )

            if (imageUri != null) {
                uploadImageToFirebase(userData)
            } else {
                saveProfileToFirestore(userData)
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun uploadImageToFirebase(userData: HashMap<String, Any>) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("profile_images/$userId.jpg") // Corregido el nombre de la carpeta

        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, imageUri!!)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            }

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)  // Reducida la calidad
            val imageData = byteArrayOutputStream.toByteArray()

            storageRef.putBytes(imageData)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        userData["profileImageUrl"] = uri.toString()  // Añadida la URL al mapa de datos
                        saveProfileToFirestore(userData)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProfileToFirestore(profileData: HashMap<String, Any>) { //  Corregido el tipo de parámetro
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)  //  Corregido el nombre de la colección
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

    //  Si necesitas los métodos setupCheckBoxes, setupRadioButtons y los métodos getSelected...
    //  debes implementarlos aquí o adaptar la lógica directamente en el listener del botón guardarPerfilButton.
    /*
    private fun setupCheckBoxes(opciones: List<String>, containerId: Int) { ... }
    private fun setupRadioButtons(opciones: List<String>, radioGroupId: Int) { ... }
    private fun getSelectedCheckBoxes(containerId: Int): List<String> { ... }
    private fun getSelectedRadioButton(radioGroupId: Int): String? { ... }
    */
}