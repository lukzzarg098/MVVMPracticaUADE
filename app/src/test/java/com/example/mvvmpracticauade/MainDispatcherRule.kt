package com.example.mvvmpracticauade

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

// Los ViewModels lanzan corrutinas con viewModelScope, que corre en Dispatchers.Main.
// En un test de JVM no hay hilo Main de Android, así que antes de cada test lo reemplazamos
// por un dispatcher de test, y al terminar lo restauramos.
// Además, el dispatcher de test usa tiempo virtual: los delay() no hacen esperar al test.
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
