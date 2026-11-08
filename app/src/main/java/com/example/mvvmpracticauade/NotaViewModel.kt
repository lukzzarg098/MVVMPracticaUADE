package com.example.mvvmpracticauade


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class NotaViewModel : ViewModel() {
// Estado y operaciones; no dibujar UI aqui

    private val _uiState = MutableStateFlow(NotaUiState())
    val uiState: StateFlow<NotaUiState> = _uiState.asStateFlow()


    //var estado by mutableStateOf(NotaUiState())
    //private set

    private var intentosDeGuardado = 0

    // Cada campo limpia solo su propio error al editarse
    fun onTitularChange(nuevoTitular: String) {
        _uiState.update { it.copy(titulo = nuevoTitular, errorTitulo = "") }
    }

    fun onContenidoChange(nuevoContenido: String) {
        _uiState.update { it.copy(contenido = nuevoContenido, errorContenido = "") }
    }

    fun onGuardar() {
        intentosDeGuardado++
        Log.d("NotaViewModel", "Intentos de guardado: $intentosDeGuardado")

        // Regla: no guardar dos veces mientras se procesa una solicitud
        if (_uiState.value.operacionEnCurso) return

        // Cada regla se evalúa por separado para poder mostrar los dos errores a la vez
        val estado = _uiState.value
        val errorTitulo =
            if (estado.titulo.isBlank()) "El título no puede estar vacío" else ""
        val errorContenido =
            if (estado.contenido.trim().length < 10) "El contenido debe tener al menos 10 caracteres" else ""

        _uiState.update { it.copy(errorTitulo = errorTitulo, errorContenido = errorContenido) }

        // Si hay algún error, no se guarda
        if (errorTitulo.isNotEmpty() || errorContenido.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(operacionEnCurso = true) }
            delay(1500.milliseconds)   // simula el guardado
            _uiState.update { it.copy(operacionEnCurso = false, titulo = "", contenido = "") }
        }
    }




}








