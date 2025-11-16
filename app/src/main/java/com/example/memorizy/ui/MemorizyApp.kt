package com.example.memorizy.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.memorizy.ui.navigation.Routes
import com.example.memorizy.ui.screen.UserSetsScreen

@Composable
fun MemorizyApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.UserSets
    ) {

        composable<Routes.UserSets> {
            UserSetsScreen(
                onAddSetClick = {
                    navController.navigate(Routes.AddSet)
                },
                onSetClick = { setId ->
                    navController.navigate(Routes.SetDetails(setId = setId))
                }
            )
        }

        composable<Routes.AddSet> {
        }

        composable<Routes.SetDetails> { backStackEntry ->
            val route: Routes.SetDetails = backStackEntry.toRoute()
            val setId = route.setId
        }
    }
}