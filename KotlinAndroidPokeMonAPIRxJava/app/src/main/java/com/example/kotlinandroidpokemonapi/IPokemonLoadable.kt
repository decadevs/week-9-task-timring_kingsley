package com.example.kotlinandroidpokemonapi

interface IPokemonLoadable {

    /**
     * Interface to load pokemon by number of pokemon entered
     */

    open fun getAllPokemon(){

    }

    open fun getAllPokemon(numberOfPokemonToLoad:Int){

    }
}