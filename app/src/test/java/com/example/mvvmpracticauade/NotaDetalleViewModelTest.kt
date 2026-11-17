package com.example.mvvmpracticauade

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// Tests del ViewModel del DETALLE: carga de la nota, validación, guardado y error.
// Todas las reglas se comprueban mirando el uiState, sin abrir la pantalla.
@OptIn(ExperimentalCoroutinesApi::class)
class NotaDetalleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val compras = Nota(id = "1", nombre = "Compras", contenido = "leche y pan integral")

    private fun crearViewModel(repository: NotaRepository) =
        NotaDetalleViewModel(repository, FileNotaRepositoryFalso())

    // ---------- Carga ----------

    @Test
    fun cargarNota_llenaElFormularioSinCambiosPendientes() = runTest {
        val viewModel = crearViewModel(NotaRepositoryFalso(listOf(compras)))

        viewModel.cargarNota(compras.id)
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertEquals("Compras", estado.titulo)
        assertEquals("leche y pan integral", estado.contenido)
        assertEquals(compras.id, estado.idEnEdicion)
        // Recién abierta, todavía no se tocó nada: Volver no debería preguntar
        assertFalse(estado.hayCambiosEnNota)
    }

    @Test
    fun cambiarTitulo_marcaCambiosSinGuardar() = runTest {
        val viewModel = crearViewModel(NotaRepositoryFalso(listOf(compras)))
        viewModel.cargarNota(compras.id)
        advanceUntilIdle()

        viewModel.onTitularChange("Supermercado")

        // Ahora sí: al tocar Volver la pantalla tiene que mostrar el diálogo
        assertTrue(viewModel.uiState.value.hayCambiosEnNota)
    }

    // ---------- Validación ----------

    @Test
    fun validacion_tituloVacioYContenidoCorto_muestraLosDosErrores() = runTest {
        val viewModel = crearViewModel(NotaRepositoryFalso())
        viewModel.onTitularChange("   ")
        viewModel.onContenidoChange("corto")

        viewModel.onGuardar()
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertEquals("El título no puede estar vacío", estado.errorTitulo)
        assertEquals("El contenido debe tener al menos 10 caracteres", estado.errorContenido)
        assertFalse(estado.guardadoCompleto)
    }

    // Desafío del ejercicio: una solicitud inválida nunca llega a llamar a guardar()
    @Test
    fun solicitudInvalida_nuncaLlamaAGuardar() = runTest {
        val repository = NotaRepositoryFalso()
        val viewModel = crearViewModel(repository)
        viewModel.onTitularChange("")
        viewModel.onContenidoChange("")

        viewModel.onGuardar()
        advanceUntilIdle()

        assertEquals(0, repository.vecesQueSeLlamoGuardar)
    }

    // ---------- Guardado correcto ----------

    @Test
    fun guardarNotaNueva_quedaEnElRepositoryYAvisaQueTermino() = runTest {
        val repository = NotaRepositoryFalso()
        val viewModel = crearViewModel(repository)
        viewModel.onTitularChange("Ideas")
        viewModel.onContenidoChange("Armar el repository falso")

        viewModel.onGuardar()
        advanceUntilIdle() // también salta el delay(1000) del ViewModel

        val guardadas = repository.obtenerTodas()
        assertEquals(1, guardadas.size)
        assertEquals("Ideas", guardadas[0].nombre)
        assertEquals("Armar el repository falso", guardadas[0].contenido)
        // guardadoCompleto es lo que hace que la pantalla vuelva a la lista
        assertTrue(viewModel.uiState.value.guardadoCompleto)
        assertFalse(viewModel.uiState.value.operacionEnCurso)
    }

    @Test
    fun editarNotaExistente_laReemplazaSinCrearOtra() = runTest {
        val repository = NotaRepositoryFalso(listOf(compras))
        val viewModel = crearViewModel(repository)
        viewModel.cargarNota(compras.id)
        advanceUntilIdle()

        viewModel.onTitularChange("Supermercado")
        viewModel.onGuardar()
        advanceUntilIdle()

        // Sigue habiendo una sola nota, con el mismo id y el título nuevo
        assertEquals(listOf(compras.copy(nombre = "Supermercado")), repository.obtenerTodas())
    }

    @Test
    fun tocarGuardarDosVecesSeguidas_guardaUnaSolaVez() = runTest {
        val repository = NotaRepositoryFalso()
        val viewModel = crearViewModel(repository)
        viewModel.onTitularChange("Ideas")
        viewModel.onContenidoChange("Armar el repository falso")

        viewModel.onGuardar()
        // El primer guardado está en curso (esperando el delay): el segundo se tiene que ignorar
        assertTrue(viewModel.uiState.value.operacionEnCurso)
        viewModel.onGuardar()
        advanceUntilIdle()

        assertEquals(1, repository.vecesQueSeLlamoGuardar)
    }

    // ---------- Error de escritura ----------

    @Test
    fun errorAlGuardar_muestraErrorYNoVuelveALaLista() = runTest {
        val repository = NotaRepositoryFalso().apply { debeFallar = true }
        val viewModel = crearViewModel(repository)
        viewModel.onTitularChange("Ideas")
        viewModel.onContenidoChange("Armar el repository falso")

        viewModel.onGuardar()
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertEquals("No se pudo guardar la nota. Probá de nuevo.", estado.errorGuardado)
        assertFalse(estado.guardadoCompleto) // no se navega: el usuario sigue en el detalle
        assertFalse(estado.operacionEnCurso) // el botón Guardar vuelve a habilitarse
        // Lo que escribió no se pierde, así puede reintentar
        assertEquals("Ideas", estado.titulo)
        assertEquals("Armar el repository falso", estado.contenido)
    }

    @Test
    fun reintentarDespuesDeUnError_guardaYLimpiaElError() = runTest {
        val repository = NotaRepositoryFalso().apply { debeFallar = true }
        val viewModel = crearViewModel(repository)
        viewModel.onTitularChange("Ideas")
        viewModel.onContenidoChange("Armar el repository falso")
        viewModel.onGuardar()
        advanceUntilIdle()

        repository.debeFallar = false
        viewModel.onGuardar()
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertEquals("", estado.errorGuardado)
        assertTrue(estado.guardadoCompleto)
        assertEquals(1, repository.obtenerTodas().size)
    }
}
