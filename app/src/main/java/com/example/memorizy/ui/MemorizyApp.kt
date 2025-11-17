package com.example.memorizy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.memorizy.ui.addset.AddStudySetScreen
import com.example.memorizy.ui.addset.AddStudySetViewModel
import com.example.memorizy.ui.navigation.Routes
import com.example.memorizy.ui.studysets.StudySetsScreen
import com.example.memorizy.ui.studysets.StudySetsViewModel

@Composable
fun MemorizyApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.UserSets
    ) {

        composable<Routes.UserSets> {
            val viewModel: StudySetsViewModel = hiltViewModel() // Фабрика ViewModel благодаря Hilt
            val uiState by viewModel.uiState.collectAsState()

            StudySetsScreen(
                onAddSetClick = { navController.navigate(Routes.AddSet) },
                onSetClick = { setId ->
                    navController.navigate(Routes.SetDetails(setId = setId))
                },
                uiState = uiState,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onDeleteSet = viewModel::onDeleteSet
            )
        }

        composable<Routes.AddSet> {
            val viewModel: AddStudySetViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            AddStudySetScreen(
                onSetCreatedClick = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() },
                uiState = uiState,
                onIconSelected = viewModel::onIconSelected,
                onNameChanged = viewModel::onNameChanged,
                onDescriptionChanged = viewModel::onDescriptionChanged,
                onCreateButtonClicked = viewModel::onCreateButtonClicked
            )
        }

        composable<Routes.SetDetails> { backStackEntry ->
            val route: Routes.SetDetails = backStackEntry.toRoute()
            val setId = route.setId
        }

        composable<Routes.AddCard> {
        }
    }
}