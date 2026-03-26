package com.ludoblitz.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ludoblitz.app.ui.screens.*

@Composable
fun LudoBlitzNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) +
                    slideIntoContainer(
                        animationSpec = tween(300, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) +
                    slideOutOfContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.End
                    )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) +
                    slideIntoContainer(
                        animationSpec = tween(300, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.End
                    )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) +
                    slideOutOfContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    )
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToMainMenu = {
                    navController.navigate(Screen.MainMenu.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.MainMenu.route) {
            MainMenuScreen(
                onNavigateToGameMode = {
                    navController.navigate(Screen.GameModeSelection.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToShop = {
                    navController.navigate(Screen.Shop.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToLeaderboard = {
                    navController.navigate(Screen.Leaderboard.route)
                },
                onNavigateToDailyRewards = {
                    navController.navigate(Screen.DailyRewards.route)
                }
            )
        }

        composable(Screen.GameModeSelection.route) {
            GameModeSelectionScreen(
                onNavigateToPlayerSetup = { gameMode, difficulty ->
                    navController.navigate("${Screen.PlayerSetup.route}?mode=$gameMode&difficulty=$difficulty")
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PlayerSetup.route) { backStackEntry ->
            val gameMode = backStackEntry.arguments?.getString("mode") ?: "classic"
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "medium"

            PlayerSetupScreen(
                gameMode = gameMode,
                difficulty = difficulty,
                onNavigateToGame = {
                    navController.navigate(Screen.GameBoard.route) {
                        popUpTo(Screen.MainMenu.route)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.GameBoard.route) {
            GameBoardScreen(
                onNavigateToResult = { winner ->
                    navController.navigate("${Screen.Result.route}?winner=$winner") {
                        popUpTo(Screen.MainMenu.route)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToThemeSelection = {
                    navController.navigate(Screen.ThemeSelection.route)
                }
            )
        }

        composable(Screen.Result.route) { backStackEntry ->
            val winner = backStackEntry.arguments?.getString("winner") ?: ""

            ResultScreen(
                winner = winner,
                onPlayAgain = {
                    navController.navigate(Screen.GameBoard.route) {
                        popUpTo(Screen.MainMenu.route)
                    }
                },
                onNavigateToMainMenu = {
                    navController.navigate(Screen.MainMenu.route) {
                        popUpTo(Screen.MainMenu.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Shop.route) {
            ShopScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAvatarSelection = {
                    navController.navigate(Screen.AvatarSelection.route)
                }
            )
        }

        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.DailyRewards.route) {
            DailyRewardsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ThemeSelection.route) {
            ThemeSelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AvatarSelection.route) {
            AvatarSelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.RulesCustomization.route) {
            RulesCustomizationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
