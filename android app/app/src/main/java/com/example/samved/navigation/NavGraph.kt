package com.example.samved.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.samved.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "welcome") {

        composable("welcome") { WelcomeScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("login") { LoginScreen(navController) }

        composable(
            route = "home/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")!!
            HomeScreen(navController, userId)
        }

        composable(
            route = "newComplaint/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")!!
            NewComplaintScreen(userId)
        }

        composable(
            route = "myComplaints/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")!!
            MyComplaintsScreen(
                userId = userId,
                onGiveFeedback = { complaintId ->
                    navController.navigate("feedback/$complaintId")
                }
            )
        }

        composable(
            route = "allComplaints/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")!!
            AllComplaintsScreen(userId)
        }

        // ✅ FEEDBACK ROUTE (MISSING EARLIER)
        composable(
            route = "feedback/{complaintId}",
            arguments = listOf(navArgument("complaintId") { type = NavType.StringType })
        ) { backStackEntry ->
            val complaintId = backStackEntry.arguments?.getString("complaintId")!!
            FeedbackScreen(complaintId)
        }
    }
}
