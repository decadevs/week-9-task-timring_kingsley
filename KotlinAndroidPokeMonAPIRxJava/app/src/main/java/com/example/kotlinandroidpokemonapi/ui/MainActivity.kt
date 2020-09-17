package com.example.kotlinandroidpokemonapi.ui

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.transition.TransitionManager
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinandroidpokemonapi.Data.Pokemon
import com.example.kotlinandroidpokemonapi.adapter.PokemonClickInterface
import com.example.kotlinandroidpokemonapi.adapter.PokemonTemplateAdapter
import com.example.kotlinandroidpokemonapi.R
import com.example.kotlinandroidpokemonapi.Service.PokemonApiFactory
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), PokemonClickInterface {
    private val pokemonService = PokemonApiFactory.pokeMonApi
    lateinit var editText:EditText
    private val disposables = CompositeDisposable()

    var imageurl = "https://pokeres.bastionbot.org/images/pokemon/"
    private var isDetailLayout=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        /**
         * When the program launches, get all pokemon's default number of characters i.e 20
         */
        fetchPokemon()

        /**
         * when the button is clicked, get all pokemon by entering the number of pokemon to get
         */

        /**
         * set recycler view layout
         */
        editText=findViewById(R.id.etnumOfPokemonToLoad)

            rvPokemon.layoutManager = GridLayoutManager(
                this@MainActivity,
                3,
                RecyclerView.VERTICAL,
                false
            )

        /**
         * Make edit text observable for change, listen to change after 300 milliseconds
         * this way we avoid calling server every milliseconds, we can have more number entered before
         * call to server
         */
        RxTextView.textChangeEvents(editText)
            .skipInitialValue()
            .debounce(300,TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
                //subscribe the consumer to the observable view's disposable or stream
            .subscribeWith(getPokemonLimit())?.let { disposables.add(it) }


    }



    private fun fetchPokemon() {
        rvPokemon.visibility=View.INVISIBLE
        imgError.visibility=View.VISIBLE
            disposables.add(pokemonService.getPokemonCharacter()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry()
                .subscribe({ query ->
                    showResult(query.results)
                },
                    { Throwable ->
                        Toast.makeText(
                            this,
                            "Could not fetch data ${Throwable.message}",
                            Toast.LENGTH_LONG
                        ).show()

                    })
            )

    }

    /**
     * consumer observes change to text entry and Get the pokemon limit
     */
    private fun getPokemonLimit(): DisposableObserver<TextViewTextChangeEvent?>? {
        return object : DisposableObserver<TextViewTextChangeEvent?>() {
            override fun onNext(textViewTextChangeEvent: TextViewTextChangeEvent) {
                try {
                    getAllPokemon(textViewTextChangeEvent.text().toString().toInt())
                }
                catch (e:Exception){
                    //if there is error on input, retrieve all default pokemon by calling fetch pokemon
                    return fetchPokemon()
                }
            }
            override fun onError(e: Throwable) {
                Toast.makeText(this@MainActivity,"Error Occured while trying to get pokemon limit",Toast.LENGTH_LONG).show()
            }

            override fun onComplete() {}
        }
    }


    /**
     * get all pokemon by adding the result from network call to disposable stream and subscribing the consumer to the stream
     */

    private fun getAllPokemon(numberOfPokemonToLoad: Int){
        disposables.add(
            pokemonService.getPokemonLimit(numberOfPokemonToLoad)
                    //subscribe call to network on io thread
                .subscribeOn(Schedulers.io())
                    //return the execution to main thread after network call so as to display to UI
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ query ->
                    showResult(query.results)
                },
                    { Throwable ->
                        Toast.makeText(
                            this,
                            "Could not fetch data ${Throwable.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    })
        )
    }

    /**
    *show the result on the recyclerview on every next stream in the disposable
     */

    private fun showResult(pokemonList: List<Pokemon>){
        imgError.visibility=View.INVISIBLE
        rvPokemon.visibility=View.VISIBLE
        var adapter= PokemonTemplateAdapter(pokemonList, this)
        rvPokemon.adapter=adapter
    }



    /**
     * When the poke mon is clicked, display the detail
     */
    override fun onPokemonClick(pokemon: Pokemon, position: Int) {
        GlobalScope.launch(Dispatchers.Main){
            val pokemonDetailService = pokemonService.getPokemonDetail(pokemon.name)
            val detailResponse= pokemonDetailService.await()
            val pokemonDetailBody=detailResponse.body()

            if (detailResponse != null) {
                if(detailResponse.isSuccessful){
                    etnumOfPokemonToLoad.visibility=View.GONE
                    rvPokemon.visibility=View.GONE
                    if(isDetailLayout){
                        swapFrames(R.layout.activity_main)
                    }
                    else{
                        if (pokemonDetailBody != null) {
                            tvpokemonName.text=pokemonDetailBody.name
                            var splitUrl=pokemon.url.split("/")
                            var pokemonIdPos= splitUrl.size-2
                            var pokemonId=splitUrl[pokemonIdPos]
                            //use the id to return the appropriate image from the image json
                            var imageUrl = "$imageurl$pokemonId.png"

                            var imageView: ImageView = findViewById(R.id.imageViewHeader)
                            Picasso.get().load(imageUrl).into(imageView);

                            //get sprite
                            var frontImage=pokemonDetailBody.sprites.front_default
                            var imageSmall=findViewById<ImageView>(R.id.imageSmall)
                            Picasso.get().load(frontImage).into(imageSmall)

                            //get abilities
                            var string = ""
                            string += pokemonDetailBody.abilities.joinToString(", ") {
                                it.ability.name
                            }
                            tvAbilities.text = string
                            tvAbilities.movementMethod = ScrollingMovementMethod()

                            //get moves
                            var stringMove = ""
                            stringMove += pokemonDetailBody.moves.joinToString(", ") {
                                it.move.name
                            }
                            tvMove.text = stringMove
                            tvMove.movementMethod = ScrollingMovementMethod()

                            //get height and weight
                            tvHeight.text= pokemonDetailBody.height.toString()
                            tvWeight.text=pokemonDetailBody.weight.toString()


                        }
                        swapFrames(R.layout.pokemon_detail_slidein_template)

                    }

//                    pokemonDetailBody?.let { Log.d("pokemonDetail", it.weight.toString()) }
                }
            }
        }
    }

    /**
     * When a pokemon is clicked, slide in the detail of the pokemon using Constraint set animation
     */
    private fun swapFrames(layoutId: Int){
        imgError.visibility=View.GONE
        val constraintSet=ConstraintSet()
        constraintSet.clone(this, layoutId)
        TransitionManager.beginDelayedTransition(ConstraintLayout)
        constraintSet.applyTo(ConstraintLayout)
        isDetailLayout=!isDetailLayout
    }

}