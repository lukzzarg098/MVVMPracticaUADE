package com.example.mvvmpracticauade


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds

class NotaViewModel (private val repository: NotaRepository,
                     private val fileRepository: FileNotaRepository
) : ViewModel()
 {
// Estado y operaciones; no dibujar UI aqui

     // Cómo se crea este ViewModel: saca los Repositories de MyApplication (se crean una sola vez)
     companion object {
         val Factory: ViewModelProvider.Factory = viewModelFactory {
             initializer {
                 val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication
                 NotaViewModel(application.notaRepository, application.archivoTextoRepository)
             }
         }
     }

    private val _uiState = MutableStateFlow(NotaUiState())
    val uiState: StateFlow<NotaUiState> = _uiState.asStateFlow()



     fun onEliminar(id: String){
         viewModelScope.launch {
             _uiState.update { it.copy(operacionEnCurso = true) }
             //if (id == _uiState.value.idEnEdicion){
            //     _uiDetalleState.update { it.copy(titulo = "", contenido = "", idEnEdicion = "") }
             //}
             repository.eliminar(id)
             _uiState.update { it.copy(operacionEnCurso = false) }
             obtenerTodasLasNotas()

         }
     }

     // Seleccionar una nota ya no pasa por acá: la pantalla navega a "detalle/{id}"
     // y el NotaDetalleViewModel carga la nota del Repository con ese id.


    fun obtenerTodasLasNotas() {
     viewModelScope.launch {

         _uiState.update { it.copy(operacionEnCurso = true) }

        _uiState.update { it.copy(notas = repository.obtenerTodas()) }

         _uiState.update { it.copy(operacionEnCurso = false) }
    }

    }



}








