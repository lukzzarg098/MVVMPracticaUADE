package com.example.mvvmpracticauade

// Cada método delega en el DAO. Acá es el único lugar donde conviven Nota y NotaEntity:
// hacia el DAO se convierte con toEntity(), y lo que vuelve se convierte con toNota().
// No hace falta withContext(Dispatchers.IO): Room ya corre las funciones suspend del DAO fuera del hilo principal.
class NotaRepositoryEnRoom(private val dao: NotaDAO) : NotaRepository {

    override suspend fun obtenerTodas(): List<Nota> {
        return dao.obtenerTodas().map { it.toNota() }
    }

    override suspend fun obtenerNotaPorId(id: String): Nota? {
        return dao.obtenerPorId(id)?.toNota()
    }

    override suspend fun guardar(nota: Nota) {
        dao.insertarOActualizar(nota.toEntity())
    }

    override suspend fun eliminar(id: String) {
        dao.eliminarPorId(id)
    }
}
