package com.example.mvvmpracticauade

import java.util.UUID

data class NotaUiState(


    val notas: List<Nota> = emptyList(),
    val archivoTexto: String = "",
    val titulo: String = "",
    val contenido: String = "",
    // Reemplazado por errorTitulo y errorContenido (un error por campo)
    // val errorValidacion: String = "",
    val operacionEnCurso: Boolean = false,
    val errorTitulo: String = "",
    val errorContenido: String = ""
)



