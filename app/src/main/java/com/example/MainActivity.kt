package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.screens.OpeningScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FilterMode
import com.example.ui.viewmodel.SomityViewModel

enum class AppScreen {
    OPENING,
    DASHBOARD
}

class MainActivity : ComponentActivity() {
    private val viewModel: SomityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.OPENING) }

                    Crossfade(
                        targetState = currentScreen,
                        animationSpec = tween(durationMillis = 150),
                        label = "screen_transition"
                    ) { target ->
                        when (target) {
                            AppScreen.OPENING -> {
                                OpeningScreen(
                                    onEnterClick = { currentScreen = AppScreen.DASHBOARD },
                                    onNavigateToAddMember = {
                                        viewModel.openAddMemberDialog()
                                        currentScreen = AppScreen.DASHBOARD
                                    },
                                    onNavigateToSearch = {
                                        viewModel.setFilter(FilterMode.ALL)
                                        currentScreen = AppScreen.DASHBOARD
                                    },
                                    onNavigateToList = {
                                        viewModel.setFilter(FilterMode.ALL)
                                        currentScreen = AppScreen.DASHBOARD
                                    },
                                    onNavigateToDue = {
                                        viewModel.setFilter(FilterMode.OVERDUE_ONLY)
                                        currentScreen = AppScreen.DASHBOARD
                                    },
                                    onNavigateToSummary = {
                                        viewModel.openSummaryDialog()
                                        currentScreen = AppScreen.DASHBOARD
                                    }
                                )
                            }
                            AppScreen.DASHBOARD -> {
                                MainDashboardScreen(
                                    viewModel = viewModel,
                                    onBackToOpening = { currentScreen = AppScreen.OPENING }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}

