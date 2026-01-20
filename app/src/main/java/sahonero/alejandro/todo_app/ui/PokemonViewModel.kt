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
import sahonero.alejandro.todo_app.data.model.PokemonDetailResponse
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

    // --- DETALLES DE UN POKEMON (ID) ---
    private val _selectedPokemon = MutableStateFlow<PokemonItem?>(null)
    val selectedPokemon: StateFlow<PokemonItem?> = _selectedPokemon.asStateFlow()
    // --- DETALLES DE UN POKEMON (NAME) ---
    private val _pokemonDetail = MutableStateFlow<PokemonDetailResponse?>(null)
    val pokemonDetail: StateFlow<PokemonDetailResponse?> = _pokemonDetail.asStateFlow()

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

    fun loadPokemon(id: String){
        viewModelScope.launch {
            try {
                _selectedPokemon.value = repo.getPokemon(id)
            }catch (e: Exception){
                _selectedPokemon.value = null
            }
        }
    }

    suspend fun getPokemonDetails(name: String): PokemonDetailResponse? {
        return try {
            val detail = repo.getPokemonByName(name.lowercase())
            _pokemonDetail.value = detail
            detail
        } catch (e: Exception) {
            e.printStackTrace()
            null
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