package com.example.kotlinandroidpokemonapi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinandroidpokemonapi.Data.Pokemon
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.all_pokemon_recycler_template.view.*

class PokemonTemplateAdapter(val pokemon: List<Pokemon>?, var action: PokemonClickInterface):RecyclerView.Adapter<PokemonTemplateAdapter.ViewHolder>() {
    /**
     * define the image url to fetch pokemon character images
     */
    var imageurl = "https://pokeres.bastionbot.org/images/pokemon/"

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var inflatedTemplate =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.all_pokemon_recycler_template, parent, false)
        return ViewHolder(inflatedTemplate)
    }

    /**
     * Bind the characters gotten to their corresponding view
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            tvPokemon.text=pokemon?.get(position)?.name.toString()
            //get the url so we can get the id of the pokemon and be able to display the corresponding image
            var url=pokemon?.get(position)?.url.toString()
            //use split method to get the id
            var splitUrl=url.split("/")
            var pokemonIdPos= splitUrl.size-2
            var pokemonId=splitUrl[pokemonIdPos]
            //use the id to return the appropriate image from the image json
            var imageUrl = "$imageurl$pokemonId.png"

            var imageView: ImageView = findViewById(R.id.imgPokemon)
            /**
             * use picasso library to load the image into the image view
             */
            Picasso.get().load(imageUrl).into(imageView);
            /**
             * set on click listener so that it listen to  any character from the recycler view when clicked
             */
            this.setOnClickListener {
                pokemon?.get(position)
                    ?.let { it1 -> action.onPokemonClick(it1, holder.adapterPosition) }
            }
        }
    }

    override fun getItemCount(): Int {
        return pokemon?.size!!
    }
}


/**
 * interface to handle click event for each pokemon character
 */
    interface PokemonClickInterface {
        fun onPokemonClick(pokemonChar: Pokemon, position: Int)
    }
