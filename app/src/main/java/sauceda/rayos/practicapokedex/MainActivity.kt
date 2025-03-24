package sauceda.rayos.practicapokedex

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    // Aquí guardamos la referencia de la BD
    private val userRef = FirebaseDatabase
        .getInstance("https://pokedex-99553-default-rtdb.firebaseio.com")
        .getReference("pokemon")

    // Esta ListView es donde vamos a listar a los Pokémon ya registrados.
    private lateinit var listView: ListView

    // Botón para abrir la pantalla donde registramos nuevos Pokémon.
    private lateinit var btnRegistrar: Button

    // Esta de todos los Pokémon que se recuperen de Firebase.
    private val pokemonList = ArrayList<Pokemon>()

    // Adapter para "vincular" la lista de Pokémon con el ListView.
    private lateinit var adapter: PokemonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Iniciamos Cloudinary
        CloudinaryManager.init(this)

        // Ubicamos cada elemento de la interfaz en variables
        listView = findViewById(R.id.pokemon_list)
        btnRegistrar = findViewById(R.id.agregar_pokemon)

        // Creamos una instancia del adapter usando nuestro ArrayList
        adapter = PokemonAdapter(this, pokemonList)
        // Vinculamos el adapter con la ListView para que se vea la info
        listView.adapter = adapter

        // Cuando demos click en este botón, nos lanza a la Activity de "RegistrarPokemon"
        btnRegistrar.setOnClickListener {
            val intent = Intent(this, RegistrarPokemon::class.java)
            startActivity(intent)
        }

        // Aquí es donde escuchamos los datos en la ruta "pokemon" de Firebase.
        // Cada vez que haya algún cambio, se ejecuta onDataChange.
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Primero vaciamos la lista para volverla a llenar y evitar duplicados
                pokemonList.clear()

                // Recorremos  cada Pokémon guardado
                for (ds in snapshot.children) {
                    // Intentamos convertir ese nodo a un objeto Pokemon
                    val p = ds.getValue(Pokemon::class.java)
                    // Si sí, lo agregamos a la lista
                    if (p != null) {
                        pokemonList.add(p)
                    }
                }
                // Notificamos a nuestro adapter que hay datos nuevos; así se refresca la ListView
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Si hay algún fallo al leer la base de datos, se manejaría aquí
            }
        })

        // Este bloque es para ajustar la interfaz en dispositivos con
        // notches, barras superiores, etc., de forma que no se encime la UI.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Aquí definimos el adapter que se encarga de agregar cada Pokémon registrado dentro del ListView. .
    private class PokemonAdapter(
        private val context: Context,
        private val items: List<Pokemon>
    ) : BaseAdapter() {

        // Regresa cuántos elementos vamos a mostrar en la lista
        override fun getCount(): Int {
            return items.size
        }

        // Devuelve el Pokémon en la posición dada de la lista
        override fun getItem(position: Int): Pokemon {
            return items[position]
        }

        // Si no necesitamos IDs reales de la DB, podemos usar el índice como ID
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        // Esta función se invoca por cada elemento en la lista para inflar
        // la vista (usando el layout "pokemon_view.xml") y asignar datos.
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val holder: ViewHolder
            val view: View

            // Si no tenemos una vista reciclada, la creamos e inflamos "pokemon_view"
            if (convertView == null) {
                val inflater = LayoutInflater.from(context)
                view = inflater.inflate(R.layout.pokemon_view, parent, false)

                // Usamos un "holder" para no tener que llamar `findViewById` en cada elemento
                holder = ViewHolder()
                holder.numPokemon = view.findViewById(R.id.numero_pokemon)
                holder.namePokemon = view.findViewById(R.id.nombre_pokemon)
                holder.imgPokemon = view.findViewById(R.id.pokemon_img)

                // Guardamos el holder en el tag de la vista para reusarlo
                view.tag = holder
            } else {
                // Si la vista ya existe, la reutilizamos
                view = convertView
                holder = view.tag as ViewHolder
            }

            // Obtenemos el Pokémon que va en esta posición
            val currentItem = getItem(position)

            // Rellenamos los TextView con el número y nombre
            holder.numPokemon.text = currentItem.number
            holder.namePokemon.text = currentItem.name

            // Con Glide, traemos la imagen de la URL (desde Cloudinary) y la ponemos en el ImageView
            Glide.with(context)
                .load(currentItem.image)
                .into(holder.imgPokemon)

            // Retornamos la vista ya con los datos puestos
            return view
        }

        // El ViewHolder es para "cachear" las vistas del layout y evitar buscarlas cada vez.
        private class ViewHolder {
            lateinit var numPokemon: TextView
            lateinit var namePokemon: TextView
            lateinit var imgPokemon: ImageView
        }
    }
}