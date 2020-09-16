package com.example.pokemonapp.requests

import com.example.pokemonapp.util.Constants
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class ServiceGenerator {
    companion object {
        //RETROFIT BUILDER
        private val retrofitBuilder = Retrofit.Builder()
            .baseUrl(Constants().BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())

        private val retrofit = retrofitBuilder.build()
        private val pokemonApi = retrofit.create(PokemonApi::class.java)

        fun getPokemonApi(): PokemonApi {
            return pokemonApi
        }
    }



}