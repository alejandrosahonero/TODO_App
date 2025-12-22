package sahonero.alejandro.todo_app

data class Task(
    val id: String = "",
    val description: String = "",
    val priority: Int = 0, // Alta(3), Media(2), Baja(1)
    val expirationDate: String = "Never",
    var completed: Boolean = false,
    val userId: String = ""
)