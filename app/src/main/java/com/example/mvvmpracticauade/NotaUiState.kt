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
    val lecturaEscrituraEnCurso: Boolean = false,
    val errorTitulo: String = "",
    val errorContenido: String = "",
    val errorFalloLectura: String = "",
    val errorFalloEscritura: String = ""
) {
    val hayCambiosSinGuardar: Boolean
        get() = if (archivoTexto.isEmpty()) titulo.isNotEmpty() || contenido.isNotEmpty() else( archivoTexto != titulo + "\n" + contenido)
}



