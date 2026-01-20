package sahonero.alejandro.todo_app.data.model

data class Task(
    val id: String = "",
    val description: String = "",
    val priority: Int = 0, // Alta(3), Media(2), Baja(1)
    val expirationDate: String = "Never",
    var completed: Boolean = false,

    val pokemon: String = "",
    val tipos: List<String> = emptyList(),
    val hp: String,
    val ataque: String,
    val defensa: String,
    val velocidad: String,
    val imagen: String = "",

    val userId: String = ""
)