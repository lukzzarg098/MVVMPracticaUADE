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

    override suspend fun escribirArchivo(texto: String): Boolean {
        var guardado = false
        try{
            withContext(Dispatchers.IO){

                val archivo = File(carpeta, "borrador.txt")
                if (!archivo.exists()){
                    archivo.createNewFile()
                    archivo.writeText(texto, Charsets.UTF_8)
                }
                else{
                    archivo.writeText(texto, Charsets.UTF_8)
                }
                guardado = true

            }
        }
        catch (e: Exception){
            guardado = false
        }
        return guardado
    }
}