package sahonero.alejandro.todo_app

import android.content.pm.PackageManager
import android.Manifest
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import kotlinx.coroutines.delay
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sahonero.alejandro.todo_app.data.model.Task
import sahonero.alejandro.todo_app.ui.PokemonViewModel
import sahonero.alejandro.todo_app.ui.tools.ShakeDetector
import sahonero.alejandro.todo_app.ui.tools.TodoPreferences
import sahonero.alejandro.todo_app.ui.theme.TODOAppTheme
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Locale

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

    val pokemonViewModel: PokemonViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ){
        composable ("login"){
            Login(
                onLogin = {
                    navController.navigate("tasks") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("tasks") {
            Tasks(
                logout = {
                    navController.navigate("login") {
                        popUpTo("tasks") { inclusive = true }
                    }
                },
                pokemonViewModel = pokemonViewModel
            )
        }
    }
}

@Composable
fun Login(onLogin: () -> Unit){
    // Firebase variables
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    // Form states
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    Spacer(Modifier.height(10.dp))
                    if(isRegistering){
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Nombre") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(20.dp))
                    }
                    if(errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    // --- LOGIN / REGISTER BUTTON ---
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if(email.isNotEmpty() && password.isNotEmpty()){
                                if(isRegistering){
                                    if(nameInput.isBlank()){
                                        errorMessage = "Escribe tu nombre para registrarte"
                                        return@Button
                                    }
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if(task.isSuccessful){
                                                val user = auth.currentUser
                                                val profileUpdates = UserProfileChangeRequest.Builder()
                                                    .setDisplayName(nameInput)
                                                    .build()

                                                user?.updateProfile(profileUpdates)
                                                    ?.addOnCompleteListener {
                                                        onLogin()
                                                    }
                                            } else errorMessage = task.exception?.message
                                        }
                                } else {
                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if(task.isSuccessful) onLogin()
                                            else errorMessage = task.exception?.message
                                        }
                                }
                            }
                        }
                    ) {
                        Text(if(isRegistering) "Registrarse" else "Iniciar Sesion")
                    }
                    // --- OTHER OPTIONS TO LOGIN ---
                    Spacer(Modifier.height(20.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Spacer(Modifier.width(8.dp))
                        HorizontalDivider(Modifier.weight(1f))
                        Spacer(Modifier.width(12.dp))
                        Text(text = "o", textAlign = TextAlign.Center)
                        Spacer(Modifier.width(12.dp))
                        HorizontalDivider(Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                    }
                    // --- GOOGLE BUTTON ---
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                val credentialManager = CredentialManager.create(context)
                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId("483788113168-4gh0mv1tq39v1js0bldr6vvbnlafvf6c.apps.googleusercontent.com")
                                    .setAutoSelectEnabled(false)
                                    .build()

                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()

                                try{
                                    val result = credentialManager.getCredential(context, request)
                                    val credential = result.credential

                                    if(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
                                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                                        auth.signInWithCredential(firebaseCredential)
                                            .addOnSuccessListener { onLogin() }
                                            .addOnFailureListener { e -> errorMessage = e.message }
                                    }
                                } catch (e: Exception){
                                    errorMessage = "Error Google: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.google_logo),
                                contentDescription = "Google logo",
                                modifier = Modifier.size(25.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Google")
                        }
                    }

                    TextButton(onClick = { isRegistering = !isRegistering }) {
                        Text(
                            text = if(isRegistering) "¿Ya tienes cuenta? Entra aquí" else "¿No tienes cuenta? Registrate aquí",
                            textAlign = TextAlign.Center,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tasks(logout: () -> Unit, pokemonViewModel: PokemonViewModel){
    // --- PREFERENCES ---
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selectedColor = TodoPreferences.getColor(context).collectAsState(initial = "Default")
    val selectedTheme = TodoPreferences.getTheme(context).collectAsState(initial = true)
    // --- FIREBASE ---
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val userName = user?.displayName ?: user?.email?.substringBefore("@") ?: "Usuario"
    setAlias(userName)
    val db = FirebaseFirestore.getInstance()

    if(user == null) return

    val taskList = remember { mutableStateOf<List<Task>>(emptyList()) }

    DisposableEffect(Unit) {
        val listener = db.collection("tasks")
            .whereEqualTo("userId", user.uid)
            .addSnapshotListener { snapshots, e ->
                if(e != null){
                    Toast.makeText(context, "Error al cargar: ${e.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                if(snapshots != null){
                    val tasks = snapshots.documents.map { doc ->
                        doc.toObject(Task::class.java)!!.copy(id = doc.id)
                    }
                    taskList.value = tasks
                }
            }
        onDispose { listener.remove() }
    }
    // --- TASKS ---
    var showAddTask by remember { mutableStateOf(false) }

    // --- SEARCH ---
    var sortByPriority by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredTasks by remember(searchQuery, taskList, sortByPriority) {
        derivedStateOf {
            // Filtramos por búsqueda
            val searchResult = if (searchQuery.isBlank()) {
                taskList.value
            } else {
                taskList.value.filter {
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
                        text = "¡Qué tal $userName!",
                    )
                    Text(
                        text = if (taskList.value.isEmpty()) "¿NO HAY NADA QUE HACER?" else if (taskList.value.size < 4) "¡TE QUEDA POCO!" else "¡HAY MUCHO POR HACER!",
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
                    scope = scope,
                    listaTareas = taskList.value,
                    cerrarSesion = {
                        auth.signOut()
                        logout()
                    }
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
                listaTareas = taskList.value,
                selectedColor = selectedColor,
                onCheckedChange = { tareaAEditar, nuevoEstado ->
                    db.collection("tasks").document(tareaAEditar.id).update("completed", nuevoEstado)
                },
                onDeleteTask = { tareaABorrar ->
                    // Borramos la tarea directamente de la BD
                    db.collection("tasks").document(tareaABorrar.id).delete()
                }
            )
        }
    }
    // --- ADD TASK DIALOG ---
    if(showAddTask){
        AddTaskDialog(
            onDismiss = { showAddTask = false },
            onConfirm = { nuevaTarea ->
                val tareaConUserId = nuevaTarea.copy(userId = user.uid)
                db.collection("tasks").add(tareaConUserId)
                    .addOnSuccessListener {
                        showAddTask = false
                        lastTaskTime = System.currentTimeMillis()
                        Toast.makeText(context, "¡Tarea guardada!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            },
            alias = alias,
            pokemonViewModel = pokemonViewModel
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
fun VerticalMenu(selectedTheme: State<Boolean>, selectedColor: State<String>, context: Context, scope: CoroutineScope, listaTareas: List<Task>, cerrarSesion: () -> Unit){
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
            DropdownMenuItem(
                text = {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = "Exportar tareas"
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Exportar tareas")
                    }
                },
                onClick = {
                    exportarTareas(context, listaTareas)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión"
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Cerrar sesión")
                    }
                },
                onClick = {
                    cerrarSesion()
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

fun exportarTareas(context: Context, listaTareas: List<Task>){
    val nombreArchivo = "tareas.txt"
    var texto = ""
    listaTareas.forEach {
        texto += "Tarea: ${it.description}\n" +
                "Prioridad: ${when(it.priority){ 1 -> "Baja" 2 -> "Media" 3 -> "Alta" else -> "Ninguna" } }\n" +
                "Fecha de expiración: ${it.expirationDate}\n" +
                "Pokemon: ${it.pokemon}" +
                "Tipos: ${it.tipos}" +
                "HP: ${it.hp}" +
                "Ataque: ${it.ataque}" +
                "Defensa: ${it.defensa}" +
                "Velocidad: ${it.velocidad}" +
                "------------\n"
    }

    // Comprobamos la version de Android
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
            put(MediaStore.Downloads.MIME_TYPE, "text/plain")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

        uri?.let {
            resolver.openOutputStream(it)?.use { output ->
                output.write(texto.toByteArray())
            }
            Toast.makeText(context, "¡Tareas guardadas en Descargas!", Toast.LENGTH_LONG).show()
        } ?: run {
            Toast.makeText(context, "No se han podido guardar las tareas", Toast.LENGTH_LONG).show()
        }
    }else {
        val directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        // Inicializamos el archivo
        val archivo = File(directorio, nombreArchivo)

        // Creamos el archivo
        FileOutputStream(archivo).use { it.write(texto.toByteArray()) }

        Toast.makeText(context, "¡Tareas guardadas en Descargas!", Toast.LENGTH_LONG).show()
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
                var isExpanded by remember { mutableStateOf(false) }
                val isCompleted = tarea.completed

                Column(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when(tarea.expirationDate){
                                            "Never" -> "Sin expiración"
                                            else -> tarea.expirationDate
                                        },
                                        color = when(tarea.expirationDate){
                                            "Never" -> MaterialTheme.colorScheme.onSurfaceVariant
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        fontStyle = if (isCompleted) FontStyle.Italic else FontStyle.Normal,
                                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = " ● ",
                                        fontStyle = if (isCompleted) FontStyle.Italic else FontStyle.Normal,
                                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                                        fontSize = 12.sp
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
                            }
                        }
                        Column() {
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
                            // --- EXPAND BUTTON ---
                            IconButton(
                                onClick = {
                                    isExpanded = !isExpanded
                                },
                                modifier = Modifier.padding(end = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Más info",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    // --- POKEMON INFO ---
                    if(isExpanded && tarea.pokemon.isNotEmpty()){
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 10.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Pokémon: ${tarea.pokemon.uppercase()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(8.dp))

                            // --- IMAGEN DEL POKÉMON ---
                            if (tarea.imagen.isNotEmpty()) {
                                AsyncImage(
                                    model = tarea.imagen,
                                    contentDescription = "Imagen de ${tarea.pokemon}",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .align(Alignment.CenterHorizontally)
                                )
                                Spacer(Modifier.height(8.dp))
                            }

                            // --- TIPOS ---
                            if (tarea.tipos.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Tipos: ",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    tarea.tipos.forEach { tipo ->
                                        Text(
                                            text = tipo,
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = MaterialTheme.shapes.small
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            // --- STATS ---
                            Text(
                                text = "Estadísticas:",
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                StatItem("HP", tarea.hp)
                                StatItem("Ataque", tarea.ataque)
                            }

                            Spacer(Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                StatItem("Defensa", tarea.defensa)
                                StatItem("Velocidad", tarea.velocidad)
                            }
                        }
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
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.small
            )
            .padding(12.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (Task) -> Unit, alias: String, pokemonViewModel: PokemonViewModel){
    // --- TASK ADDING ---
    var nuevaTarea by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableIntStateOf(0) }
    // --- FOCUS ON TASK DESCRIPTION ---
    val focusRequester = remember { FocusRequester() }
    // --- DATE PICKER ---
    var fechaSeleccionada by remember { mutableStateOf("Never") }
    // --- API ---
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

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
                        onClick = {
                            scope.launch {
                                isLoading = true
                                isError = false

                                val ultimaPalabra = nuevaTarea.trim().split(" ").lastOrNull() ?: ""

                                // Obtenemos los detalles del Pokémon
                                val pokemonDetails = pokemonViewModel.getPokemonDetails(ultimaPalabra)

                                if(pokemonDetails != null) {
                                    // Extraemos los stats (PokeAPI devuelve en orden específico)
                                    val hp = pokemonDetails.stats.find { it.stat.name == "hp" }?.base_stat?.toString() ?: "0"
                                    val ataque = pokemonDetails.stats.find { it.stat.name == "attack" }?.base_stat?.toString() ?: "0"
                                    val defensa = pokemonDetails.stats.find { it.stat.name == "defense" }?.base_stat?.toString() ?: "0"
                                    val velocidad = pokemonDetails.stats.find { it.stat.name == "speed" }?.base_stat?.toString() ?: "0"

                                    // Extraemos tipos (traducidos al español)
                                    val tipos = pokemonDetails.types.map { it.type.name }

                                    // Imagen de mejor calidad (official artwork)
                                    val imagen = pokemonDetails.sprites.front_default ?: ""

                                    // Creamos la tarea completa con toda la info del Pokémon
                                    val nuevaTareaCompleta = Task(
                                        description = nuevaTarea,
                                        priority = selectedPriority,
                                        expirationDate = fechaSeleccionada,
                                        pokemon = pokemonDetails.name,
                                        tipos = tipos,
                                        hp = hp,
                                        ataque = ataque,
                                        defensa = defensa,
                                        velocidad = velocidad,
                                        imagen = imagen,
                                        userId = "" // Se asignará en Tasks()
                                    )

                                    onConfirm(nuevaTareaCompleta)
                                } else
                                    isError = true

                                isLoading = false
                            }
                        },
                        enabled = nuevaTarea.isNotBlank()
                    ) {
                        if(isLoading)
                            Text("Validando...")
                        else
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
                    onValueChange = {
                        nuevaTarea = it
                        isError = false
                    },
                    placeholder = { Text("Alimentar a Snorlax")},
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    isError = isError
                )
                if (isError) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "La última palabra debe ser el nombre de un Pokémon.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
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
                Spacer(Modifier.height(20.dp))
                // --- DATE PICKER ---
                DatePickerModal(
                    currentDate = fechaSeleccionada,
                    onDateSelected = { newDate ->
                        fechaSeleccionada = newDate
                    }
                )

            }
        }
    }
    // --- FOCUS REQUEST FOR TEXTFIELD ---
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(currentDate: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    // Inicializamos el calendario con la fecha de hoy
    val calendario = Calendar.getInstance()
    val anio = calendario.get(Calendar.YEAR)
    val mes = calendario.get(Calendar.MONTH)
    val dia = calendario.get(Calendar.DAY_OF_MONTH)

    // Configuración del diálogo
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val fechaFormateada = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
            onDateSelected(fechaFormateada)
        },
        anio, mes, dia
    )

    OutlinedTextField(
        value = if(currentDate == "Never") "" else currentDate,
        onValueChange = {},
        label = { Text("¿Para cuando?") },
        placeholder = { Text("DD/MM/AAAA") },
        readOnly = true,
        trailingIcon = {
            IconButton( onClick = { datePickerDialog.show() }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Seleccionar fecha"
                )
            }
        }
    )
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

@OptIn(ExperimentalMaterial3Api::class)
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