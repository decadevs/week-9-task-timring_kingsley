package com.example.kotlinandroidpokemonapi.Data

import android.hardware.camera2.params.Capability


/**
 * Dataclass for the json fields in the api to map to
 */
data class Pokemon(val height:Int,
                   val name:String,
                   val weight:Int,
                   val url:String,
                   val abilities:List<Ability>,
                   val forms:List<Forms>,
                   val moves:List<Moves>,
                   val sprites:Sprite )

data class Ability(
    val ability:AbilityX
)

data class AbilityX(
    val name:String
)

data class Forms(
    val name:String
)


data class Moves(
    val move:MoveStructure)


data class MoveStructure(
    val name:String
)

data class Sprite(
    val front_default:String,
    val other:OtherPic
)

data class OtherPic(
    val dream_word:DreamWorldPic
)

data class DreamWorldPic(
    val front_default: String
)

data class PokeMonList(val results:List<Pokemon>)

