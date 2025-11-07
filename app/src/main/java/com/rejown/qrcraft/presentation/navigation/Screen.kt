package com.rejown.qrcraft.presentation.navigation

sealed class Screen(val route: String) {
    data object Scanner : Screen("scanner")
    data object Generator : Screen("generator")
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object Detail : Screen("detail/{id}/{type}") {
        fun createRoute(id: Long, type: String) = "detail/$id/$type"
    }
}
