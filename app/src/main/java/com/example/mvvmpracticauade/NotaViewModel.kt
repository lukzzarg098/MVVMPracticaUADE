package com.example.mvvmpracticauade

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NotaViewModel : ViewModel() {
// Estado y operaciones; no dibujar UI aqui

var estado by mutableStateOf(NotaUiState())
    private set

    private var intentosDeGuardado = 0

    fun onTitularChange(nuevoTitular: String) {
        estado = estado.copy(titulo = nuevoTitular, errorValidacion = "")
    }

    fun onContenidoChange(nuevoContenido: String) {
        estado = estado.copy(contenido = nuevoContenido)
    }

    fun onGuardar() {

        intentosDeGuardado ++
        if (estado.titulo.isBlank()) {
            estado = estado.copy(errorValidacion = "El título no puede estar vacío")
        } else {
            viewModelScope.launch {
                estado = estado.copy(operacionEnCurso = true)
                delay(1500)   // simula el guardado
                estado = estado.copy(operacionEnCurso = false)
            }
        }

        Log.d("NotaViewModel", "Intentos de guardado: $intentosDeGuardado")


    }








}