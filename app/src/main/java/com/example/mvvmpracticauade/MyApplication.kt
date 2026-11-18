package com.example.mvvmpracticauade

import android.app.Application
import androidx.room.Database
import androidx.room.Room


class MyApplication : Application() {


    val archivoTextoRepository: FileNotaRepository by lazy {
        FileNotaRepositoryImplementacion(filesDir)
    }

    val database: AppDataBase by lazy {
        Room.databaseBuilder(this, AppDataBase::class.java, "notas.db").build()
    }

    val notaRepository: NotaRepository by lazy {
        NotaRepositoryEnRoom(database.notaDao())
    }
}
