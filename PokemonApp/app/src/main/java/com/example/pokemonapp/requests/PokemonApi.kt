package com.example.pokemonapp.requests

import com.example.pokemonapp.AllPokemons
import com.example.pokemonapp.data.Pokemon
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface PokemonApi {
    //ACCESS BASE API LEVEL
    @GET("pokemon?limit=200&offset=0")
    fun getPokemon(): Call<AllPokemons>

    //ACCESS POKEMON DETAILS
    @GET("pokemon/{name}")
    fun getPokemonDetails(@Path("name") key: String ): Call<Pokemon>

    @GET
    fun getPokemons(@Url url: String): Call<Pokemon>
}