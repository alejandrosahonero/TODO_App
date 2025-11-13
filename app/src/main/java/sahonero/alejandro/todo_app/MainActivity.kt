package sahonero.alejandro.todo_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import sahonero.alejandro.todo_app.ui.theme.TODOAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TODOAppTheme{
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        modifier = modifier,
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

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Card(
            modifier = Modifier.padding(20.dp),
            //colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    onValueChange = { nombre = it; showErrorNombre=false },
                    label = {Text("Nombre")},
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                if (showErrorNombre){
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
                    onValueChange = { alias = it; showErrorAlias=false },
                    label = {Text("Alias")},
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                if (showErrorAlias){
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
                        if(nombre.isBlank() && alias.isBlank()){
                            showErrorNombre = true
                            showErrorAlias = true
                        }else if(nombre.isBlank()) {
                            showErrorNombre = true
                        }else if (alias.isBlank()){
                            showErrorAlias = true
                        }else {
                            showErrorNombre = false
                            showErrorAlias = false
                            onLogin(nombre, alias)
                        }
                    }
                ) {
                    Text("Continuar")
                }
            }
        }
    }
}

@Composable
fun Tasks(nombre: String, alias: String, onBack: () -> Unit){
    var nuevaTarea by remember { mutableStateOf("") }
    val listaTareas = remember { mutableStateListOf<String>() }
    val tareasCompletadas = remember { mutableStateListOf<String>() }

    var expanded by remember { mutableStateOf(false) }
    val opcionesMenu = listOf("Preferencias")

    Box(
        Modifier.fillMaxSize()
            .padding(20.dp)
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "¡Qué tal $nombre ($alias)!"
                    )
                    Text(
                        text = if (listaTareas.isEmpty()) "¿NO HAY NADA QUE HACER?" else "HAY MUCHO POR HACER!",
                        fontSize = 25.sp
                    )
                }
                IconButton(
                    onClick = { expanded = true },
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Menu"
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
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
                                onClick = {}
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nuevaTarea,
                    onValueChange = { nuevaTarea = it },
                    label = { Text("¿Qué tienes que hacer $alias?") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(),
                    singleLine = true
                )
                Spacer(Modifier.width(10.dp))
                Button(onClick = {
                    if (!nuevaTarea.isBlank()) {
                        listaTareas.add(nuevaTarea)
                        nuevaTarea = ""
                    }
                }) {
                    Text("Añadir")
                }
            }
            Spacer(Modifier.height(10.dp))
            LazyColumn(
                Modifier.fillMaxSize()
            ) {
                items(listaTareas) { tarea ->

                    val isCompleted = tareasCompletadas.contains(tarea)

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
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
                            Text(
                                text = tarea,
                                fontSize = 15.sp,
                                textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                            )
                        }
                        IconButton(
                            onClick = {
                                listaTareas.remove(tarea)
                                tareasCompletadas.remove(tarea)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Borrar tarea",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
        IconButton(
            onClick = {},
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.size(60.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Añadir tarea",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TODOAppTheme {
        //Login ( onLogin = {} )
        Tasks( "Alejandro", "ale", onBack = {})
    }
}