package com.example.mvvmpracticauade

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

class NotaRepositoryEnJSON(private val carpeta: File): NotaRepository {


    override suspend fun eliminar(id: String) {
        val lista = leerArchivo().toMutableList()
        lista.removeAll { it.id == id }
        escribirArchivo(lista)
    }
    override suspend fun guardar(nota: Nota) {
        val lista = leerArchivo().toMutableList()
        val indice = lista.indexOfFirst { it.id == nota.id }
        if (indice >= 0) lista[indice] = nota else lista.add(nota)
        escribirArchivo(lista)
    }
    override suspend fun obtenerTodas(): List<Nota> {
        return leerArchivo()
    }

    private suspend fun leerArchivo(): List<Nota>{

        var textoArchivo = ""
        val listaNotasAEnviar: List<Nota>

        withContext(Dispatchers.IO){
            val archivo = File(carpeta, "notas.json")
            if (!archivo.exists()){

                listaNotasAEnviar = emptyList()

            }
            else{
                textoArchivo = archivo.readText(Charsets.UTF_8)
                if (textoArchivo.isNotBlank()){
                    val array = JSONArray(textoArchivo)
                    val listaNotas = mutableListOf<Nota>()
                    for (i in 0 until array.length()){
                        val jsonObject = array.getJSONObject(i)
                        val id = jsonObject.getString("id")
                        val nombre = jsonObject.getString("nombre")
                        val contenido = jsonObject.getString("contenido")
                        listaNotas.add(Nota(id = id, nombre = nombre, contenido = contenido))
                    }
                    listaNotasAEnviar = listaNotas
                }
                else{
                    listaNotasAEnviar = emptyList()
                }

            }
        }
        return listaNotasAEnviar
    }
    // Formato de notas.json: un array JSON con un objeto por nota.
    //   [{"id":"...","nombre":"...","contenido":"..."}, ...]
    // JSONObject se encarga de escapar comillas, ";", saltos de línea, etc.
    // Sobrescribe el archivo entero con la lista completa: primero escribe notas.json.tmp y,
    // solo si terminó bien, lo renombra a notas.json (así nunca queda un notas.json a medio escribir).
    private suspend fun escribirArchivo(notas: List<Nota>){
        val array = JSONArray()
        for (nota in notas) {
            val jsonObject = JSONObject()
            jsonObject.put("id", nota.id)
            jsonObject.put("nombre", nota.nombre)
            jsonObject.put("contenido", nota.contenido)
            array.put(jsonObject)
        }

        withContext(Dispatchers.IO) {
            val archivo = File(carpeta, "notas.json.tmp")
            archivo.writeText(array.toString(), Charsets.UTF_8)
            val reemplazo = archivo.renameTo(File(carpeta, "notas.json"))
            if (!reemplazo) throw IOException("Error al escribir el archivo")
        }
    }

}