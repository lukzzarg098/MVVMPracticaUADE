package com.example.mvvmpracticauade

import java.io.File

interface FileNotaRepository {

    suspend fun leerArchivo(): String


}