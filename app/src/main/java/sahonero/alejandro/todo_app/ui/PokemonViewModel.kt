package sahonero.alejandro.todo_app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sahonero.alejandro.todo_app.data.model.PokemonItem
import sahonero.alejandro.todo_app.data.network.PokemonApi
import sahonero.alejandro.todo_app.data.repository.PokemonRepository

class PokemonViewModel: ViewModel() {
    private val api = Retrofit.Builder()
        .baseUrl("https://pokeapi.co/api/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PokemonApi::class.java)

    private val repo = PokemonRepository(api)

    // --- PAGINACION MANUAL ---
    companion object{
        private const val PAGE_SIZE = 20
    }
    private var currentOffset = 0
    private var isLoading = false
    private var canLoadMore = true

    // --- ONE POKEMON ---
    private val _selectedPokemon = MutableStateFlow<PokemonItem?>(null)
    val selectedPokemon: StateFlow<PokemonItem?> = _selectedPokemon.asStateFlow()

    // --- POKEMON NAMES ---
    private val _pokemonNames = MutableStateFlow<List<String>>(emptyList())
    val pokemonNames: StateFlow<List<String>> = _pokemonNames.asStateFlow()

    init {}

    private fun loadAllPokemonNames(){
        viewModelScope.launch {
            try {
                val response = repo.getAllPokemons()
                _pokemonNames.value = response.results.map { it.name }
            }catch (e: Exception){
                e.printStackTrace()
                _pokemonNames.value = emptyList()
            }
        }
    }

    private fun loadMorePokemons() {
        if(isLoading || !canLoadMore) return

        viewModelScope.launch {
            isLoading = true
            try {
                val response = repo.getPokemons(offset = currentOffset, limit = PAGE_SIZE)
                val newNames = response.results.map { it.name }

                if(newNames.isNotEmpty()){
                    _pokemonNames.update { currentList -> currentList + newNames }
                    currentOffset += PAGE_SIZE
                }else{
                    canLoadMore = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _pokemonNames.value = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    fun loadPokemon(id: String){
        viewModelScope.launch {
            try {
                _selectedPokemon.value = repo.getPokemon(id)
            }catch (e: Exception){
                _selectedPokemon.value = null
            }
        }
    }

    suspend fun isValidPokemonName(name: String): Boolean {
        if(name.isBlank()) return false

        return try {
            repo.getPokemon(name.lowercase())
            true
        }catch (e: Exception){
            false
        }
    }
}