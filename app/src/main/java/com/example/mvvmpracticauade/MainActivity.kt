package com.example.mvvmpracticauade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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

        // PRUEBA Ejercicio 9: repositorio que siempre falla. Para volver al real, descomentar la línea de abajo
        // y borrar FileNotaRepositoryConError().
        val archivoTextoRepository: FileNotaRepository =
            //FileNotaRepositoryConError()
            FileNotaRepositoryImplementacion(applicationContext.filesDir)
        val notaRepository: NotaRepository = NotaRepositoryEnJSON(applicationContext.filesDir)

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
fun NotaScreen(
    estado: NotaUiState,
    modifier: Modifier = Modifier,
    onTitular: (String) -> Unit,
    onContenido: (String) -> Unit,
    onGuardar: () -> Unit,
    onGuardarBorrador: () -> Unit,
    onDescartar: () -> Unit,
    onReintentar: () -> Unit,
    onSeleccionarNota: (Nota) -> Unit,
    onEliminar: (String) -> Unit
) {
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


        Button(
            onClick = onGuardar,
            enabled = !estado.operacionEnCurso,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (estado.operacionEnCurso) {
                CircularProgressIndicator()
            } else {
                // Si hay una nota seleccionada, guardar la reemplaza en vez de crear una nueva
                Text(if (estado.idEnEdicion.isNotEmpty()) "Guardar cambios" else "Guardar nota")
            }
        }

        AccionesBorrador(
            hayCambiosSinGuardar = estado.hayCambiosSinGuardar,
            enCurso = estado.lecturaEscrituraEnCurso,
            errorLectura = estado.errorFalloLectura,
            errorEscritura = estado.errorFalloEscritura,
            onGuardarBorrador = onGuardarBorrador,
            onDescartar = onDescartar,
            onReintentar = onReintentar
        )

        Borrador(texto = estado.archivoTexto)

        // weight(1f): la lista ocupa el espacio que queda debajo del formulario y scrollea ahi
        ListaNotas(
            notas = estado.notas,
            idEnEdicion = estado.idEnEdicion,
            operacionEnCurso = estado.operacionEnCurso,
            onSeleccionarNota = onSeleccionarNota,
            onEliminar = onEliminar,
            modifier = Modifier.weight(1f)
        )
    }
}

// Estado del borrador (cargando / error / sin guardar / guardado) + botones. Solo dibuja lo que recibe:
// qué estado corresponde lo decide el ViewModel, no la pantalla.
@Composable
fun AccionesBorrador(
    hayCambiosSinGuardar: Boolean,
    enCurso: Boolean,
    errorLectura: String,
    errorEscritura: String,
    onGuardarBorrador: () -> Unit,
    onDescartar: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Una sola línea de estado; el orden del when define la prioridad
        when {
            enCurso -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Text("Procesando borrador…", style = MaterialTheme.typography.bodyMedium)
            }

            // Si falló la lectura se ofrece reintentar (vuelve a llamar a leerArchivo)
            errorLectura.isNotEmpty() -> Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    errorLectura,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onReintentar) {
                    Text("Reintentar")
                }
            }

            // Si falló la escritura, los cambios siguen en pantalla: se puede volver a "Guardar borrador"
            errorEscritura.isNotEmpty() -> Text(
                errorEscritura,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )

            hayCambiosSinGuardar -> Text(
                "● Cambios sin guardar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )

            else -> Text(
                "✓ Borrador guardado",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onGuardarBorrador,
                enabled = !enCurso && hayCambiosSinGuardar,
                modifier = Modifier.weight(1f)
            ) {
                Text("Guardar borrador")
            }
            OutlinedButton(
                onClick = onDescartar,
                enabled = !enCurso && hayCambiosSinGuardar,
                modifier = Modifier.weight(1f)
            ) {
                Text("Descartar cambios")
            }
        }
    }
}

// Muestra el texto del borrador que ya viene en el estado. No sabe que existe un archivo:
// la lectura la hace el Repository y el ViewModel solo publica el String.
@Composable
fun Borrador(texto: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Guardado en disco", style = MaterialTheme.typography.titleMedium)
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
// Tocar una tarjeta la selecciona para editar; "Eliminar" avisa el id hacia arriba.
@Composable
fun ListaNotas(
    notas: List<Nota>,
    idEnEdicion: String,
    operacionEnCurso: Boolean,
    onSeleccionarNota: (Nota) -> Unit,
    onEliminar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
            val enEdicion = nota.id == idEnEdicion
            Card(
                onClick = { onSeleccionarNota(nota) },
                enabled = !operacionEnCurso,
                // La nota que se está editando se resalta con otro color
                colors = if (enEdicion) {
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                } else {
                    CardDefaults.cardColors()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (enEdicion) {
                            Text(
                                "Editando",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Text(nota.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(nota.contenido, style = MaterialTheme.typography.bodyMedium)
                    }
                    TextButton(
                        onClick = { onEliminar(nota.id) },
                        enabled = !operacionEnCurso
                    ) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
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
        onGuardarBorrador = notaViewModel::escribirArchivo,
        onDescartar = notaViewModel::descartarCambios,
        onReintentar = notaViewModel::leerArchivo,
        onSeleccionarNota = notaViewModel::onSeleccionarNota,
        onEliminar = notaViewModel::onEliminar,
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
            onGuardar = {},
            onGuardarBorrador = {},
            onDescartar = {},
            onReintentar = {},
            onSeleccionarNota = {},
            onEliminar = {}
        )
    }
}
