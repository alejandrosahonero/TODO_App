package sahonero.alejandro.todo_app

import android.os.Bundle
import android.renderscript.RenderScript
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import sahonero.alejandro.todo_app.ui.theme.TODOAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TODOAppTheme{
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

            Tasks(
                nombre = nombre,
                alias = alias,
                onBack = { navController.popBackStack() }
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
                    Icon(
                        painter = painterResource(R.drawable.todo_logo),
                        contentDescription = "Logo de la app",
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "TO-DO App",
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it; showErrorNombre = false },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    if (showErrorNombre) {
                        Spacer(Modifier.height(15.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "El nombre no puede estar vacío",
                                color = Color.Red,
                                fontSize = 15.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(
                        value = alias,
                        onValueChange = { alias = it; showErrorAlias = false },
                        label = { Text("Alias") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    if (showErrorAlias) {
                        Spacer(Modifier.height(15.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "El alias no puede estar vacío",
                                color = Color.Red,
                                fontSize = 15.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
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
fun Tasks(nombre: String, alias: String, onBack: () -> Unit){
    var showAddTask by remember { mutableStateOf(false) }
    val listaTareas = remember { mutableStateMapOf<String, Int>() }
    val tareasCompletadas = remember { mutableStateListOf<String>() }

    var expanded by remember { mutableStateOf(false) }
    val opcionesMenu = listOf("Preferencias")

    var searchQuery by remember { mutableStateOf("") }
    val filteredTasks = if (searchQuery.isBlank()) {
        listaTareas
    } else {
        listaTareas.filter { (tarea, _) ->
            tarea.contains(searchQuery, ignoreCase = true)
        }
    }

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
            // --- WELCOME ---
            Row(Modifier.fillMaxWidth()) {
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
                VerticalMenu(
                    expanded = expanded,
                    opcionesMenu = opcionesMenu,
                    onExpand = { expanded = true },
                    onDismiss = { expanded = false }
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
                tareasCompletadas = tareasCompletadas
            )
        }
    }
    if(showAddTask){
        AddTaskDialog(
            onDismiss = { showAddTask = false },
            onConfirm = { nuevaTarea, prioridad ->
                if (nuevaTarea.isNotBlank()) {
                    listaTareas[nuevaTarea] = prioridad
                }
                showAddTask = false
            },
            alias = alias
        )
    }
}

@Composable
fun VerticalMenu(expanded: Boolean, opcionesMenu: List<String>, onExpand: () -> Unit, onDismiss: () -> Unit){
    // --- MENU ---
    IconButton(
        onClick = onExpand,
        modifier = Modifier.size(30.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = "Menu"
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss
        ) {
            opcionesMenu.forEach { opcion ->
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
                            Text(opcion)
                        }
                    },
                    onClick = onDismiss
                )
            }
        }
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
fun TaskList( filteredTasks: Map<String, Int>, listaTareas: MutableMap<String, Int>, tareasCompletadas: MutableList<String>){
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // --- LIST ---
        LazyColumn(
            Modifier.fillMaxSize()
        ) {
            items(filteredTasks.toList()) { (tarea, prioridad) ->

                val isCompleted = tareasCompletadas.contains(tarea)

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 20.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(top = 10.dp, bottom = 10.dp, start = 10.dp)
                    ) {
                        Checkbox(
                            checked = isCompleted,
                            onCheckedChange = { nuevoValor ->
                                if (nuevoValor) tareasCompletadas.add(tarea) else tareasCompletadas.remove(
                                    tarea
                                )
                            }
                        )
                        Spacer(Modifier.width(4.dp))
                        Column() {
                            Text(
                                text = tarea,
                                fontSize = 15.sp,
                                color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                fontStyle = if (isCompleted) FontStyle.Italic else FontStyle.Normal,
                                textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                                lineHeight = 8.sp
                            )
                            Text(
                                text = when(prioridad){
                                    1 -> "Baja"
                                    2 -> "Media"
                                    3 -> "Alta"
                                    else -> "Sin prioridad"
                                },
                                color = when(prioridad){
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
                    IconButton(
                        onClick = {
                            listaTareas.remove(tarea)
                            tareasCompletadas.remove(tarea)
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
        if(listaTareas.isEmpty()){
            Text(
                text = "¡Anímate a añadir una tarea!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit,
    alias: String
){
    var nuevaTarea by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(0) }

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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TODOAppTheme {
        //Login ( onLogin = {} )
        //Tasks( "Alejandro", "ale", onBack = {})
        //AddTaskDialog( onDismiss = {}, onConfirm = {}, "ale")
    }
}