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

class NotaViewModel : ViewModel() {
// Estado y operaciones; no dibujar UI aqui

    private val _uiState = MutableStateFlow(NotaUiState())
    val uiState: StateFlow<NotaUiState> = _uiState.asStateFlow()


    //var estado by mutableStateOf(NotaUiState())
    //private set

    private var intentosDeGuardado = 0

    fun onTitularChange(nuevoTitular: String) {


        _uiState.update {it.copy(titulo = nuevoTitular, errorValidacion = "")}
    }

    fun onContenidoChange(nuevoContenido: String) {

        _uiState.update {it.copy(contenido = nuevoContenido, errorValidacion = "")}


    }

    fun onGuardar() {

        intentosDeGuardado ++
        if (_uiState.value.titulo.isBlank()) {


           _uiState.update {it.copy(errorValidacion = "El título no puede estar vacío")}

        } else {
            viewModelScope.launch {

                _uiState.update {it.copy(errorValidacion = "", operacionEnCurso = true)}

                delay(1500)   // simula el guardado

                _uiState.update { it.copy(operacionEnCurso = false) }

            }
        }

        Log.d("NotaViewModel", "Intentos de guardado: $intentosDeGuardado")


    }








}