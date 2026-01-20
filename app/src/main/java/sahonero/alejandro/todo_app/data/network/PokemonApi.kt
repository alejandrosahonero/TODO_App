package sahonero.alejandro.todo_app.data.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import sahonero.alejandro.todo_app.data.model.PokemonDetailResponse
import sahonero.alejandro.todo_app.data.model.PokemonItem
import sahonero.alejandro.todo_app.data.model.PokemonListResponse

interface PokemonApi {
    @GET("pokemon/{id}")
    suspend fun getPokemon(
        @Path("id") id: String
    ): PokemonItem

    @GET("pokemon/{name}")
    suspend fun getPokemonByName(
        @Path("name") name: String
    ): PokemonDetailResponse

    @GET("pokemon")
    suspend fun getAllPokemons(
        @Query("limit") limit: Int = 1500
    ): PokemonListResponse
}