package com.example.pokemonapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.pokemon_profile.view.*

const val BASE_IMAGE_URL = "https://pokeres.bastionbot.org/images/pokemon"

class RVAdapter(var clickListener: OnPokemonClickListener, var pokemonList: List<Result>): RecyclerView.Adapter<RVAdapter.MyViewHolder>() {

    //ADD RECYCLER VIEW LISTENER
    fun setOnPokemonClickListener(listener: OnPokemonClickListener) {
        clickListener = listener
    }

    //IMPLEMENT MEMBERS
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.pokemon_profile, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return pokemonList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentPokemon = pokemonList[position]
//        Glide.with(context)load(currentPokemon.img).into(holder.pokemonImage)
        holder.pokemonName.text = currentPokemon.name

        val pokemonUrl = currentPokemon.url.split("/")
        val id = pokemonUrl[pokemonUrl.size - 2]
        Picasso.get().load("$BASE_IMAGE_URL/$id.png").into(holder.pokemonImage)
    }

    //VIEW-HOLDER
    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var pokemonImage: ImageView
        var pokemonName: TextView

        init {
            pokemonImage = itemView.pokemon_pic
            pokemonName = itemView.pokemon_name
            itemView.setOnClickListener{
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION) {
                    clickListener.onPokemonClick(position, pokemon = AllPokemons(itemCount,
                        "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20",
                        null, pokemonList))
                }
            }
        }
    }
}

interface OnPokemonClickListener {
    fun onPokemonClick(position: Int, pokemon: AllPokemons)
}