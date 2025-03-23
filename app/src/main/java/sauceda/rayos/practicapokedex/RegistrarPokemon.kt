package sauceda.rayos.practicapokedex

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class RegistrarPokemon : AppCompatActivity() {

    private val userRef = FirebaseDatabase
        .getInstance("https://pokedex-99553-default-rtdb.firebaseio.com")
        .getReference("pokemon")

    // Request Code para el intent de la galería
    private val PICK_IMAGE_REQUEST = 100

    // Views para capturar información
    private lateinit var edtNumber: EditText
    private lateinit var edtName: EditText
    private lateinit var btnSelectImage: Button
    private lateinit var btnRegister: Button

    // Guardamos la última URL obtenida de Cloudinary (o nula si no se ha subido)
    private var uploadedImageUrl: String? = null

    private val READ_EXTERNAL_STORAGE_PERMISSION = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrar_pokemon)

        // Referencias a las vistas
        edtNumber = findViewById(R.id.numero_pokemon)
        edtName = findViewById(R.id.nomb_pokemon)
        btnSelectImage = findViewById(R.id.btn_seleccionar_imagen)
        btnRegister = findViewById(R.id.registrar)

        // Listener para abrir la galería
        btnSelectImage.setOnClickListener {
            openGallery()
        }

        // Listener para "Registrar"
        btnRegister.setOnClickListener {
            saveMarkFromForm()
        }

        userRef.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
            }
        })


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun openGallery() {
        // 1. Verificamos si ya tenemos el permiso de lectura
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Ya tenemos el permiso, abrimos la galería
            launchGalleryIntent()
        } else {
            // No tenemos el permiso, lo solicitamos al usuario
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_PERMISSION
            )
        }
    }

    private fun launchGalleryIntent() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Este callback se llama cuando el usuario responde al diálogo de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION) {
            // Verificamos si aceptó el permiso
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El usuario concedió el permiso
                launchGalleryIntent()
            } else {
                // El usuario denegó el permiso
                // Muestra un mensaje explicando que no se puede continuar sin permiso
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                // Subir imagen a Cloudinary
                uploadImageToCloudinary(selectedImageUri)
            }
        }
    }

    private fun uploadImageToCloudinary(imageUri: Uri) {
        // Asegúrate de que Cloudinary ya fue inicializado en tu MainActivity.
        MediaManager.get().upload(imageUri)
            .option("folder", "pokedex") // carpeta en tu Cloudinary (opcional)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    // Mostrar algo: "Iniciando subida..."
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    // Puedes mostrar un ProgressBar si quieres
                }

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>) {
                    // Típicamente la URL HTTPS está en "secure_url"
                    val secureUrl = resultData["secure_url"] as String
                    uploadedImageUrl = secureUrl
                    // Podrías notificar al usuario: "Imagen subida con éxito"
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    // Maneja error
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    // Manejo reintento, raramente necesario
                }
            })
            .dispatch()
    }

    private fun saveMarkFromForm(){
        val number = edtNumber.text.toString()
        val name = edtName.text.toString()

        val pokemon = Pokemon(
            number,
            name,
            uploadedImageUrl ?: ""
        )

        // Guardar en Firebase
        userRef.push().setValue(pokemon)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Se guardó ok, podrías terminar la actividad o limpiar campos
                    // finish()
                } else {
                    // Manejar error
                }
            }
    }

}