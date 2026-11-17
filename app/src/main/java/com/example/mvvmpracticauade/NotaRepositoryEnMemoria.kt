package com.example.mvvmpracticauade

// Implementacion en memoria: las notas viven en una lista y se pierden al cerrar la app.
// No sabe nada de Compose ni del ViewModel; solo cumple el contrato NotaRepository.
class NotaRepositoryEnMemoria : NotaRepository {

    private val notas = mutableListOf<Nota>()

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
