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

class NotaDetalleViewModel(private val repository: NotaRepository,
                           private val fileRepository: FileNotaRepository) : ViewModel() {

    // Cómo se crea este ViewModel: saca los Repositories de MyApplication (se crean una sola vez)
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication
                NotaDetalleViewModel(application.notaRepository, application.archivoTextoRepository)
            }
        }
    }

    private var intentosDeGuardado = 0

    private val _uiState = MutableStateFlow(NotaDetalleUiState())
    val uiState: StateFlow<NotaDetalleUiState> = _uiState.asStateFlow()

    // Cada campo limpia solo su propio error al editarse
    fun onTitularChange(nuevoTitular: String) {
        _uiState.update { it.copy(titulo = nuevoTitular, errorTitulo = "") }
    }

    fun onContenidoChange(nuevoContenido: String) {
        _uiState.update { it.copy(contenido = nuevoContenido, errorContenido = "") }
    }

    // Sale del modo edición sin guardar: el formulario vuelve a "nota nueva"
    fun onCancelarEdicion() {
        _uiState.update {
            it.copy(titulo = "", contenido = "", idEnEdicion = "", errorTitulo = "", errorContenido = "")
        }
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
            delay(1000.milliseconds)

            if (estado.idEnEdicion.isNotEmpty()) {
                val nota = Nota(
                    id = estado.idEnEdicion,
                    nombre = estado.titulo,
                    contenido = estado.contenido
                )
                repository.guardar(nota)
                _uiState.update { it.copy(operacionEnCurso = false) }
            } else {
                val nota = Nota(nombre = estado.titulo, contenido = estado.contenido)
                repository.guardar(nota)
                _uiState.update { it.copy(operacionEnCurso = false) }

            }
            // Avisa que terminó de guardar: la pantalla vuelve a la lista recién ahora,
            // así el guardado no se corta a la mitad
            _uiState.update { it.copy(guardadoCompleto = true) }



        }
    }

    fun leerArchivo(){

        var archivoLeido = ""
        var titulo = ""
        var contenido = ""

        viewModelScope.launch {
            _uiState.update { it.copy(lecturaEscrituraEnCurso = true) }

            try {

                archivoLeido = fileRepository.leerArchivo()

                if (archivoLeido.isNotEmpty()){
                    val partes = archivoLeido.split("\n")
                    titulo = partes[0]
                    contenido = partes.subList(1, partes.size).joinToString("\n")}
                _uiState.update {it.copy(titulo= titulo, contenido = contenido, archivoTexto = archivoLeido, errorFalloLectura = "") }

            }
            catch (e: IOException){
                _uiState.update { it.copy(errorFalloLectura = "Error al leer el archivo") }
            }


            _uiState.update { it.copy(lecturaEscrituraEnCurso = false) }
        }
    }

    fun descartarCambios(){
        leerArchivo()
    }

    fun cargarNota(id: String?) {

        if (id != null) {
            viewModelScope.launch {
                _uiState.update { it.copy(operacionEnCurso = true) }
                val nota = repository.obtenerNotaPorId(id)
                if (nota != null) {
                    _uiState.update {
                        it.copy(
                            titulo = nota.nombre,
                            contenido = nota.contenido,
                            idEnEdicion = nota.id,
                            tituloOriginal = nota.nombre,
                            contenidoOriginal = nota.contenido
                        )
                    }
                }
                _uiState.update { it.copy(operacionEnCurso = false) }
            }
        }

    }
    fun escribirArchivo(){

        val nuevoTexto = _uiState.value.titulo + "\n" + _uiState.value.contenido

        viewModelScope.launch {
            _uiState.update { it.copy(lecturaEscrituraEnCurso = true) }

            try{

                fileRepository.escribirArchivo(nuevoTexto)
                _uiState.update { it.copy(archivoTexto = nuevoTexto) }
                _uiState.update { it.copy(errorFalloEscritura = "") }

            }
            catch (e: IOException){

                _uiState.update { it.copy(errorFalloEscritura = "Error al escribir el archivo") }

            }

            _uiState.update { it.copy(lecturaEscrituraEnCurso = false) }
        }

    }






}