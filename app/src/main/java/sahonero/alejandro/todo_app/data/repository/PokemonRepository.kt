package sahonero.alejandro.todo_app.data.repository

import sahonero.alejandro.todo_app.data.model.PokemonListResponse
import sahonero.alejandro.todo_app.data.network.PokemonApi

class PokemonRepository(private val api: PokemonApi) {
    suspend fun getPokemon(id: String) = api.getPokemon(id)
    suspend fun getPokemonByName(name: String) = api.getPokemonByName(name)
    suspend fun getAllPokemons(): PokemonListResponse = api.getAllPokemons()
}