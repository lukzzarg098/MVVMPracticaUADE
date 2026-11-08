package com.example.mvvmpracticauade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mvvmpracticauade.ui.theme.MVVMPracticaUADETheme

// Punto de partida de la guia MVVM: pantalla traida de RoomLabUADE (commit f26ef91),
// antes de agregar Room. Todo vive dentro del composable a proposito: el Ejercicio 1
// pide analizar donde esta cada responsabilidad sin cambiar todavia la implementacion.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MVVMPracticaUADETheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)      // respeta las barras del sistema
                            .padding(16.dp),       // margen extra, opcional
                        contentAlignment = Alignment.Center
                    ) {
                        ListaDeTareasScreen()
                    }
                }
            }
        }
    }
}

data class Tarea(
    val nombre: String,
    val completada: Boolean
)

@Composable
//al girar la pantalla o cerrar el proceso, toda la informacion que agregue se pierde. El remember
//solo guarda el estado ante la recomposicion, pero no ante la creacion de la actividad
fun ListaDeTareasScreen() {

    var tareas by remember {
        mutableStateOf(
            listOf(Tarea("hacer la cama", false))
        )
    }

    val tareaParaAgregar = rememberTextFieldState()

    Column() {

        Row() {
            TextField(
                state = tareaParaAgregar,
                label = { Text("Nombre de la tarea") }
            )
            Button(onClick = {
                if (tareaParaAgregar.text.toString() != "") {
                    tareas = tareas + (Tarea(tareaParaAgregar.text.toString(), false))
                    tareaParaAgregar.clearText()
                }
            }) {
                Text("Agregar tarea")
            }
        }

        Row() {
            LazyColumn() {
                items(tareas) { item ->
                    Row() {
                        Text(item.nombre)
                        Checkbox(
                            item.completada,
                            onCheckedChange = {
                                val tarea = Tarea(item.nombre, !item.completada)
                                val nueva = tareas.map { t -> if (t.nombre == item.nombre) tarea else t }
                                tareas = nueva
                            }
                        )
                        Button(onClick = {
                            tareas = tareas.filter { t -> t.nombre != item.nombre }
                        }) {
                            Text("Eliminar tarea")
                        }
                    }
                }
            }
        }

        Text("Contador de tareas: ${tareas.size}")

        Button(onClick = {
            tareas = listOf()
        }) {
            Text("Borrar todas las tareas")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListaDeTareasPreview() {
    MVVMPracticaUADETheme {
        ListaDeTareasScreen()
    }
}
