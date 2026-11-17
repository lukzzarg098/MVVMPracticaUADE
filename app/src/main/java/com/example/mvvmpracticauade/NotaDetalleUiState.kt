package com.example.mvvmpracticauade

data class NotaDetalleUiState(



    val idEnEdicion: String = "",
    val archivoTexto: String = "",
    val titulo: String = "",
    val tituloOriginal: String = "",
    val contenidoOriginal: String = "",
    val contenido: String = "",
    val operacionEnCurso: Boolean = false,
    val lecturaEscrituraEnCurso: Boolean = false,
    val errorTitulo: String = "",
    val errorContenido: String = "",
    val errorFalloLectura: String = "",
    val errorFalloEscritura: String = "",
    // Error al guardar la nota en el Repository (distinto de los errores del borrador)
    val errorGuardado: String = "",
    // Se pone en true cuando la nota terminó de guardarse: la pantalla lo usa para volver a la lista
    val guardadoCompleto: Boolean = false
) {
    val hayCambiosSinGuardar: Boolean
        get() = if (archivoTexto.isEmpty()) titulo.isNotEmpty() || contenido.isNotEmpty() else( archivoTexto != titulo + "\n" + contenido)

    val hayCambiosEnNota: Boolean
        get() = titulo != tituloOriginal || contenido != contenidoOriginal
}
