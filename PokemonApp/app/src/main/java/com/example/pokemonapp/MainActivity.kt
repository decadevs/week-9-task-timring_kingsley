package com.example.pokemonapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pokemonapp.requests.ServiceGenerator
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity: AppCompatActivity(), OnPokemonClickListener {

    lateinit var rvAdapter : RVAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /** MONITOR NETWORK AVAILABILITY */
        val netWorkLiveData = NetWorkLiveData(this)
        netWorkLiveData.observe(this, Observer {
            if(it) {
                //LAUNCH SERVICE AND POPULATE RECYCLER VIEW
                launchService()
                rvAdapter = RVAdapter(this, listOf())
                recycler_view.adapter = rvAdapter
                rvAdapter.setOnPokemonClickListener(this)
                recycler_view.setHasFixedSize(true)
                recycler_view.layoutManager = GridLayoutManager(this, 2)
                resetVisibility()
            } else {
                setVisibility()
            }
        })

    }

    fun launchService() {
        var list= mutableListOf<Result>()

       ServiceGenerator.getPokemonApi().getPokemon().enqueue(
           object : Callback<AllPokemons> {
               override fun onFailure(call: Call<AllPokemons>, t: Throwable) {
                   Log.d("FAILURE", "$t")
                   Toast.makeText(this@MainActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
               }

               override fun onResponse(call: Call<AllPokemons>, response: Response<AllPokemons>){
                   val allPokemons = response.body()!!.results
                   for(pokemon in allPokemons) {
                       list.add(pokemon)
                   }
                   rvAdapter.pokemonList = list.toList()
                   rvAdapter.notifyDataSetChanged()
               }
           }
       )
    }

    override fun onPokemonClick(position: Int, pokemon: AllPokemons) {
        val pokemonName = pokemon.results[position].name
        val intent = Intent(this, PokemonDetailsActivity::class.java)
        val url = pokemon.results[position].url.split("/")
        val id = url[url.size-2]
        intent.putExtra("pokemonName", pokemonName)
        intent.putExtra("id",id )
        Toast.makeText(this, pokemonName, Toast.LENGTH_SHORT).show()
        startActivity(intent)
    }

    fun setVisibility() {
        //HANDLE VIEWS VISIBILITY
        recycler_view.visibility = View.INVISIBLE
        error_message.visibility = View.VISIBLE
        sad_pokemon.visibility = View.VISIBLE
    }

    fun resetVisibility() {
        //HANDLE VIEWS VISIBILITY
        recycler_view.visibility = View.VISIBLE
        error_message.visibility = View.INVISIBLE
        sad_pokemon.visibility = View.INVISIBLE
    }

}