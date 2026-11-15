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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mvvmpracticauade.ui.theme.MVVMPracticaUADETheme
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val archivoTextoRepository: FileNotaRepository =
            FileNotaRepositoryImplementacion(applicationContext.filesDir)
        val notaRepository: NotaRepository = NotaRepositoryPrecargado()

        val viewModel: NotaViewModel by viewModels{
            viewModelFactory {
                initializer { NotaViewModel(notaRepository, archivoTextoRepository) }
            }
        }


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
    val id: String = UUID.randomUUID().toString(),
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

        Borrador(texto = estado.archivoTexto)

        // weight(1f): la lista ocupa el espacio que queda debajo del formulario y scrollea ahi
        ListaNotas(notas = estado.notas, modifier = Modifier.weight(1f))
    }
}

// Muestra el texto del borrador que ya viene en el estado. No sabe que existe un archivo:
// la lectura la hace el Repository y el ViewModel solo publica el String.
@Composable
fun Borrador(texto: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Borrador", style = MaterialTheme.typography.titleMedium)
            if (texto.isEmpty()) {
                Text(
                    "No hay borrador guardado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    texto,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Solo dibuja las notas que recibe. No sabe de donde vienen (ni del ViewModel ni del Repository).
@Composable
fun ListaNotas(notas: List<Nota>, modifier: Modifier = Modifier) {
    if (notas.isEmpty()) {
        Text("Todavía no hay notas", modifier = modifier)
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // key = id: Compose identifica cada fila por su nota, no por su posicion
        items(notas, key = { it.id }) { nota ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(nota.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(nota.contenido, style = MaterialTheme.typography.bodyMedium)
                }
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
                operacionEnCurso = false,
                archivoTexto = "Ideas sueltas para la próxima clase:\n- repasar withContext\n- probar Device Explorer",
                notas = listOf(
                    Nota(nombre = "Ideas proyecto", contenido = "Armar el repository en memoria"),
                    Nota(nombre = "Turno médico", contenido = "Martes 10 hs, llevar estudios")
                )
            ),
            // En el Preview no hay nadie que maneje los eventos: lambdas vacías
            onTitular = {},
            onContenido = {},
            onGuardar = {}
        )
    }
}
