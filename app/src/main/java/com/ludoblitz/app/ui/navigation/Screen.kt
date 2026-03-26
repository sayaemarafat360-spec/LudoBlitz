package com.ludoblitz.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object MainMenu : Screen("main_menu")
    object GameModeSelection : Screen("game_mode_selection")
    object PlayerSetup : Screen("player_setup")
    object GameBoard : Screen("game_board")
    object Settings : Screen("settings")
    object Result : Screen("result")
    object Shop : Screen("shop")
    object Profile : Screen("profile")
    object Leaderboard : Screen("leaderboard")
    object DailyRewards : Screen("daily_rewards")
    object ThemeSelection : Screen("theme_selection")
    object AvatarSelection : Screen("avatar_selection")
    object RulesCustomization : Screen("rules_customization")
}
