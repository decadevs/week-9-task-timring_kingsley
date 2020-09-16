package com.example.pokemonapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.example.pokemonapp.data.Pokemon
import com.example.pokemonapp.requests.ServiceGenerator
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_pokemon_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


const val BASE_POKEMON_URL = "https://pokeres.bastionbot.org/images/pokemon"

class PokemonDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pokemon_details)

        //GET DATA FROM INTENT SENT FORM MAIN ACTIVITY
        val pokemonName = intent.getStringExtra("pokemonName")
        val id = intent.getStringExtra("id")

        /** MONITOR NETWORK AVAILABILITY */
        val netWorkLiveData = NetWorkLiveData(this)
        netWorkLiveData.observe(this, androidx.lifecycle.Observer {
            if (it){
                Toast.makeText(this@PokemonDetailsActivity, "network available", Toast.LENGTH_SHORT).show()
                //LAUNCH SERVICE AND FILL FIELDS
                launchService(pokemonName, id)

            } else {
                setVisibility()
            }
        })
    }

    fun fillFields(pokemonDetails: Pokemon, id:String) {
        var text = StringBuilder()
        pokemonName.text = pokemonDetails.name
        value_h.text = "${pokemonDetails.height}m"
        value_w.text = "${pokemonDetails.weight}Kg"

        //IMAGE
        Picasso.get().load("$BASE_POKEMON_URL/$id.png").into(pokemon_profile_image)

        //ABILITIES
        val abilities = pokemonDetails.abilities
        for(ability in abilities) {
            text.append("${ability.ability.name} \n\n")
        }
        value_ability.text = text

        //MOVES
        text.clear()
        val moves = pokemonDetails.moves
        for(move in moves) {
            text.append("${move.move.name} \n\n")
        }
        value_move.text = text

        //FORMS
        text.clear()
        val forms = pokemonDetails.forms
        for(form in forms) {
            text.append("${form.name} \n\n")
        }
        value_form.text = text
    }

    fun setVisibility() {
        //HANDLE VIEWS VISIBILITY
        pkm_logo.visibility = View.VISIBLE
        error_message.visibility = View.VISIBLE
        sad_pokemon.visibility = View.VISIBLE

        view.visibility = View.INVISIBLE
        pokemon_profile_image.visibility = View.INVISIBLE
        attribute_h_w.visibility = View.INVISIBLE
        values_h_w.visibility = View.INVISIBLE
        attrbutes_a_m_f.visibility = View.INVISIBLE
        values_a_m_f.visibility = View.INVISIBLE

        pokemon_details.setBackgroundColor(Color.WHITE)
    }

    fun resetVisibility() {
        pkm_logo.visibility = View.INVISIBLE
        error_message.visibility = View.INVISIBLE
        sad_pokemon.visibility = View.INVISIBLE

        view.visibility = View.VISIBLE
        pokemon_profile_image.visibility = View.VISIBLE
        attribute_h_w.visibility = View.VISIBLE
        values_h_w.visibility = View.VISIBLE
        attrbutes_a_m_f.visibility = View.VISIBLE
        values_a_m_f.visibility = View.VISIBLE

        pokemon_details.setBackgroundColor(Color.parseColor( "#ffcb05"))
    }

    fun launchService(pokemonName: String?, id: String?) {
        ServiceGenerator.getPokemonApi().getPokemonDetails("$pokemonName").enqueue(
            object : Callback<Pokemon> {
                override fun onFailure(call: Call<Pokemon>, t: Throwable) {
                    Log.d("FAILURE", "$t")
                    Toast.makeText(this@PokemonDetailsActivity, "Something went wrong!", Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<Pokemon>, response: Response<Pokemon>){
                    val pokemonDetails = response.body()
                    //SET FIELDS DATA
                    if(id!=null) fillFields(pokemonDetails!!, id)
                    resetVisibility()
                    Toast.makeText(this@PokemonDetailsActivity, "Network available", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

}

