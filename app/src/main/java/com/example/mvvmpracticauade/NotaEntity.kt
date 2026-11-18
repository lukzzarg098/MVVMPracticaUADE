package com.example.mvvmpracticauade

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notas")
data class NotaEntity (

    @PrimaryKey
    val id: String,
    val nombre: String = "",
    val contenido: String = "",



)

fun NotaEntity.toNota(): Nota {
    return Nota(id = id, nombre = nombre, contenido = contenido)
}

fun Nota.toEntity(): NotaEntity {
    return NotaEntity(id, nombre, contenido)
}