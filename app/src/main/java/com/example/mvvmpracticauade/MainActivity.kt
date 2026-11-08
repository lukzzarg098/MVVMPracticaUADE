package com.example.mvvmpracticauade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mvvmpracticauade.ui.theme.MVVMPracticaUADETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: NotaViewModel by viewModels()

        enableEdgeToEdge()
        setContent {
            MVVMPracticaUADETheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    NotaScreenHost(notaViewModel = viewModel, modifier = Modifier.padding(padding))


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
            isError = estado.errorTitulo.isNotEmpty(),
            supportingText = { if (estado.errorTitulo.isNotEmpty()) Text(estado.errorTitulo) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = estado.contenido,
            onValueChange = onContenido ,
            label = { Text("Contenido") },
            isError = estado.errorContenido.isNotEmpty(),
            supportingText = { if (estado.errorContenido.isNotEmpty()) Text(estado.errorContenido) },
            minLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        // Reemplazado por los errores de cada campo (supportingText)
        // if (estado.errorValidacion.isNotEmpty()) {
        //     Text(estado.errorValidacion, color = MaterialTheme.colorScheme.error)
        // }

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
fun NotaScreenHost(notaViewModel: NotaViewModel, modifier: Modifier){


    val notaUiState by notaViewModel.uiState.collectAsStateWithLifecycle()


    NotaScreen(
        estado = notaUiState,
        onTitular = notaViewModel::onTitularChange,
        onContenido = notaViewModel::onContenidoChange,
        onGuardar = notaViewModel::onGuardar,
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
                errorContenido = "El contenido debe tener al menos 10 caracteres",
                operacionEnCurso = false
            ),
            // En el Preview no hay nadie que maneje los eventos: lambdas vacías
            onTitular = {},
            onContenido = {},
            onGuardar = {}
        )
    }
}
