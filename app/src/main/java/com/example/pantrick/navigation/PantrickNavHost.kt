// [Materi: NavHost & Jetpack Navigation Compose] Pengaturan alur navigasi terpusat berbasis Type-Safe Navigation
package com.example.pantrick.navigation

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.pantrick.R
import com.example.pantrick.data.model.StorageLocation
import com.example.pantrick.ui.component.PantrickBottomBar
import com.example.pantrick.ui.screen.AddIngredientScreen
import com.example.pantrick.ui.screen.EditIngredientScreen
import com.example.pantrick.ui.screen.HomeScreen
import com.example.pantrick.ui.screen.LoginScreen
import com.example.pantrick.ui.screen.PantryScreen
import com.example.pantrick.ui.screen.PlaceholderTabScreen
import com.example.pantrick.ui.screen.ProfileScreen
import com.example.pantrick.ui.screen.SignUpScreen
import com.example.pantrick.ui.viewmodel.AuthViewModel
import com.example.pantrick.ui.viewmodel.PantryViewModel

private const val TAG = "PantrickNav"

// [Materi: NavHost Composable] Single NavHost dengan ViewModel Scoped ke Activity level
@Composable
fun PantrickNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    pantryViewModel: PantryViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    // [Materi: StateFlow Observation] Mengamati currentUser dan items pantry secara reaktif
    val currentUser by authViewModel.currentUser.collectAsState()
    val pantryItems by pantryViewModel.items.collectAsState()

    // [Materi: Dynamic Data Synchronization] Memuat bahan setiap kali user aktif berganti
    LaunchedEffect(currentUser) {
        pantryViewModel.loadItemsForUser(currentUser?.email)
    }

    // [Materi: Initial Route Determination] Tentukan startDestination sekali di awal: ada sesi -> HomeRoute, tidak ada -> LoginRoute
    val initialDestination = remember {
        if (authViewModel.hasActiveSession()) HomeRoute else LoginRoute
    }

    // [Materi: BackStack Entry Observation] Mengamati rute aktif
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // [Materi: Type-Safe Route Matching via hasRoute]
    val isHomeTab = currentDestination?.hasRoute<HomeRoute>() == true
    val isPantryTab = currentDestination?.hasRoute<PantryRoute>() == true
    val isAddTab = currentDestination?.hasRoute<AddRoute>() == true
    val isRecipesTab = currentDestination?.hasRoute<RecipesRoute>() == true
    val isProfileTab = currentDestination?.hasRoute<ProfileRoute>() == true

    val isTabDestination = isHomeTab || isPantryTab || isAddTab || isRecipesTab || isProfileTab

    // [Materi: Dialog Konfirmasi Keluar Aplikasi di Halaman Beranda]
    var showExitDialog by remember { mutableStateOf(false) }
    val activity = LocalContext.current as? Activity

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.exit_app_dialog_title),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.exit_app_dialog_desc),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        activity?.finish()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_exit),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }

    // [Materi: Scaffold di Level NavHost] Satu Scaffold utama membungkus NavHost dan PantrickBottomBar
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (isTabDestination) {
                val currentTab: Any = when {
                    isHomeTab -> HomeRoute
                    isPantryTab -> PantryRoute
                    isAddTab -> AddRoute
                    isRecipesTab -> RecipesRoute
                    isProfileTab -> ProfileRoute
                    else -> HomeRoute
                }

                PantrickBottomBar(
                    currentRoute = currentTab,
                    onItemClick = { targetRoute ->
                        Log.d(TAG, "Navigating to tab: ${targetRoute::class.simpleName}")
                        navController.navigate(targetRoute) {
                            popUpTo(HomeRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = initialDestination
        ) {
            // [Materi: composable<Route>] Destinasi Layar Masuk (Login)
            composable<LoginRoute> {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToSignUp = {
                        Log.d(TAG, "Navigating from Login to SignUp")
                        navController.navigate(SignUpRoute)
                    },
                    onLoginSuccess = {
                        Log.d(TAG, "Login success -> Navigating to HomeRoute")
                        navController.navigate(HomeRoute) {
                            popUpTo(LoginRoute) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // [Materi: composable<Route>] Destinasi Layar Daftar (Sign Up)
            composable<SignUpRoute> {
                SignUpScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = {
                        Log.d(TAG, "Navigating back from SignUp to Login")
                        navController.popBackStack()
                    },
                    onSignUpSuccess = {
                        Log.d(TAG, "Sign Up success -> Navigating to HomeRoute")
                        navController.navigate(HomeRoute) {
                            popUpTo(LoginRoute) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // [Materi: composable<Route>] Destinasi Tab Beranda (Home)
            composable<HomeRoute> {
                BackHandler(enabled = true) {
                    showExitDialog = true
                }

                HomeScreen(
                    currentUser = currentUser,
                    items = pantryItems,
                    contentPadding = innerPadding,
                    onNotificationClick = {
                        Log.d(TAG, "Notification bell clicked")
                    },
                    onProfileClick = {
                        Log.d(TAG, "Navigating from Home to ProfileRoute")
                        navController.navigate(ProfileRoute) {
                            popUpTo(HomeRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToAdd = {
                        Log.d(TAG, "Navigating from Home to AddRoute")
                        navController.navigate(AddRoute) {
                            popUpTo(HomeRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToPantry = {
                        Log.d(TAG, "Navigating from Home to PantryRoute")
                        navController.navigate(PantryRoute) {
                            popUpTo(HomeRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // [Materi: composable<Route>] Destinasi Tab Pantry
            composable<PantryRoute> {
                PantryScreen(
                    currentUser = currentUser,
                    items = pantryItems,
                    pantryViewModel = pantryViewModel,
                    onNavigateToAdd = { location ->
                        Log.d(TAG, "Navigating from Pantry to AddRoute with location: $location")
                        navController.navigate(AddRoute(location.name)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToEdit = { itemId ->
                        Log.d(TAG, "Navigating from Pantry to EditIngredientRoute for item: $itemId")
                        navController.navigate(EditIngredientRoute(itemId)) {
                            launchSingleTop = true
                        }
                    },
                    onPlanMealClick = {
                        Log.d(TAG, "Navigating from Pantry to RecipesRoute")
                        navController.navigate(RecipesRoute) {
                            popUpTo(HomeRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onProfileClick = {
                        Log.d(TAG, "Navigating from Pantry to ProfileRoute")
                        navController.navigate(ProfileRoute) {
                            popUpTo(HomeRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    contentPadding = innerPadding
                )
            }

            // [Materi: composable<Route>] Destinasi Tab Tambah Bahan
            composable<AddRoute> { backStackEntry ->
                val addRoute = backStackEntry.toRoute<AddRoute>()
                val initialLoc = StorageLocation.entries.find { it.name == addRoute.initialLocation }
                    ?: StorageLocation.KULKAS

                AddIngredientScreen(
                    currentUser = currentUser,
                    pantryViewModel = pantryViewModel,
                    initialLocation = initialLoc,
                    onSaveSuccess = {
                        Log.d(TAG, "Item saved successfully -> Navigating back")
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        Log.d(TAG, "Navigating back from AddIngredient")
                        navController.popBackStack()
                    },
                    contentPadding = innerPadding
                )
            }

            // [Materi: composable<Route>] Destinasi Layar Edit Bahan
            composable<EditIngredientRoute> { backStackEntry ->
                val editRoute = backStackEntry.toRoute<EditIngredientRoute>()
                EditIngredientScreen(
                    itemId = editRoute.itemId,
                    currentUser = currentUser,
                    pantryViewModel = pantryViewModel,
                    onSaveSuccess = {
                        Log.d(TAG, "Item updated successfully -> Navigating back")
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        Log.d(TAG, "Navigating back from EditIngredient")
                        navController.popBackStack()
                    },
                    contentPadding = innerPadding
                )
            }

            // [Materi: composable<Route>] Destinasi Tab Resep
            composable<RecipesRoute> {
                PlaceholderTabScreen(
                    title = stringResource(R.string.recipes_screen_title),
                    description = stringResource(R.string.recipes_screen_desc),
                    contentPadding = innerPadding
                )
            }

            // [Materi: composable<Route>] Destinasi Tab Profil
            composable<ProfileRoute> {
                ProfileScreen(
                    currentUser = currentUser,
                    items = pantryItems,
                    itemsCount = pantryItems.size,
                    authViewModel = authViewModel,
                    onLogoutConfirmed = {
                        Log.d(TAG, "Logout confirmed -> Clearing backstack and navigating to LoginRoute")
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    contentPadding = innerPadding
                )
            }
        }
    }
}
