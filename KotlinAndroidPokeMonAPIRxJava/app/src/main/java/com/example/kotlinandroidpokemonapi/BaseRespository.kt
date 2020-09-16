package com.example.kotlinandroidpokemonapi

import retrofit2.Response
import android.util.Log
import java.io.IOException

/**
 * Using safe api call to handle calls to the network by suspending funtions
 */

open class BaseRepository{
    suspend fun <T:Any> safeApiCall(call:suspend ()->Response<T>,errorMessage:String):T?{
        val result:Result<T> = safeApiResult(call,errorMessage)

        var data:T?=null

        when(result){
            is Result.Success ->
                data=result.data
            is Result.Error-> {
                Log.d("1. DataRepository", "$errorMessage & Exception - ${result.exception}")
            }
        }
        return data
    }

    private suspend fun <T:Any> safeApiResult(call:suspend ()->Response<T>,errorMessage:String):Result<T>{
        val response=call.invoke()
        if(response.isSuccessful) Result.Success(response.body()!!)

        return Result.Error(IOException("Error Occured during getting safe API Result, Custom Error -$errorMessage"))
    }
}