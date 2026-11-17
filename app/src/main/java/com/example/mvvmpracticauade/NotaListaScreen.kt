package com.example.mvvmpracticauade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mvvmpracticauade.ui.theme.MVVMPracticaUADETheme

// Host: conecta el ViewModel con la pantalla. La navegación llega como lambdas desde NotasNavHost.
@Composable
fun NotaListaHost(
    notaViewModel: NotaViewModel,
    onSeleccionarNota: (Nota) -> Unit,
    onNuevaNota: () -> Unit,
    modifier: Modifier = Modifier
) {

    LaunchedEffect(Unit) { notaViewModel.obtenerTodasLasNotas() }

    val notaUiState by notaViewModel.uiState.collectAsStateWithLifecycle()

    NotaListaScreen(
        estado = notaUiState,
        onSeleccionarNota = onSeleccionarNota,
        onNuevaNota = onNuevaNota,
        onEliminar = notaViewModel::onEliminar,
        modifier = modifier
    )
}

@Composable
fun NotaListaScreen(
    estado: NotaUiState,
    onSeleccionarNota: (Nota) -> Unit,
    onNuevaNota: () -> Unit,
    onEliminar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onNuevaNota,
            enabled = !estado.operacionEnCurso,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nueva nota")
        }

        // weight(1f): la lista ocupa el espacio que queda y scrollea ahí
        ListaNotas(
            notas = estado.notas,
            operacionEnCurso = estado.operacionEnCurso,
            onSeleccionarNota = onSeleccionarNota,
            onEliminar = onEliminar,
            modifier = Modifier.weight(1f)
        )
    }
}

// Solo dibuja las notas que recibe. No sabe de donde vienen (ni del ViewModel ni del Repository).
// Tocar una tarjeta avisa qué nota se eligió; "Eliminar" avisa el id hacia arriba.
@Composable
fun ListaNotas(
    notas: List<Nota>,
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
            Card(
                onClick = { onSeleccionarNota(nota) },
                enabled = !operacionEnCurso,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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

// En el Preview no hay nadie que maneje los eventos: lambdas vacías
@Preview(showBackground = true)
@Composable
fun NotaListaScreenPreview() {
    MVVMPracticaUADETheme {
        NotaListaScreen(
            estado = NotaUiState(
                notas = listOf(
                    Nota(nombre = "Ideas proyecto", contenido = "Armar el repository en memoria"),
                    Nota(nombre = "Turno médico", contenido = "Martes 10 hs, llevar estudios")
                )
            ),
            onSeleccionarNota = {},
            onNuevaNota = {},
            onEliminar = {}
        )
    }
}
