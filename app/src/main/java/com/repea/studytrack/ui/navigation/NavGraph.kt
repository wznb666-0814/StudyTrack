package com.repea.studytrack.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.repea.studytrack.ui.screens.*
import com.repea.studytrack.ui.navigation.Screen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
    onOnboardingComplete: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(160, easing = FastOutSlowInEasing)) +
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> (fullWidth * 0.08f).toInt() },
                    animationSpec = tween(160, easing = FastOutSlowInEasing)
                )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(140, easing = FastOutSlowInEasing)) +
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -(fullWidth * 0.06f).toInt() },
                    animationSpec = tween(140, easing = FastOutSlowInEasing)
                )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(160, easing = FastOutSlowInEasing)) +
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -(fullWidth * 0.08f).toInt() },
                    animationSpec = tween(160, easing = FastOutSlowInEasing)
                )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(140, easing = FastOutSlowInEasing)) +
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> (fullWidth * 0.06f).toInt() },
                    animationSpec = tween(140, easing = FastOutSlowInEasing)
                )
        }
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController, onFinish = onOnboardingComplete)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.SubjectList.route) {
            SubjectListScreen(navController = navController)
        }
        composable(Screen.ExamList.route) {
            ExamListScreen(navController = navController)
        }
        composable(Screen.AddExam.route) {
            AddExamScreen(navController = navController)
        }
        composable(Screen.BatchAddExam.route) {
            BatchAddExamScreen(navController = navController)
        }
        composable(
            route = Screen.EditExam.route,
            arguments = listOf(navArgument("recordId") { type = NavType.IntType })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getInt("recordId")
            AddExamScreen(navController = navController, recordId = recordId)
        }
        composable(Screen.Analysis.route) {
            AnalysisScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.Personalization.route) {
            PersonalizationScreen(navController = navController)
        }
        composable(
            route = Screen.AiChat.route,
            arguments = listOf(navArgument("subjectId") { type = NavType.IntType })
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: -1
            AiChatScreen(navController = navController, subjectId = subjectId)
        }
        composable(Screen.Donate.route) {
            DonateScreen(navController = navController)
        }
    }
}
