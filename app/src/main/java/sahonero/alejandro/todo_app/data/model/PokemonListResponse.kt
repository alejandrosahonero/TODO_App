package sahonero.alejandro.todo_app.data.model

data class PokemonListResponse (
    val results: List<PokemonItem>
)

data class PokemonItem(
    val name: String,
    val url: String
){
    fun getId(): String{
        return url.trimEnd('/').split("/").last()
    }

    fun getImageUrl(): String {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${getId()}.png"
    }
}