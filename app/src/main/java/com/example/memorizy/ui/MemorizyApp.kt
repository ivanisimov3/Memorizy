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
import com.example.memorizy.ui.setdetails.SetDetailsScreen
import com.example.memorizy.ui.setdetails.SetDetailsViewModel
import com.example.memorizy.ui.studysets.StudySetsScreen
import com.example.memorizy.ui.studysets.StudySetsViewModel

@Composable
fun MemorizyApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.StudySets
    ) {

        composable<Routes.StudySets> {
            val viewModel: StudySetsViewModel = hiltViewModel() // Фабрика ViewModel благодаря Hilt
            val uiState by viewModel.uiState.collectAsState()

            StudySetsScreen(
                onAddSetClick = { navController.navigate(Routes.AddStudySet) },
                onSetClick = { setId ->
                    navController.navigate(Routes.SetDetails(setId = setId))
                },
                uiState = uiState,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onDeleteSet = viewModel::onDeleteSet
            )
        }

        composable<Routes.AddStudySet> {
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

        composable<Routes.SetDetails> {
            val viewModel: SetDetailsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            SetDetailsScreen(
                onAddCardClick = {
                    val route: Routes.SetDetails = it.toRoute()
                    navController.navigate(Routes.AddCard(setId = route.setId))
                },
                onBackClick = { navController.popBackStack() },
                uiState = uiState,
                onDeleteCard = viewModel::onDeleteCard
            )
        }

        composable<Routes.AddCard> {
        }
    }
}