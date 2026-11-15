package com.example.mvvmpracticauade

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FileNotaRepositoryImplementacion(private val carpeta: File) : FileNotaRepository {

    override suspend fun leerArchivo(): String {
        var contenido = ""
        withContext(Dispatchers.IO){





            val archivo = File(carpeta, "borrador.txt")

            if (!archivo.exists()){
                contenido =  ""
            }
            else{
                contenido = archivo.readText(Charsets.UTF_8)

            }



        }
        return contenido










    }


}