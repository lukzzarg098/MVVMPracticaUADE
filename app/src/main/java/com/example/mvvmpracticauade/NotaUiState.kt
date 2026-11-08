package com.example.mvvmpracticauade

data class NotaUiState(
    val titulo: String = "",
    val contenido: String = "",
    val errorValidacion: String = "",
    var operacionEnCurso: Boolean = false
)



