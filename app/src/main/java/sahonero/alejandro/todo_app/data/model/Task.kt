package sahonero.alejandro.todo_app.data.model

data class Task(
    val id: String = "",
    val description: String = "",
    val priority: Int = 0, // Alta(3), Media(2), Baja(1)
    val expirationDate: String = "Never",
    var completed: Boolean = false,

    val pokemon: String = "",
    val tipos: List<String> = emptyList(),
    val hp: String = "0",
    val ataque: String = "0",
    val defensa: String = "0",
    val velocidad: String = "0",
    val imagen: String = "",

    val userId: String = ""
)