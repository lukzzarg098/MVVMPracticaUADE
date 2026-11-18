package com.example.mvvmpracticauade

import java.util.UUID

data class Nota(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val contenido: String,


)


