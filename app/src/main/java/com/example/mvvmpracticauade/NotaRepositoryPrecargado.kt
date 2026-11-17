package com.example.mvvmpracticauade

// Otra implementacion del mismo contrato: tambien en memoria, pero arranca con notas cargadas.
// Sirve para el desafio: cambiar de implementacion sin tocar la pantalla.
class NotaRepositoryPrecargado : NotaRepository {

    private val notas = mutableListOf(
        Nota(nombre = "Bienvenida", contenido = "Esta nota viene del repository precargado"),
        Nota(nombre = "Compras", contenido = "Leche, pan, yerba y café"),
        Nota(nombre = "Facultad", contenido = "Terminar la guía de MVVM y Repository")
    )

    override suspend fun obtenerNotaPorId(id: String): Nota? {
        return notas.find { it.id == id }
    }

    // Devuelve una copia para que nadie de afuera pueda modificar la lista interna
    override suspend fun obtenerTodas(): List<Nota> = notas.toList()

    // Si ya existe una nota con ese id la reemplaza; si no, la agrega
    override suspend fun guardar(nota: Nota) {
        val indice = notas.indexOfFirst { it.id == nota.id }
        if (indice >= 0) notas[indice] = nota else notas.add(nota)
    }

    override suspend fun eliminar(id: String) {
        notas.removeAll { it.id == id }
    }
}
