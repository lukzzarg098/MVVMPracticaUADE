package com.example.mvvmpracticauade

interface NotaRepository {
    suspend fun obtenerTodas(): List<Nota>
    suspend fun guardar(nota: Nota)
    suspend fun eliminar(id: String)
    
    suspend fun obtenerNotaPorId(id: String): Nota?
}
