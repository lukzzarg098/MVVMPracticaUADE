package com.example.mvvmpracticauade

import kotlinx.coroutines.delay
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds

// Repository de PRUEBA (Ejercicio 9): simula que el disco falla para ver la ruta de error
// sin tocar permisos del dispositivo. El delay es solo para que se llegue a ver el "cargando".
// Lanza IOException a propósito: es la que el ViewModel atrapa. Usarlo temporalmente en MainActivity.
class FileNotaRepositoryConError : FileNotaRepository {

    override suspend fun leerArchivo(): String {
        delay(1500.milliseconds)
        throw IOException("Fallo simulado al leer borrador.txt")
    }

    override suspend fun escribirArchivo(texto: String) {
        delay(1500.milliseconds)
        throw IOException("Fallo simulado al escribir borrador.txt")
    }
}
