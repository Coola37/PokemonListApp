package com.yigitkula.pokemonlistapp.retrofit

import com.yigitkula.pokemonlistapp.BuildConfig
import com.yigitkula.pokemonlistapp.model.Pokedex
import com.yigitkula.pokemonlistapp.model.Pokemon
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class PokemonApiService {
    val BASE_URL = "https://raw.githubusercontent.com/Biuni/PokemonGO-Pokedex/master/"
    val api = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(PokemonApi::class.java)
    fun getData(): Single<Pokedex>{
        return api.getPokemons()
    }
}