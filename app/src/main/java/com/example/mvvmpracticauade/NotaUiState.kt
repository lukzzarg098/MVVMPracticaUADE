package com.example.mvvmpracticauade

data class NotaUiState(
    val titulo: String = "",
    val contenido: String = "",
    // Reemplazado por errorTitulo y errorContenido (un error por campo)
    // val errorValidacion: String = "",
    val operacionEnCurso: Boolean = false,
    val errorTitulo: String = "",
    val errorContenido: String = ""
)



