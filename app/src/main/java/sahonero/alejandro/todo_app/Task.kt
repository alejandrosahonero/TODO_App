package sahonero.alejandro.todo_app

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val priority: Int, // Alta(3), Media(2), Baja(1)
    var isCompleted: Boolean = false
)