package com.yigitkula.pokemonlistapp.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.yigitkula.pokemonlistapp.model.Pokedex
import com.yigitkula.pokemonlistapp.model.Pokemon
import com.yigitkula.pokemonlistapp.retrofit.PokemonApiService
import com.yigitkula.pokemonlistapp.retrofit.PokemonDatabase
import com.yigitkula.pokemonlistapp.util.CustomSharedPreferences
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch


class ListViewModel(application: Application) : BaseViewModel(application){
    private val pokemonApiService = PokemonApiService()
    private val disposable = CompositeDisposable()
    private var customPreferences = CustomSharedPreferences(getApplication())
    private var refrehTime = 10 * 60 * 1000 * 1000 * 1000L // -> 10 minutes in nanoseconds

    val pokemons = MutableLiveData<List<Pokemon>>()
    val pokemonError = MutableLiveData<Boolean>()
    val pokemonLoading = MutableLiveData<Boolean>()

    fun refreshData(){
        val updateTime = customPreferences.getTime()
        if(updateTime != null && updateTime != 0L && System.nanoTime() - updateTime < refrehTime){
            getDataFromSQLite()
        }else{
            getDataFromAPI()
        }
    }
    fun refreshFromAPI(){
        getDataFromAPI()
    }
    private fun getDataFromSQLite() {
        pokemonLoading.value=true
        launch {
            val pokemons = PokemonDatabase(getApplication()).pokemonDao().getAllPokemons()
            showPokemons(pokemons)
            Toast.makeText(getApplication(),"Pokemons Data From SQLite",Toast.LENGTH_LONG).show()
        }
    }
    fun getDataFromAPI(){
        pokemonLoading.value=true
        pokemonError.value=false

       disposable.add(
        pokemonApiService.getData()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object: DisposableSingleObserver<Pokedex>(){
                override fun onSuccess(t: Pokedex) {
                    storeInSQLite(t.pokemon!!)
                    Toast.makeText(getApplication(),"Pokemons Data From API",Toast.LENGTH_LONG).show()

                }

                override fun onError(e: Throwable) {
                    pokemonError.value = true
                    pokemonLoading.value = false
                    e.printStackTrace()
                }
            })
          )}

    private fun showPokemons(pokemonList: List<Pokemon>) {
        pokemons.value = pokemonList
        pokemonError.value = false
        pokemonLoading.value = false
    }
    private fun storeInSQLite(list: List<Pokemon>) {
        launch {
            val dao = PokemonDatabase(getApplication()).pokemonDao()
            dao.deleteAllPokemons()
            val listLong = dao.insertAll(*list.toTypedArray())  // list -> individual
            var i = 0
            while(i<list.size){
                list[i].uuid = listLong[i].toInt()
                i = i+1
            }
            showPokemons(list)
        }
        customPreferences.saveTime(System.nanoTime())
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}



