package com.example.refractiveindexapp.ui.components

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.refractiveindexapp.parsing.Catalogue
import com.example.refractiveindexapp.parsing.Page
import com.example.refractiveindexapp.ui.view.MainViewModel

@Composable
fun AppNavigation(
    viewModel: MainViewModel
) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {

        composable("main") {

            MainScreen(
                viewModel = viewModel,
                onAddMaterial = {
                    navController.navigate("addMaterial")
                },
                onAbout = {
                    navController.navigate("about")
                },
                onSettings = {
                    navController.navigate("settings")
                }
            )
        }


        composable("addMaterial") {

            AddMaterialScreen(
                viewModel = viewModel,
                onFinished = {
                    navController.popBackStack()
                }
            )
        }

        composable("about") {
            AboutScreen()
        }

        composable("settings") {
            SettingsScreen(viewModel, onNavigateUp = navController::popBackStack)
        }
    }
}
