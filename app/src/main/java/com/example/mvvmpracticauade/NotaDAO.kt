package com.example.mvvmpracticauade

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// Las consultas asumen que NotaEntity usa tableName = "notas" y que su clave primaria es "id" (String)
@Dao
interface NotaDAO {

    @Query("SELECT * FROM notas")
    suspend fun obtenerTodas(): List<NotaEntity>

    @Query("SELECT * FROM notas WHERE id = :id")
    suspend fun obtenerPorId(id: String): NotaEntity?

    // REPLACE: si ya existe una nota con ese id la pisa, si no la inserta.
    // Así sirve tanto para el alta como para la edición (igual que guardar() en el Repository)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(nota: NotaEntity)

    @Query("DELETE FROM notas WHERE id = :id")
    suspend fun eliminarPorId(id: String)
}
