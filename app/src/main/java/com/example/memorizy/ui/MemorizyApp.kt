package com.example.memorizy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.memorizy.ui.screens.addcard.AddCardScreen
import com.example.memorizy.ui.screens.addcard.AddCardViewModel
import com.example.memorizy.ui.screens.addstudyset.AddStudySetScreen
import com.example.memorizy.ui.screens.addstudyset.AddStudySetViewModel
import com.example.memorizy.ui.navigation.Routes
import com.example.memorizy.ui.screens.auth.AuthScreen
import com.example.memorizy.ui.screens.auth.AuthViewModel
import com.example.memorizy.ui.screens.learningmode.LearningModeScreen
import com.example.memorizy.ui.screens.learningmode.LearningModeViewModel
import com.example.memorizy.ui.screens.setdetails.SetDetailsScreen
import com.example.memorizy.ui.screens.setdetails.SetDetailsViewModel
import com.example.memorizy.ui.screens.settings.SettingsScreen
import com.example.memorizy.ui.screens.settings.SettingsViewModel
import com.example.memorizy.ui.screens.studysets.StudySetsScreen
import com.example.memorizy.ui.screens.studysets.StudySetsViewModel

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
                onSettingsClick = { navController.navigate(Routes.Settings) },
                onAddSetClick = { navController.navigate(Routes.AddStudySet) },
                onSetClick = { setId ->
                    navController.navigate(Routes.SetDetails(setId = setId))
                },
                uiState = uiState,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onDeleteSet = viewModel::onDeleteSet
            )
        }

        composable<Routes.Settings> {
            val viewModel: SettingsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            SettingsScreen(
                onLoginClick = { navController.navigate(Routes.Auth) },
                onLogoutClick = viewModel::onLogout,
                onBackClick = { navController.popBackStack() },
                uiState = uiState,
                onSyncClick = viewModel::onSyncNow,
                onThemeChange = viewModel::onThemeChanged
            )
        }

        composable<Routes.Auth> {
            val viewModel: AuthViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            AuthScreen(
                onAuthClick = {
                    navController.popBackStack()
                },
                uiState = uiState,
                onUsernameChanged = viewModel::onUsernameChanged,
                onPasswordChanged = viewModel::onPasswordChanged,
                onLoginClick = viewModel::onLoginClick,
                onRegisterClick = viewModel::onRegisterClick
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

            val route: Routes.SetDetails = it.toRoute()

            SetDetailsScreen(
                onLearningModeClick = {
                    navController.navigate(Routes.LearningMode(setId = route.setId))
                },
                onAddCardClick = {
                    navController.navigate(Routes.AddCard(setId = route.setId))
                },
                onBackClick = { navController.popBackStack() },
                uiState = uiState,
                onDeleteCard = viewModel::onDeleteCard,
                onStartEditing = viewModel::onStartEditing,
                updateDraftName = viewModel::updateDraftName,
                updateDraftDescription = viewModel::updateDraftDescription,
                updateDraftIcon = viewModel::updateDraftIcon,
                updateDraftCard = viewModel::updateDraftCard,
                onCancelEditing = viewModel::onCancelEditing,
                onSaveChanges = viewModel::onSaveChanges
            )
        }

        composable<Routes.LearningMode> {
            val viewModel: LearningModeViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            LearningModeScreen(
                onBackClick = { navController.popBackStack() },
                uiState = uiState,
                restartGame = viewModel::restartLearning,
                onFlipCard = viewModel::onFlipCard,
                onSwipeRight = viewModel::onSwipeRight,
                onSwipeLeft = viewModel::onSwipeLeft,
                toggleShuffle = viewModel::toggleShuffle
            )
        }

        composable<Routes.AddCard> {
            val viewModel: AddCardViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            AddCardScreen(
                onCardCreatedClick = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() },
                uiState = uiState,
                onTermChanged = viewModel::onTermChanged,
                onDefinitionChanged = viewModel::onDefinitionChanged,
                onCreateButtonClicked = viewModel::onCreateButtonClicked
            )
        }
    }
}