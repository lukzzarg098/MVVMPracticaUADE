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
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds

class NotaViewModel (private val repository: NotaRepository = NotaRepositoryEnMemoria(),
                     private val fileRepository: FileNotaRepository
) : ViewModel()
 {
// Estado y operaciones; no dibujar UI aqui

    private val _uiState = MutableStateFlow(NotaUiState())
    val uiState: StateFlow<NotaUiState> = _uiState.asStateFlow()

    init {
        obtenerTodasLasNotas()
        leerArchivo()

    }
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
            delay(1000.milliseconds)
            val notaNew = Nota(nombre = estado.titulo, contenido = estado.contenido)
            repository.guardar(notaNew)
            _uiState.update { it.copy(operacionEnCurso = false) }
            obtenerTodasLasNotas()
            _uiState.update {  it.copy(titulo = "", contenido = "") }

        }
    }

    fun obtenerTodasLasNotas() {
     viewModelScope.launch {

         _uiState.update { it.copy(operacionEnCurso = true) }

        _uiState.update { it.copy(notas = repository.obtenerTodas()) }

         _uiState.update { it.copy(operacionEnCurso = false) }
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








