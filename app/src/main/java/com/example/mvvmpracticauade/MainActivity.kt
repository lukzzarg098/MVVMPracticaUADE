package com.example.mvvmpracticauade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mvvmpracticauade.ui.theme.MVVMPracticaUADETheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MVVMPracticaUADETheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    NotaScreenHost(modifier = Modifier.padding(padding))


                }
            }
        }
    }
}

data class Nota(
    val nombre: String,
    val contenido: String
)

// La pantalla solo DIBUJA lo que viene en `estado`. No guarda copias propias del texto.
@Composable
fun NotaScreen(estado: NotaUiState, modifier: Modifier = Modifier, onTitular: (String) -> Unit, onContenido: (String) -> Unit, onGuardar: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextField(
            value = estado.titulo,
            onValueChange = onTitular,
            label = { Text("Título") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = estado.contenido,
            onValueChange = onContenido ,
            label = { Text("Contenido") },
            minLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        if (estado.errorValidacion.isNotEmpty()) {
            Text(estado.errorValidacion, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = onGuardar,
            enabled = !estado.operacionEnCurso,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (estado.operacionEnCurso) {
                CircularProgressIndicator()
            } else {
                Text("Guardar")
            }
        }
    }
}

@Composable
fun NotaScreenHost(modifier: Modifier){
    var textoTitulo by remember {mutableStateOf("")}
    var textoContenido by remember {mutableStateOf("")}

    val scope = rememberCoroutineScope()


    var estado by remember { mutableStateOf(NotaUiState(
        titulo = textoTitulo,
        contenido = textoContenido,
        errorValidacion = "",
        operacionEnCurso = false
    )) }


    NotaScreen(
        estado =estado,
        onTitular = { estado = estado.copy(titulo = it, errorValidacion = "") },
        onContenido = { estado = estado.copy(contenido = it, errorValidacion = "") },
        onGuardar = {
            if (estado.titulo.isBlank()) {
                estado = estado.copy(errorValidacion = "El título no puede estar vacío")
            } else {
                scope.launch {
                    estado = estado.copy(operacionEnCurso = true)
                    delay(1500)   // simula el guardado
                    estado = estado.copy(operacionEnCurso = false)
                }
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun NotaScreenPreview() {
    MVVMPracticaUADETheme {
        NotaScreen(
            estado = NotaUiState(
                titulo = "Compras",
                contenido = "",
                errorValidacion = "El título no puede estar vacío",
                operacionEnCurso = false
            ),
            // En el Preview no hay nadie que maneje los eventos: lambdas vacías
            onTitular = {},
            onContenido = {},
            onGuardar = {}
        )
    }
}
