package com.example.mvvmpracticauade

import android.app.Application


class MyApplication : Application() {


    val archivoTextoRepository: FileNotaRepository by lazy {
        FileNotaRepositoryImplementacion(filesDir)
    }

    val notaRepository: NotaRepository by lazy {
        NotaRepositoryEnMemoria()
    }
}
