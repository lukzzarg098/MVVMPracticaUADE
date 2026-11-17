package com.example.mvvmpracticauade

data class NotaUiState(


    val notas: List<Nota> = emptyList(),
    val archivoTexto: String = "",
    val operacionEnCurso: Boolean = false,
    val lecturaEscrituraEnCurso: Boolean = false,
    val errorFalloLectura: String = "",
    val errorFalloEscritura: String = ""

)



