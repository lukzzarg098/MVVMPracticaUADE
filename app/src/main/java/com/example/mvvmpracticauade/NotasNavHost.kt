package com.example.mvvmpracticauade

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// Rutas de la app. Al detalle solo viaja el id de la nota (nunca la nota entera).
// Sin id = nota nueva.
private const val RUTA_LISTA = "lista"
private const val RUTA_DETALLE = "detalle?id={id}"

// La navegación vive acá: las pantallas solo avisan "tocaron esta nota" o "quieren volver".
// Cada ViewModel se crea con viewModel() dentro de su destino, así queda atado a esa entrada
// de navegación: cada vez que se abre el detalle arranca con un NotaDetalleViewModel nuevo.
@Composable
fun NotasNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = RUTA_LISTA, modifier = modifier) {

        composable(RUTA_LISTA) {
            NotaListaHost(
                notaViewModel = viewModel(factory = NotaViewModel.Factory),
                onSeleccionarNota = { nota -> navController.navigate("detalle?id=${nota.id}") },
                onNuevaNota = { navController.navigate("detalle") }
            )
        }

        composable(
            route = RUTA_DETALLE,
            arguments = listOf(navArgument("id") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            NotaDetalleHost(
                notaDetalleViewModel = viewModel(factory = NotaDetalleViewModel.Factory),
                idNota = backStackEntry.arguments?.getString("id"),
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
