package sahonero.alejandro.todo_app.data.repository

import sahonero.alejandro.todo_app.data.model.PokemonListResponse
import sahonero.alejandro.todo_app.data.network.PokemonApi

class PokemonRepository(private val api: PokemonApi) {
    suspend fun getPokemon(id: String) = api.getPokemon(id)
    suspend fun getPokemons(offset: Int, limit: Int): PokemonListResponse {
        return api.getPokemons(offset = offset, limit = limit)
    }
    suspend fun getAllPokemons(): PokemonListResponse {
        return api.getAllPokemons()
    }
}