package com.example.mvvmpracticauade

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// Tests del ViewModel de la LISTA. No abren ninguna pantalla ni tocan archivos:
// el ViewModel recibe un Repository falso con datos que elegimos nosotros.
@OptIn(ExperimentalCoroutinesApi::class)
class NotaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val compras = Nota(id = "1", nombre = "Compras", contenido = "leche y pan integral")
    private val turno = Nota(id = "2", nombre = "Turno médico", contenido = "Martes 10 hs, llevar estudios")

    @Test
    fun cargaInicial_muestraLasNotasDelRepository() = runTest {
        // Preparar
        val repository = NotaRepositoryFalso(listOf(compras, turno))
        val viewModel = NotaViewModel(repository, FileNotaRepositoryFalso())

        // Actuar (en la app lo dispara el LaunchedEffect de la pantalla)
        viewModel.obtenerTodasLasNotas()
        advanceUntilIdle()

        // Verificar
        val estado = viewModel.uiState.value
        assertEquals(listOf(compras, turno), estado.notas)
        assertFalse(estado.operacionEnCurso)
    }

    @Test
    fun cargaInicial_sinNotas_listaVacia() = runTest {
        val viewModel = NotaViewModel(NotaRepositoryFalso(), FileNotaRepositoryFalso())

        viewModel.obtenerTodasLasNotas()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.notas.isEmpty())
    }

    @Test
    fun eliminar_sacaLaNotaDeLaListaYDelRepository() = runTest {
        val repository = NotaRepositoryFalso(listOf(compras, turno))
        val viewModel = NotaViewModel(repository, FileNotaRepositoryFalso())
        viewModel.obtenerTodasLasNotas()
        advanceUntilIdle()

        viewModel.onEliminar(compras.id)
        advanceUntilIdle()

        // La lista en pantalla y el Repository (la fuente de verdad) coinciden
        assertEquals(listOf(turno), viewModel.uiState.value.notas)
        assertEquals(listOf(turno), repository.obtenerTodas())
    }
}
