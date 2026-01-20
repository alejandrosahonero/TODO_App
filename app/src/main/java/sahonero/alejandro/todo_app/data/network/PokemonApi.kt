package sahonero.alejandro.todo_app.data.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import sahonero.alejandro.todo_app.data.model.PokemonItem
import sahonero.alejandro.todo_app.data.model.PokemonListResponse

interface PokemonApi {
    @GET("pokemon/{id}")
    suspend fun getPokemon(@Path("id") id: String): PokemonItem
    @GET("pokemon")
    suspend fun getPokemons(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): PokemonListResponse
    @GET("pokemon")
    suspend fun getAllPokemons(
        @Query("limit") limit: Int = 1500
    ): PokemonListResponse
}