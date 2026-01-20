package sahonero.alejandro.todo_app.data.model

data class PokemonDetailResponse(
    val name: String,
    val types: List<TypeSlot>,
    val stats: List<StatSlot>,
    val sprites: Sprites
)

data class TypeSlot(
    val type: TypeInfo
)

data class TypeInfo(
    val name: String
)

data class StatSlot(
    val base_stat: Int,
    val stat: StatInfo
)

data class StatInfo(
    val name: String
)

data class Sprites(
    val front_default: String?
)