package com.example.kotlinandroidpokemonapi.Service

import com.example.kotlinandroidpokemonapi.APIConstant


/**
 * Pokemon Factory that uses the retrofit factory class to consume the REST api
 */
object PokemonApiFactory {
    val pokeMonApi:PokemonApi=RetrofitFactory.retrofit(APIConstant.POKEMON_BASE_URL).create(PokemonApi::class.java)
}

