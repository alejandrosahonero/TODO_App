package sahonero.alejandro.todo_app

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tareas")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val priority: Int, // Alta(3), Media(2), Baja(1)
    var isCompleted: Boolean = false
)