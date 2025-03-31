package com.andriws.hello

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var changeImageButton: Button
    private lateinit var nombresEditText: TextInputEditText
    private lateinit var apellidoEditText: TextInputEditText
    private lateinit var nacionalidadSpinner: Spinner
    private lateinit var razaSpinner1: Spinner
    private lateinit var nombresParejaEditText: TextInputEditText
    private lateinit var apellidoParejaEditText: TextInputEditText
    private lateinit var nacionalidadParejaSpinner: Spinner
    private lateinit var razaSpinner2: Spinner
    private lateinit var paisCiudadResidenciaAutoCompleteTextView: AutoCompleteTextView
    private lateinit var tipoRelacionSpinner: Spinner
    private lateinit var hijosSpinner: Spinner
    private lateinit var perrosCheckBox: CheckBox
    private lateinit var gatosCheckBox: CheckBox
    private lateinit var conejosCheckBox: CheckBox
    private lateinit var hamstersCheckBox: CheckBox
    private lateinit var otrosCheckBox: CheckBox
    private lateinit var religionRadioGroup: RadioGroup
    private lateinit var religionCatolicaRadioButton: RadioButton
    private lateinit var religionCristianaRadioButton: RadioButton
    private lateinit var religionJudiaRadioButton: RadioButton
    private lateinit var religionMusulmanaRadioButton: RadioButton
    private lateinit var religionHinduRadioButton: RadioButton
    private lateinit var religionNingunaRadioButton: RadioButton
    private lateinit var ideologiaPoliticaSpinner: Spinner
    private lateinit var consumoAlcoholRadioGroup: RadioGroup
    private lateinit var consumoAlcoholSiRadioButton: RadioButton
    private lateinit var consumoAlcoholNoRadioButton: RadioButton
    private lateinit var consumoAlcoholAVecesRadioButton: RadioButton
    private lateinit var consumoTabacoRadioGroup: RadioGroup
    private lateinit var consumoTabacoSiRadioButton: RadioButton
    private lateinit var consumoTabacoNoRadioButton: RadioButton
    private lateinit var consumoTabacoAVecesRadioButton: RadioButton
    private lateinit var futbolCheckBox: CheckBox
    private lateinit var baloncestoCheckBox: CheckBox
    private lateinit var natacionCheckBox: CheckBox
    private lateinit var ciclismoCheckBox: CheckBox
    private lateinit var tenisCheckBox: CheckBox
    private lateinit var otrosDeportesCheckBox: CheckBox
    private lateinit var lecturaCheckBox: CheckBox
    private lateinit var viajesCheckBox: CheckBox
    private lateinit var cocinaCheckBox: CheckBox
    private lateinit var jardineriaCheckBox: CheckBox
    private lateinit var manualidadesCheckBox: CheckBox
    private lateinit var otrosHobbiesCheckBox: CheckBox
    private lateinit var dietaSpinner: Spinner
    private lateinit var generoMusicalSpinner: Spinner
    private lateinit var nextButton: Button

    private var selectedImageUri: Uri? = null
    private lateinit var storageReference: StorageReference
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile)

        profileImageView = findViewById(R.id.profileImageView)
        changeImageButton = findViewById(R.id.changeImageButton)
        nombresEditText = findViewById(R.id.nombres)
        apellidoEditText = findViewById(R.id.apellidoEditText)
        nacionalidadSpinner = findViewById(R.id.nacionalidadSpinner)
        razaSpinner1 = findViewById(R.id.razaSpinner1)
        nombresParejaEditText = findViewById(R.id.nombresParejaEditText)
        apellidoParejaEditText = findViewById(R.id.apellidoParejaEditText)
        nacionalidadParejaSpinner = findViewById(R.id.nacionalidadParejaSpinner)
        razaSpinner2 = findViewById(R.id.razaSpinner2)
        paisCiudadResidenciaAutoCompleteTextView = findViewById(R.id.paisCiudadResidenciaAutoCompleteTextView)
        tipoRelacionSpinner = findViewById(R.id.tipoRelacionSpinner)
        hijosSpinner = findViewById(R.id.hijosSpinner)
        perrosCheckBox = findViewById(R.id.perrosCheckBox)
        gatosCheckBox = findViewById(R.id.gatosCheckBox)
        conejosCheckBox = findViewById(R.id.conejosCheckBox)
        hamstersCheckBox = findViewById(R.id.hamstersCheckBox)
        otrosCheckBox = findViewById(R.id.otrosCheckBox)
        religionRadioGroup = findViewById(R.id.religionRadioGroup)
        religionCatolicaRadioButton = findViewById(R.id.religionCatolicaRadioButton)
        religionCristianaRadioButton = findViewById(R.id.religionCristianaRadioButton)
        religionJudiaRadioButton = findViewById(R.id.religionJudiaRadioButton)
        religionMusulmanaRadioButton = findViewById(R.id.religionMusulmanaRadioButton)
        religionHinduRadioButton = findViewById(R.id.religionHinduRadioButton)
        religionNingunaRadioButton = findViewById(R.id.religionNingunaRadioButton)
        ideologiaPoliticaSpinner = findViewById(R.id.ideologiaPoliticaSpinner)
        consumoAlcoholRadioGroup = findViewById(R.id.consumoAlcoholRadioGroup)
        consumoAlcoholSiRadioButton = findViewById(R.id.consumoAlcoholSiRadioButton)
        consumoAlcoholNoRadioButton = findViewById(R.id.consumoAlcoholNoRadioButton)
        consumoAlcoholAVecesRadioButton = findViewById(R.id.consumoAlcoholAVecesRadioButton)
        consumoTabacoRadioGroup = findViewById(R.id.consumoTabacoRadioGroup)
        consumoTabacoSiRadioButton = findViewById(R.id.consumoTabacoSiRadioButton)
        consumoTabacoNoRadioButton = findViewById(R.id.consumoTabacoNoRadioButton)
        consumoTabacoAVecesRadioButton = findViewById(R.id.consumoTabacoAVecesRadioButton)
        futbolCheckBox = findViewById(R.id.futbolCheckBox)
        baloncestoCheckBox = findViewById(R.id.baloncestoCheckBox)
        natacionCheckBox = findViewById(R.id.natacionCheckBox)
        ciclismoCheckBox = findViewById(R.id.ciclismoCheckBox)
        tenisCheckBox = findViewById(R.id.tenisCheckBox)
        otrosDeportesCheckBox = findViewById(R.id.otrosDeportesCheckBox)
        lecturaCheckBox = findViewById(R.id.lecturaCheckBox)
        viajesCheckBox = findViewById(R.id.viajesCheckBox)
        cocinaCheckBox = findViewById(R.id.cocinaCheckBox)
        jardineriaCheckBox = findViewById(R.id.jardineriaCheckBox)
        manualidadesCheckBox = findViewById(R.id.manualidadesCheckBox)
        otrosHobbiesCheckBox = findViewById(R.id.otrosHobbiesCheckBox)
        dietaSpinner = findViewById(R.id.dietaSpinner)
        generoMusicalSpinner = findViewById(R.id.generoMusicalSpinner)
        nextButton = findViewById(R.id.nextButton)

        storageReference = FirebaseStorage.getInstance().reference
        firestoreDb = FirebaseFirestore.getInstance()
        auth = Firebase.auth

        changeImageButton.setOnClickListener { selectImage() }

        ArrayAdapter.createFromResource(
            this,
            R.array.nacionalidades,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            nacionalidadSpinner.adapter = adapter
            nacionalidadParejaSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.razas,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            razaSpinner1.adapter = adapter
            razaSpinner2.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.tipos_relacion,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            tipoRelacionSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.cantidades_hijos,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            hijosSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.ideologias_politicas,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            ideologiaPoliticaSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.dietas,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            dietaSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.generos_musicales,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            generoMusicalSpinner.adapter = adapter
        }

        nextButton.setOnClickListener {
            saveProfileData()
        }

        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null && data.data != null) {
                    selectedImageUri = data.data
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                        profileImageView.setImageBitmap(bitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        loadProfileData()
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResultLauncher.launch(intent)
    }

    private fun uploadImage(userId: String, onSuccess: (String) -> Unit) {
        selectedImageUri?.let { uri ->
            val fileReference = storageReference.child("profile_images/$userId/${UUID.randomUUID()}.jpg")
            fileReference.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    fileReference.downloadUrl.addOnSuccessListener { downloadUri ->
                        onSuccess(downloadUri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error uploading image", e)
                    Toast.makeText(this, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            onSuccess("") // No image selected, proceed without image URL
        }
    }

    private fun saveProfileData() {
        val userId = auth.currentUser?.uid ?: return

        val profileData = hashMapOf(
            "nombres" to nombresEditText.text.toString(),
            "apellido" to apellidoEditText.text.toString(),
            "nacionalidad" to nacionalidadSpinner.selectedItem.toString(),
            "raza1" to razaSpinner1.selectedItem.toString(),
            "nombresPareja" to nombresParejaEditText.text.toString(),
            "apellidoPareja" to apellidoParejaEditText.text.toString(),
            "nacionalidadPareja" to nacionalidadParejaSpinner.selectedItem.toString(),
            "raza2" to razaSpinner2.selectedItem.toString(),
            "tipoRelacion" to tipoRelacionSpinner.selectedItem.toString(),
            "hijos" to hijosSpinner.selectedItem.toString(),
            "perros" to perrosCheckBox.isChecked,
            "gatos" to gatosCheckBox.isChecked,
            "conejos" to conejosCheckBox.isChecked,
            "hamsters" to hamstersCheckBox.isChecked,
            "otrosMascotas" to otrosCheckBox.isChecked,
            "religion" to getSelectedReligion(),
            "ideologiaPolitica" to ideologiaPoliticaSpinner.selectedItem.toString(),
            "consumoAlcohol" to getSelectedConsumoAlcohol(),
            "consumoTabaco" to getSelectedConsumoTabaco(),
            "futbol" to futbolCheckBox.isChecked,
            "baloncesto" to baloncestoCheckBox.isChecked,
            "natacion" to natacionCheckBox.isChecked,
            "ciclismo" to ciclismoCheckBox.isChecked,
            "tenis" to tenisCheckBox.isChecked,
            "otrosDeportes" to otrosDeportesCheckBox.isChecked,
            "lectura" to lecturaCheckBox.isChecked,
            "viajes" to viajesCheckBox.isChecked,
            "cocina" to cocinaCheckBox.isChecked,
            "jardineria" to jardineriaCheckBox.isChecked,
            "manualidades" to manualidadesCheckBox.isChecked,
            "otrosHobbies" to otrosHobbiesCheckBox.isChecked,
            "dieta" to dietaSpinner.selectedItem.toString(),
            "generoMusical" to generoMusicalSpinner.selectedItem.toString()
        )

        uploadImage(userId) { imageUrl ->
            if (imageUrl.isNotEmpty()) {
                profileData["imageUrl"] = imageUrl
            }

            firestoreDb.collection("profiles").document(userId)
                .set(profileData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating profile", e)
                    Toast.makeText(this, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun loadProfileData() {
        val userId = auth.currentUser?.uid ?: return

        firestoreDb.collection("profiles").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val profileData = document.data
                    profileData?.let {
                        nombresEditText.setText(it["nombres"] as? String ?: "")
                        apellidoEditText.setText(it["apellido"] as? String ?: "")
                        setSpinnerValue(nacionalidadSpinner, it["nacionalidad"] as? String)
                        setSpinnerValue(razaSpinner1, it["raza1"] as? String)
                        nombresParejaEditText.setText(it["nombresPareja"] as? String ?: "")
                        apellidoParejaEditText.setText(it["apellidoPareja"] as? String ?: "")
                        setSpinnerValue(nacionalidadParejaSpinner, it["nacionalidadPareja"] as? String)
                        setSpinnerValue(razaSpinner2, it["raza2"] as? String)
                        setSpinnerValue(tipoRelacionSpinner, it["tipoRelacion"] as? String)
                        setSpinnerValue(hijosSpinner, it["hijos"] as? String)
                        perrosCheckBox.isChecked = it["perros"] as? Boolean ?: false
                        gatosCheckBox.isChecked = it["gatos"] as? Boolean ?: false
                        conejosCheckBox.isChecked = it["conejos"] as? Boolean ?: false
                        hamstersCheckBox.isChecked = it["hamsters"] as? Boolean ?: false
                        otrosCheckBox.isChecked = it["otrosMascotas"] as? Boolean ?: false
                        setSelectedReligion(it["religion"] as? String)
                        setSpinnerValue(ideologiaPoliticaSpinner, it["ideologiaPolitica"] as? String)
                        setSelectedConsumoAlcohol(it["consumoAlcohol"] as? String)
                        setSelectedConsumoTabaco(it["consumoTabaco"] as? String)
                        futbolCheckBox.isChecked = it["futbol"] as? Boolean ?: false
                        baloncestoCheckBox.isChecked = it["baloncesto"] as? Boolean ?: false
                        natacionCheckBox.isChecked = it["natacion"] as? Boolean ?: false
                        ciclismoCheckBox.isChecked = it["ciclismo"] as? Boolean ?: false
                        tenisCheckBox.isChecked = it["tenis"] as? Boolean ?: false
                        otrosDeportesCheckBox.isChecked = it["otrosDeportes"] as? Boolean ?: false
                        lecturaCheckBox.isChecked = it["lectura"] as? Boolean ?: false
                        viajesCheckBox.isChecked = it["viajes"] as? Boolean ?: false
                        cocinaCheckBox.isChecked = it["cocina"] as? Boolean ?: false
                        jardineriaCheckBox.isChecked = it["jardineria"] as? Boolean ?: false
                        manualidadesCheckBox.isChecked = it["manualidades"] as? Boolean ?: false
                        otrosHobbiesCheckBox.isChecked = it["otrosHobbies"] as? Boolean ?: false
                        setSpinnerValue(dietaSpinner, it["dieta"] as? String)
                        setSpinnerValue(generoMusicalSpinner, it["generoMusical"] as? String)

                        val imageUrl = it["imageUrl"] as? String
                        imageUrl?.let { url ->
                            Glide.with(this).load(url).into(profileImageView)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading profile data", e)
                Toast.makeText(this, "Error loading profile data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun setSpinnerValue(spinner: Spinner, value: String?) {
        if (value != null) {
            val adapter = spinner.adapter
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i).toString() == value) {
                    spinner.setSelection(i)
                    break
                }
            }
        }
    }

    private fun getSelectedReligion(): String {
        return when (religionRadioGroup.checkedRadioButtonId) {
            R.id.religionCatolicaRadioButton -> "Católica"
            R.id.religionCristianaRadioButton -> "Cristiana"
            R.id.religionJudiaRadioButton -> "Judía"
            R.id.religionMusulmanaRadioButton -> "Musulmana"
            R.id.religionHinduRadioButton -> "Hindú"
            R.id.religionNingunaRadioButton -> "Ninguna"
            else -> ""
        }
    }

    private fun setSelectedReligion(religion: String?) {
        when (religion) {
            "Católica" -> religionCatolicaRadioButton.isChecked = true
            "Cristiana" -> religionCristianaRadioButton.isChecked = true
            "Judía" -> religionJudiaRadioButton.isChecked = true
            "Musulmana" -> religionMusulmanaRadioButton.isChecked = true
            "Hindú" -> religionHinduRadioButton.isChecked = true
            "Ninguna" -> religionNingunaRadioButton.isChecked = true
        }
    }

    private fun getSelectedConsumoAlcohol(): String {
        return when (consumoAlcoholRadioGroup.checkedRadioButtonId) {
            R.id.consumoAlcoholSiRadioButton -> "Sí"
            R.id.consumoAlcoholNoRadioButton -> "No"
            R.id.consumoAlcoholAVecesRadioButton -> "A veces"
            else -> ""
        }
    }

    private fun setSelectedConsumoAlcohol(consumo: String?) {
        when (consumo) {
            "Sí" -> consumoAlcoholSiRadioButton.isChecked = true
            "No" -> consumoAlcoholNoRadioButton.isChecked = true
            "A veces" -> consumoAlcoholAVecesRadioButton.isChecked = true
        }
    }

    private fun getSelectedConsumoTabaco(): String {
        return when (consumoTabacoRadioGroup.checkedRadioButtonId) {
            R.id.consumoTabacoSiRadioButton -> "Sí"
            R.id.consumoTabacoNoRadioButton -> "No"
            R.id.consumoTabacoAVecesRadioButton -> "A veces"
            else -> ""
        }
    }

    private fun setSelectedConsumoTabaco(consumo: String?) {
        when (consumo) {
            "Sí" -> consumoTabacoSiRadioButton.isChecked = true
            "No" -> consumoTabacoNoRadioButton.isChecked = true
            "A veces" -> consumoTabacoAVecesRadioButton.isChecked = true
        }
    }


    companion object {
        private const val TAG = "EditProfileActivity"
    }
}