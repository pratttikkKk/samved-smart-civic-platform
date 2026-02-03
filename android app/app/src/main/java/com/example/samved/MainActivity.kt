package com.example.samved

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.samved.navigation.NavGraph
import com.example.samved.ui.theme.SAMVEDTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SAMVEDTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    val navController = rememberNavController()
    NavGraph(navController)
}
// errors
// new_complaint:: page photo form desktop or mobile gallery or from camera not taking it taking it from photos,
// not taking locations , no proper issue type, need to add other issue option , info not submitting
//my_complaint :: after clicking button app going off, not showing photos for proof,
//showing same records in every ones account. need a one my account complaint and 1 for all other with repost option
//admin page::should be different page for ward wise complaints and rating, column representation of total ward wise data
