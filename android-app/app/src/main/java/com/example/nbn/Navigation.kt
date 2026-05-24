package com.example.nbn

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import com.example.nbn.ui.screens.OnboardingScreen
import com.example.nbn.ui.screens.OrchestratorScreen
import com.example.nbn.ui.screens.PulseScreen
import com.example.nbn.ui.screens.VaultScreen

@Serializable data object Onboarding : NavKey
@Serializable data object Pulse : NavKey
@Serializable data object Orchestrator : NavKey
@Serializable data object Vault : NavKey

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Onboarding)

    val currentRoute = backStack.last()

    if (currentRoute == Onboarding) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<Onboarding> { 
                    OnboardingScreen(onComplete = { 
                        backStack.add(Pulse) 
                    }) 
                }
                entry<Pulse> { PulseScreen() }
                entry<Orchestrator> { OrchestratorScreen() }
                entry<Vault> { VaultScreen() }
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = currentRoute == Pulse,
                        onClick = { backStack.add(Pulse) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Pulse") },
                        label = { Text("Pulse") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Orchestrator,
                        onClick = { backStack.add(Orchestrator) },
                        icon = { Icon(Icons.Default.Chat, contentDescription = "Orchestrator") },
                        label = { Text("Orchestrator") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Vault,
                        onClick = { backStack.add(Vault) },
                        icon = { Icon(Icons.Default.Inventory, contentDescription = "Vault") },
                        label = { Text("Vault") }
                    )
                }
            }
        ) { innerPadding ->
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<Onboarding> { OnboardingScreen(onComplete = { 
                        backStack.add(Pulse) 
                    }) }
                    entry<Pulse> { PulseScreen(Modifier.padding(innerPadding).safeDrawingPadding()) }
                    entry<Orchestrator> { OrchestratorScreen(Modifier.padding(innerPadding).safeDrawingPadding()) }
                    entry<Vault> { VaultScreen(Modifier.padding(innerPadding).safeDrawingPadding()) }
                }
            )
        }
    }
}
