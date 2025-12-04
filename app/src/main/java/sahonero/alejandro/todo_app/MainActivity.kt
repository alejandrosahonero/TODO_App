package sahonero.alejandro.todo_app

import android.content.pm.PackageManager
import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Build
import kotlinx.coroutines.delay
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sahonero.alejandro.todo_app.ui.theme.TODOAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TODOAppTheme(
                darkTheme = TodoPreferences.getTheme(LocalContext.current).collectAsState(initial = isSystemInDarkTheme()).value
            ){
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "login"
    ){
        composable ("login"){
            Login(
                onLogin = { nombre, alias ->
                    navController.navigate("tasks/$nombre/$alias") }
            )
        }
        composable(
            route = "tasks/{nombre}/{alias}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("alias") { type = NavType.StringType }
            )
        ){ backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre")!!
            val alias = backStackEntry.arguments?.getString("alias")!!

            setAlias(alias)

            Tasks(
                nombre = nombre,
                alias = alias
            )
        }
    }
}

@Composable
fun Login(onLogin: (String, String) -> Unit){
    var nombre by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }

    var showErrorNombre by remember { mutableStateOf(false) }
    var showErrorAlias by remember { mutableStateOf(false) }

    Surface( Modifier.fillMaxSize() ) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)

            ) {
                Column(
                    Modifier.padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // --- APP ICON ---
                    Icon(
                        painter = painterResource(R.drawable.todo_logo),
                        contentDescription = "Logo de la app",
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(20.dp))
                    // --- APP TITLE ---
                    Text(
                        text = "TO-DO App",
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    // --- FIELDS ---
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it; showErrorNombre = false },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = showErrorNombre
                    )
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(
                        value = alias,
                        onValueChange = { alias = it; showErrorAlias = false },
                        label = { Text("Alias") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = showErrorAlias
                    )
                    if(showErrorNombre || showErrorAlias){
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Debe completar ambos campos",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 15.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    // --- LOGIN BUTTON ---
                    Button(
                        onClick = {
                            if (nombre.isBlank() && alias.isBlank()) {
                                showErrorNombre = true
                                showErrorAlias = true
                            } else if (nombre.isBlank()) {
                                showErrorNombre = true
                            } else if (alias.isBlank()) {
                                showErrorAlias = true
                            } else {
                                showErrorNombre = false
                                showErrorAlias = false
                                onLogin(nombre.trim(), alias.trim())
                            }
                        }
                    ) {
                        Text("Continuar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tasks(nombre: String, alias: String){
    // --- PREFERENCES ---
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selectedColor = TodoPreferences.getColor(context).collectAsState(initial = "Default")
    val selectedTheme = TodoPreferences.getTheme(context).collectAsState(initial = true)
    // --- DATABASE ---
    val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java, "todoapp-database"
    ).build()
    val taskDao = db.taskDao()
    // --- TASKS ---
    var showAddTask by remember { mutableStateOf(false) }
    val listaTareasState = taskDao.getTasks().collectAsState(initial = emptyList())
    val listaTareas by listaTareasState
    // --- SEARCH ---
    var sortByPriority by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredTasks by remember(searchQuery, listaTareasState.value, sortByPriority) {
        derivedStateOf {
            // Obtenemos el Flow
            val currentList = listaTareas

            // Filtramos por búsqueda
            val searchResult = if (searchQuery.isBlank()) {
                currentList
            } else {
                currentList.filter {
                    it.description.contains(searchQuery, ignoreCase = true)
                }
            }

            // Luego ordenamos si hay un shake
            if(sortByPriority)
                searchResult.sortedByDescending { it.priority }
            else
                searchResult
        }
    }
    // --- NOTIFICATIONS ---
    var lastTaskTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    // For API 33+
    val permissionLauncher = rememberLauncherForActivityResult( contract = ActivityResultContracts.RequestPermission() ){}

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTask = true }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir tareas")
            }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            // --- TOP APP BAR ---
            Row(Modifier.fillMaxWidth()) {
                // --- WELCOME ---
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "¡Qué tal $nombre!",
                    )
                    Text(
                        text = if (listaTareas.isEmpty()) "¿NO HAY NADA QUE HACER?" else if (listaTareas.size < 4) "¡TE QUEDA POCO!" else "¡HAY MUCHO POR HACER!",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 40.sp
                    )
                }
                // --- MENU ---
                VerticalMenu(
                    selectedTheme = selectedTheme,
                    selectedColor = selectedColor,
                    context = context,
                    scope = scope
                )
            }
            Spacer(Modifier.height(10.dp))
            // --- SEARCH BAR ---
            SearchBar(
                searchQuery = searchQuery,
                onValueChange = { newValue -> searchQuery = newValue }
            )
            Spacer(Modifier.height(10.dp))
            // --- TASKS LIST ---
            TaskList(
                filteredTasks = filteredTasks,
                listaTareas = listaTareas,
                selectedColor = selectedColor,
                onCheckedChange = { tareaAEditar, nuevoEstado ->
                    scope.launch {
                        taskDao.updateTask(tareaAEditar.copy(isCompleted = nuevoEstado))
                    }
                },
                onDeleteTask = { tareaABorrar ->
                    // Borramos la tarea directamente de la BD
                    scope.launch {
                        taskDao.deleteTask(tareaABorrar)
                    }
                }
            )
        }
    }
    // --- ADD TASK DIALOG ---
    if(showAddTask){
        AddTaskDialog(
            onDismiss = { showAddTask = false },
            onConfirm = { nuevaTarea, prioridad ->
                if (nuevaTarea.isNotBlank()) {
                    // Insertamos una nueva tarea directamente a la BD
                    scope.launch {
                        taskDao.insertTask(
                            Task(
                                description = nuevaTarea,
                                priority = prioridad
                            )
                        )
                    }
                    // Reiniciamos el contador cada que creamos una tarea
                    lastTaskTime = System.currentTimeMillis()
                }
                showAddTask = false
            },
            alias = alias
        )
    }
    // --- PERMISSION CHECK ---
    LaunchedEffect(Unit) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        createNotificationsChannel(context)
    }
    // --- INACTIVITY TIMER ---
    LaunchedEffect(lastTaskTime) {
        // Cancelar el anterior y empezar un nuevo job
        delay(3 * 60 * 1000L)
        sendInactivityNotification(context)
    }
    // --- SHAKE TO SORT ---
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val shakeDetector = ShakeDetector {
            // Alternamos el orden
            sortByPriority = !sortByPriority
            Toast.makeText(context, "¡Lista ordenada por prioridad!", Toast.LENGTH_LONG).apply { show() }
        }

        // --- SENSOR LISTENER ---
        sensorManager.registerListener(
            shakeDetector,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )
        onDispose {
            sensorManager.unregisterListener(shakeDetector)
        }
    }
}

@Composable
fun VerticalMenu(selectedTheme: State<Boolean>, selectedColor: State<String>, context: Context, scope: CoroutineScope){
    var expanded by remember { mutableStateOf(false) }
    var showPreferencesDialog by remember { mutableStateOf(false) }
    // --- MENU ---
    IconButton(
        onClick = { expanded = true },
        modifier = Modifier.size(30.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = "Menu"
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Preferencias"
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Preferencias")
                    }
                },
                onClick = {
                    showPreferencesDialog = true
                    expanded = false
                }
            )
        }
    }
    if (showPreferencesDialog){
        PreferencesDialog(
            selectedTheme = selectedTheme,
            selectedColor = selectedColor,
            context = context,
            scope = scope,
            onDismiss = { showPreferencesDialog = false }
        )
    }
}

@Composable
fun SearchBar(searchQuery: String, onValueChange: (String) -> Unit){
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onValueChange,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar"
            )
        },
        placeholder = { Text("Busca algo") },
        label = null,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            errorContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        singleLine = true
    )
}
@Composable
fun TaskList(filteredTasks: List<Task>, listaTareas: List<Task>, selectedColor: State<String>, onCheckedChange: (Task, Boolean) -> Unit, onDeleteTask: (Task) -> Unit){
    var showRemoveDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // --- LIST ---
        LazyColumn(
            Modifier.fillMaxSize()
        ) {
            items( items = filteredTasks, key = { it.id } ) { tarea ->

                val isCompleted = tarea.isCompleted

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // --- CHECKBOX AND TASK ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 10.dp, bottom = 10.dp, start = 10.dp)
                    ) {
                        Checkbox(
                            checked = isCompleted,
                            onCheckedChange = { isChecked ->
                                onCheckedChange(tarea, isChecked)
                            }
                        )
                        Spacer(Modifier.width(4.dp))
                        // --- DESCRIPTION AND PRIORITY ---
                        Column {
                            Text(
                                text = tarea.description,
                                fontSize = 15.sp,
                                color = when(selectedColor.value){
                                    "Rojo" -> Color.Red
                                    "Verde" -> Color.Green
                                    "Azul" -> Color.Blue
                                    else -> if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                },
                                fontStyle = if (isCompleted) FontStyle.Italic else FontStyle.Normal,
                                textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                            )
                            Text(
                                text = when(tarea.priority){
                                    1 -> "Baja"
                                    2 -> "Media"
                                    3 -> "Alta"
                                    else -> "Sin prioridad"
                                },
                                color = when(tarea.priority){
                                    1 -> Color.Green
                                    2 -> Color.Yellow
                                    3 -> Color.Red
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontStyle = if (isCompleted) FontStyle.Italic else FontStyle.Normal,
                                textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                                fontSize = 12.sp
                            )
                        }
                    }
                    // --- DELETE BUTTON ---
                    IconButton(
                        onClick = {
                            taskToDelete = tarea
                            showRemoveDialog = true
                        },
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Borrar tarea",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        // --- ADVICE MESSAGE ---
        if(listaTareas.isEmpty()){
            Text(
                text = "¡Anímate a añadir una tarea!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    if(showRemoveDialog && taskToDelete != null){
        RemoveTaskDialog(
            onConfirm = {
                onDeleteTask(taskToDelete!!)
                showRemoveDialog = false
                taskToDelete = null
            },
            onDismiss = { showRemoveDialog = false }
        )
    }
}
@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit, alias: String){
    var nuevaTarea by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableIntStateOf(0) }

    val focusRequester = remember { FocusRequester() }

    Dialog( onDismissRequest = onDismiss ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(Modifier.padding(16.dp) ) {
                // --- ADD/CANCEL BUTTONS ---
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton( onClick = onDismiss ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancelar"
                        )
                    }
                    TextButton(
                        onClick = { if (nuevaTarea.isNotBlank()) onConfirm(nuevaTarea, selectedPriority) },
                        enabled = nuevaTarea.isNotBlank()
                    ) {
                        Text("Listo")
                    }
                }
                // --- CONTENT ---
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "¿Qué quieres hacer $alias?",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                TextField(
                    value = nuevaTarea,
                    onValueChange = { nuevaTarea = it },
                    placeholder = { Text("Comprar patatas")},
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                Spacer(Modifier.height(20.dp))
                // --- PRIORITIES ---
                Text(
                    text = "Prioridad",
                    fontWeight = FontWeight.Bold
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(end = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // High priority
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedPriority == 3,
                            onClick = { selectedPriority = 3}
                        )
                        Text("Alta", modifier = Modifier.clickable{ selectedPriority = 3})
                    }
                    // Medium priority
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedPriority == 2,
                            onClick = { selectedPriority = 2}
                        )
                        Text("Media", modifier = Modifier.clickable{ selectedPriority = 2})
                    }
                    // Low priority
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedPriority == 1,
                            onClick = { selectedPriority = 1}
                        )
                        Text("Baja", modifier = Modifier.clickable{ selectedPriority = 1})
                    }
                }
            }
        }
    }
    // --- FOCUS REQUEST FOR TEXTFIELD ---
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }
}

@Composable
fun RemoveTaskDialog(onConfirm: () -> Unit, onDismiss: () -> Unit){
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¡Cuidado!") },
        text = { Text("¿Estas seguro que deseas borrar esta tarea?") },
        shape = MaterialTheme.shapes.medium,
        confirmButton = {
            TextButton( onClick = onConfirm ) {
                Text("Borrar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton( onClick = onDismiss ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun PreferencesDialog(selectedTheme: State<Boolean>, selectedColor: State<String>, context: Context, scope: CoroutineScope, onDismiss: () -> Unit){
    val textColor = listOf("Defecto", "Rojo", "Verde", "Azul")
    Dialog( onDismissRequest = onDismiss ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // --- LIGHT/DARK THEME ---
                Text(
                    text = "Tema de la Aplicación",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(15.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LightMode,
                        contentDescription = "Modo claro",
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = selectedTheme.value,
                        onCheckedChange = { newValue ->
                            scope.launch {
                                TodoPreferences.setTheme(context, newValue)
                            }
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.Nightlight,
                        contentDescription = "Modo oscuro",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
                // --- TEXT COLOR ---
                Text(
                    text = "Color de las tareas",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(10.dp))
                textColor.forEach { taskColor ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedColor.value == taskColor,
                            onClick = {
                                scope.launch {
                                    TodoPreferences.setColor(context, taskColor)
                                }
                            }
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(taskColor)
                    }
                }
                Spacer(Modifier.height(20.dp))
                // --- ADVICE ---
                Text(
                    text = "¡Recuerda que puedes ordenar la lista de tareas sacudiendo el dispositivo!",
                    style = MaterialTheme.typography.labelMedium,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// --- INACTIVITY NOTIFICATION FUNCTIONALLITY ---
private const val CHANNEL_ID = "inactivity_channel"
private const val NOTIFICATION_ID= 1

// --- NOTIFICATION CHANNEL ---
fun createNotificationsChannel(context: Context){
    val name = "Recordatorios de tareas"
    val description = "Recordatorios por inactividad"

    val importance = NotificationManagerCompat.IMPORTANCE_MAX

    val channel = NotificationChannelCompat.Builder(CHANNEL_ID, importance).apply {
        setName(name)
        setDescription(description)
        setVibrationEnabled(true)
    }.build()

    NotificationManagerCompat.from(context).createNotificationChannel(channel)
}

// --- SEND INACTIVITY NOTIFICATION ---
fun sendInactivityNotification(context: Context){
    // Comporbamos el permiso
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            return //No envia si no hay permisos
        }
    }
    //Construir la notificacion
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.check_applogo)
        .setContentTitle("Hola $alias")
        .setContentText("Hace tiempo que no añades una tarea")
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setAutoCancel(true)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .build()

    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
}

private var alias = ""
fun setAlias(nAlias: String){
    alias = nAlias
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TODOAppTheme {
        //Login ( onLogin = {} )
        //Tasks( "Alejandro", "ale", onBack = {})
        //AddTaskDialog( onDismiss = {}, onConfirm = {}, "ale")
        //PreferencesDialog("Rojo")
    }
}