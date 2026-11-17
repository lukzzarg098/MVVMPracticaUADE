package com.example.mvvmpracticauade

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mvvmpracticauade.ui.theme.MVVMPracticaUADETheme

// Host: conecta el ViewModel con la pantalla. La navegación llega como lambdas desde NotasNavHost.
@Composable
fun NotaDetalleHost(
    notaDetalleViewModel: NotaDetalleViewModel,
    idNota: String?,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(idNota) { notaDetalleViewModel.cargarNota(idNota) }
    val detalleUiState by notaDetalleViewModel.uiState.collectAsStateWithLifecycle()

    // Cuando el ViewModel avisa que terminó de guardar, volvemos a la lista (sin diálogo: ya se guardó)
    LaunchedEffect(detalleUiState.guardadoCompleto) {
        if (detalleUiState.guardadoCompleto) onVolver()
    }

    // Si la nota tiene cambios sin guardar, en vez de volver directo se le pregunta al usuario.
    // Que el diálogo esté abierto o no es estado de la UI (no es un dato de la nota).
    var mostrarDialogoSalir by rememberSaveable { mutableStateOf(false) }
    val intentarVolver: () -> Unit = {
        if (detalleUiState.hayCambiosEnNota) {
            mostrarDialogoSalir = true
        } else {
            onVolver()
        }
    }

    // Intercepta también el "atrás" del sistema (gesto o botón), no solo nuestro botón Volver
    BackHandler(enabled = detalleUiState.hayCambiosEnNota) { mostrarDialogoSalir = true }

    if (mostrarDialogoSalir) {
        DialogoCambiosSinGuardar(
            onDescartar = {
                mostrarDialogoSalir = false
                onVolver()
            },
            onSeguirEditando = { mostrarDialogoSalir = false }
        )
    }

    NotaDetalleScreen(
        estado = detalleUiState,
        onTitular = notaDetalleViewModel::onTitularChange,
        onContenido = notaDetalleViewModel::onContenidoChange,
        onGuardar = notaDetalleViewModel::onGuardar,
        onVolver = intentarVolver,
        onGuardarBorrador = notaDetalleViewModel::escribirArchivo,
        onDescartar = notaDetalleViewModel::descartarCambios,
        onReintentar = notaDetalleViewModel::leerArchivo,
        modifier = modifier
    )
}

// Se muestra al querer volver con cambios sin guardar. Solo dibuja y avisa qué eligió el usuario.
@Composable
fun DialogoCambiosSinGuardar(
    onDescartar: () -> Unit,
    onSeguirEditando: () -> Unit
) {
    AlertDialog(
        // Tocar afuera del diálogo = seguir editando (no se pierde nada por accidente)
        onDismissRequest = onSeguirEditando,
        title = { Text("Cambios sin guardar") },
        text = { Text("Si volvés ahora, se pierden los cambios de esta nota.") },
        confirmButton = {
            TextButton(onClick = onSeguirEditando) {
                Text("Seguir editando")
            }
        },
        dismissButton = {
            TextButton(onClick = onDescartar) {
                Text("Descartar", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

// La pantalla solo DIBUJA lo que viene en `estado`. No guarda copias propias del texto.
@Composable
fun NotaDetalleScreen(
    estado: NotaDetalleUiState,
    onTitular: (String) -> Unit,
    onContenido: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit,
    onGuardarBorrador: () -> Unit,
    onDescartar: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
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
            onValueChange = onContenido,
            label = { Text("Contenido") },
            isError = estado.errorContenido.isNotEmpty(),
            supportingText = { if (estado.errorContenido.isNotEmpty()) Text(estado.errorContenido) },
            minLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        val editando = estado.idEnEdicion.isNotEmpty()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onGuardar,
                enabled = !estado.operacionEnCurso,
                modifier = Modifier.weight(1f)
            ) {
                if (estado.operacionEnCurso) {
                    CircularProgressIndicator()
                } else {
                    // Si se abrió una nota existente, guardar la reemplaza en vez de crear una nueva
                    Text(if (editando) "Guardar cambios" else "Guardar nota")
                }
            }
            OutlinedButton(
                onClick = onVolver,
                enabled = !estado.operacionEnCurso,
                modifier = Modifier.weight(1f)
            ) {
                Text("Volver")
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

// En el Preview no hay nadie que maneje los eventos: lambdas vacías
@Preview(showBackground = true)
@Composable
fun NotaDetalleScreenPreview() {
    MVVMPracticaUADETheme {
        NotaDetalleScreen(
            estado = NotaDetalleUiState(
                titulo = "Compras",
                contenido = "",
                errorContenido = "El contenido debe tener al menos 10 caracteres",
                archivoTexto = "Ideas sueltas para la próxima clase:\n- repasar withContext\n- probar Device Explorer"
            ),
            onTitular = {},
            onContenido = {},
            onGuardar = {},
            onVolver = {},
            onGuardarBorrador = {},
            onDescartar = {},
            onReintentar = {}
        )
    }
}
