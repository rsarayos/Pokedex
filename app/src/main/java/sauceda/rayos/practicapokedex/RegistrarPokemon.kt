package sauceda.rayos.practicapokedex

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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

    // Apuntamos a nuestra BD de Firebase en la parte de "pokemon"
    private val userRef = FirebaseDatabase
        .getInstance("https://pokedex-99553-default-rtdb.firebaseio.com")
        .getReference("pokemon")

    // Código que nos sirve para identificar la actividad cuando abrimos la galería.
    private val PICK_IMAGE_REQUEST = 100

    // Elementos del layout que estaremos usando para los datos y la UI.
    private lateinit var edtNumber: EditText
    private lateinit var edtName: EditText
    private lateinit var btnSelectImage: Button
    private lateinit var btnRegister: Button
    private lateinit var tvMensajeImagen: TextView

    // En esta variable guardaremos la URL final que Cloudinary nos devuelva.
    private var uploadedImageUrl: String? = null

    // Código para la solicitud de permiso de lectura del almacenamiento.
    private val READ_EXTERNAL_STORAGE_PERMISSION = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ajusta la interfaz para pantallas edge-to-edge
        enableEdgeToEdge()
        // Vincula este Activity con el layout
        setContentView(R.layout.activity_registrar_pokemon)

        // Aquí enlazamos los componentes de la interfaz con nuestras variables
        edtNumber = findViewById(R.id.numero_pokemon)
        edtName = findViewById(R.id.nomb_pokemon)
        btnSelectImage = findViewById(R.id.btn_seleccionar_imagen)
        btnRegister = findViewById(R.id.registrar)
        tvMensajeImagen = findViewById(R.id.mensaje_imagen)

        // Cuando se clickea el botón para seleccionar imagen, llamamos a openGallery()
        btnSelectImage.setOnClickListener {
            openGallery()
        }

        // Cuando se clickea en "Registrar", mandamos a guardar la info en Firebase
        btnRegister.setOnClickListener {
            saveMarkFromForm()
        }

        // Escuchamos cambios en la rama "pokemon", aunque aquí no hacemos nada concreto
        userRef.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {}
        })

        // Manejo opcional de insets (barras superiores, laterales, etc.)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Esta función revisa si ya tenemos permiso para leer la galería;
    // si no, lo solicita al usuario.
    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Ya tenemos permiso, entonces abrimos la galería
            launchGalleryIntent()
        } else {
            // Aún no, así que lo pedimos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_PERMISSION
            )
        }
    }

    // Aquí lanzamos un intent que filtra solo imágenes en la galería
    // y mostramos un pequeño mensaje en el TextView.
    private fun launchGalleryIntent() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
        tvMensajeImagen.text = "¡Imagen subida con éxito!"
    }

    // Callback que se llama cuando el usuario acepta o deniega permisos.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El usuario concedió el permiso, podemos abrir la galería
                launchGalleryIntent()
            } else {
                // El usuario rechazó el permiso, podríamos avisarle que sin eso no puede seleccionar imágenes
            }
        }
    }

    // Cuando volvemos de la galería (después de que el usuario escogió una imagen),
    // obtenemos el Uri y subimos a Cloudinary.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                uploadImageToCloudinary(selectedImageUri)
            }
        }
    }

    // Esta función se encarga de subir la imagen seleccionada a Cloudinary.
    // Al completarse, se guarda la URL en "uploadedImageUrl".
    private fun uploadImageToCloudinary(imageUri: Uri) {
        MediaManager.get().upload(imageUri)
            .option("folder", "pokedex") // puedes cambiar la carpeta si quieres
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    // Si deseas mostrar un mensaje "subiendo...", lo harías aquí
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    // Podrías manejar una barra de progreso
                }

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>) {
                    // En secure_url normalmente viene la URL completa para ver la imagen
                    val secureUrl = resultData["secure_url"] as String
                    uploadedImageUrl = secureUrl
                    // Ya quedó guardada la URL en nuestro campo, lista para guardarse en Firebase
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    // Aquí atrapas si algo falla al subir
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    // Solo si la subida se reprograma
                }
            })
            .dispatch()
    }

    // Toma el número y nombre que el usuario ingresó, y la URL
    // (si es que ya se subió la imagen). Luego manda a Firebase.
    private fun saveMarkFromForm() {
        val number = edtNumber.text.toString()
        val name = edtName.text.toString()

        // Si uploadedImageUrl es null, enviamos un "" para evitar valores nulos
        val pokemon = Pokemon(
            number,
            name,
            uploadedImageUrl ?: ""
        )

        // Hacemos push en la referencia de Firebase para crear un nuevo nodo
        userRef.push().setValue(pokemon)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // No hubo problemas, regresamos a la pantalla anterior
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // Manejo de error al guardar
                }
            }
    }
}