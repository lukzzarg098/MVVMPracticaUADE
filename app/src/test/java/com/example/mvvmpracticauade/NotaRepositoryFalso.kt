package com.example.mvvmpracticauade

import java.io.IOException

// Repository FALSO para tests (Ejercicio 13). Vive en src/test: la app nunca lo usa.
// - Arranca con las notas que le pase el test, así sabemos qué esperar.
// - Con debeFallar = true, guardar() tira IOException para probar el camino de error.
// - Cuenta cuántas veces se llamó a guardar(), para verificar que una nota inválida nunca se guarda.
class NotaRepositoryFalso(notasIniciales: List<Nota> = emptyList()) : NotaRepository {

    private val notas = notasIniciales.toMutableList()

    var debeFallar = false
    var vecesQueSeLlamoGuardar = 0
        private set

    override suspend fun obtenerTodas(): List<Nota> = notas.toList()

    override suspend fun obtenerNotaPorId(id: String): Nota? = notas.find { it.id == id }

    override suspend fun guardar(nota: Nota) {
        vecesQueSeLlamoGuardar++
        if (debeFallar) throw IOException("Fallo simulado al guardar")

        val indice = notas.indexOfFirst { it.id == nota.id }
        if (indice >= 0) notas[indice] = nota else notas.add(nota)
    }

    override suspend fun eliminar(id: String) {
        notas.removeAll { it.id == id }
    }
}

// Falso mínimo del archivo de borrador: los ViewModels lo piden en el constructor,
// pero estos tests no prueban el borrador. Guarda el texto en memoria, sin tocar disco.
class FileNotaRepositoryFalso(private var texto: String = "") : FileNotaRepository {

    override suspend fun leerArchivo(): String = texto

    override suspend fun escribirArchivo(texto: String) {
        this.texto = texto
    }
}
