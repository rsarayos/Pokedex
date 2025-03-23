package sauceda.rayos.practicapokedex

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class RegistrarPokemon : AppCompatActivity() {

    private val userRef = FirebaseDatabase.getInstance("https://pokedex-99553-default-rtdb.firebaseio.com").getReference("pokemon")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrar_pokemon)

        var btnSave: Button = findViewById(R.id.registrar) as Button
        btnSave.setOnClickListener { saveMarkFromForm() }

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

    private fun saveMarkFromForm(){
        var number: EditText = findViewById(R.id.numero_pokemon) as EditText
        var name: EditText = findViewById(R.id.nomb_pokemon) as EditText
        var image: EditText = findViewById(R.id.imagen_pokemon) as EditText

        val pokemon = Pokemon(
            number.text.toString(),
            name.text.toString(),
            image.text.toString()
        )

        userRef.push().setValue(pokemon)
    }

}