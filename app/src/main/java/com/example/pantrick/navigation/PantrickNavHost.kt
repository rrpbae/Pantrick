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
import com.example.pantrick.ui.screen.AddPantryItemScreen
import com.example.pantrick.ui.screen.ChangePasswordScreen
import com.example.pantrick.ui.screen.EditIngredientScreen
import com.example.pantrick.ui.screen.EditProfileScreen
import com.example.pantrick.ui.screen.HomeScreen
import com.example.pantrick.ui.screen.LoginScreen
import com.example.pantrick.ui.screen.NotificationScreen
import com.example.pantrick.ui.screen.PantryScreen
import com.example.pantrick.ui.screen.ProfileScreen
import com.example.pantrick.ui.screen.SignUpScreen
import com.example.pantrick.ui.screen.RecipesScreen
import com.example.pantrick.ui.screen.RecipeDetailScreen
import com.example.pantrick.ui.viewmodel.AuthViewModel
import com.example.pantrick.ui.viewmodel.PantryViewModel
import com.example.pantrick.ui.viewmodel.RecipeViewModel

private const val TAG = "PantrickNav"

@Composable
fun PantrickNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    pantryViewModel: PantryViewModel = viewModel(),
    recipeViewModel: RecipeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val photoPath by authViewModel.profileImagePath.collectAsState()
    val pantryItems by pantryViewModel.items.collectAsState()
    val savedRecipes by recipeViewModel.savedRecipes.collectAsState()

    LaunchedEffect(currentUser) {
        pantryViewModel.loadItemsForUser(currentUser?.email)
        recipeViewModel.loadSavedRecipesForUser(currentUser?.email)
    }

    val initialDestination = remember {
        if (authViewModel.hasActiveSession()) HomeRoute else LoginRoute
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isHomeTab = currentDestination?.hasRoute<HomeRoute>() == true
    val isPantryTab = currentDestination?.hasRoute<PantryRoute>() == true
    val isAddTab = currentDestination?.hasRoute<AddRoute>() == true
    val isRecipesTab = currentDestination?.hasRoute<RecipesRoute>() == true
    val isProfileTab = currentDestination?.hasRoute<ProfileRoute>() == true

    val isTabDestination = isHomeTab || isPantryTab || isAddTab || isRecipesTab || isProfileTab

    var showExitDialog by remember { mutableStateOf(false) }
    val activity = LocalContext.current as? Activity

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(text = stringResource(R.string.exit_app_dialog_title), color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(text = stringResource(R.string.exit_app_dialog_desc), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { showExitDialog = false; activity?.finish() }) {
                    Text(text = stringResource(R.string.action_exit), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(text = stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (isTabDestination) {
                val currentTab: Any = when {
                    isHomeTab -> HomeRoute
                    isPantryTab -> PantryRoute
                    isAddTab -> AddRoute()
                    isRecipesTab -> RecipesRoute
                    isProfileTab -> ProfileRoute
                    else -> HomeRoute
                }

                PantrickBottomBar(
                    currentRoute = currentTab,
                    onItemClick = { targetRoute ->
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
            composable<LoginRoute> {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToSignUp = { navController.navigate(SignUpRoute) },
                    onLoginSuccess = {
                        navController.navigate(HomeRoute) {
                            popUpTo(LoginRoute) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable<SignUpRoute> {
                SignUpScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = { navController.popBackStack() },
                    onSignUpSuccess = {
                        navController.navigate(HomeRoute) {
                            popUpTo(LoginRoute) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable<HomeRoute> {
                BackHandler(enabled = true) { showExitDialog = true }
                HomeScreen(
                    currentUser = currentUser,
                    items = pantryItems,
                    savedRecipes = savedRecipes,
                    onToggleSaveRecipe = { recipeViewModel.toggleSaveRecipe(it) },
                    photoPath = photoPath,
                    contentPadding = innerPadding,
                    onNotificationClick = { navController.navigate(NotificationRoute) { launchSingleTop = true } },
                    onProfileClick = { navController.navigate(ProfileRoute) { popUpTo(HomeRoute) { saveState = true }; launchSingleTop = true; restoreState = true } },
                    onNavigateToAdd = { navController.navigate(AddPantryItemRoute("KULKAS")) },
                    onNavigateToPantry = { navController.navigate(PantryRoute) { popUpTo(HomeRoute) { saveState = true }; launchSingleTop = true; restoreState = true } },
                    onNavigateToRecipeDetail = { recipeId -> navController.navigate(RecipeDetailRoute(recipeId)) }
                )
            }

            composable<PantryRoute> {
                PantryScreen(
                    currentUser = currentUser,
                    items = pantryItems,
                    pantryViewModel = pantryViewModel,
                    photoPath = photoPath,
                    onNavigateToAdd = { location -> navController.navigate(AddPantryItemRoute(location.name)) },
                    onNavigateToEdit = { itemId -> navController.navigate(EditIngredientRoute(itemId)) { launchSingleTop = true } },
                    onPlanMealClick = { navController.navigate(RecipesRoute) { popUpTo(HomeRoute) { saveState = true }; launchSingleTop = true; restoreState = true } },
                    onProfileClick = { navController.navigate(ProfileRoute) { popUpTo(HomeRoute) { saveState = true }; launchSingleTop = true; restoreState = true } },
                    onNotificationClick = { navController.navigate(NotificationRoute) { launchSingleTop = true } },
                    contentPadding = innerPadding
                )
            }

            // Rute form input bahan baru di Pantry
            composable<AddPantryItemRoute> { backStackEntry ->
                val addPantryRoute = try {
                    backStackEntry.toRoute<AddPantryItemRoute>()
                } catch (e: Exception) {
                    null
                }
                val initialLoc = StorageLocation.entries.find { it.name == addPantryRoute?.initialLocation } ?: StorageLocation.KULKAS

                AddPantryItemScreen(
                    currentUser = currentUser,
                    pantryViewModel = pantryViewModel,
                    initialLocation = initialLoc,
                    onSaveSuccess = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() },
                    contentPadding = innerPadding
                )
            }

            // Rute AddRoute untuk Recipe Matcher (Tombol + di footer navigasi bawah)
            composable<AddRoute> {
                AddIngredientScreen(
                    currentUser = currentUser,
                    pantryViewModel = pantryViewModel,
                    savedRecipes = savedRecipes,
                    onToggleSaveRecipe = { recipeViewModel.toggleSaveRecipe(it) },
                    photoPath = photoPath,
                    onNavigateBack = { navController.popBackStack() },
                    onNotificationClick = { navController.navigate(NotificationRoute) { launchSingleTop = true } },
                    onProfileClick = { navController.navigate(ProfileRoute) { popUpTo(HomeRoute) { saveState = true }; launchSingleTop = true; restoreState = true } },
                    onNavigateToRecipeDetail = { recipeId ->
                        navController.navigate(RecipeDetailRoute(recipeId))
                    },
                    contentPadding = innerPadding
                )
            }

            composable<NotificationRoute> {
                NotificationScreen(
                    items = pantryItems,
                    onNavigateBack = { navController.popBackStack() },
                    contentPadding = innerPadding
                )
            }

            composable<EditIngredientRoute> { backStackEntry ->
                val editRoute = backStackEntry.toRoute<EditIngredientRoute>()
                EditIngredientScreen(
                    itemId = editRoute.itemId,
                    currentUser = currentUser,
                    pantryViewModel = pantryViewModel,
                    onSaveSuccess = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() },
                    contentPadding = innerPadding
                )
            }

            composable<RecipesRoute> {
                RecipesScreen(
                    currentUser = currentUser,
                    savedRecipes = savedRecipes,
                    onToggleSaveRecipe = { recipeViewModel.toggleSaveRecipe(it) },
                    photoPath = photoPath,
                    onNotificationClick = { navController.navigate(NotificationRoute) { launchSingleTop = true } },
                    onProfileClick = { navController.navigate(ProfileRoute) { popUpTo(HomeRoute) { saveState = true }; launchSingleTop = true; restoreState = true } },
                    contentPadding = innerPadding,
                    onNavigateToDetail = { recipeId ->
                        navController.navigate(RecipeDetailRoute(recipeId))
                    }
                )
            }

            composable<RecipeDetailRoute> { backStackEntry ->
                val detailRoute = backStackEntry.toRoute<RecipeDetailRoute>()
                RecipeDetailScreen(
                    recipeId = detailRoute.recipeId,
                    savedRecipes = savedRecipes,
                    pantryItems = pantryItems,
                    contentPadding = innerPadding,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable<ProfileRoute> {
                ProfileScreen(
                    currentUser = currentUser,
                    items = pantryItems,
                    itemsCount = pantryItems.size,
                    authViewModel = authViewModel,
                    onEditProfileClick = {
                        navController.navigate(EditProfileRoute)
                    },
                    onChangePasswordClick = {
                        navController.navigate(ChangePasswordRoute)
                    },
                    onLogoutConfirmed = {
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    contentPadding = innerPadding
                )
            }

            composable<EditProfileRoute> {
                EditProfileScreen(
                    currentUser = currentUser,
                    initialPhotoPath = authViewModel.getProfileImagePath(),
                    onSave = { newName, newPhotoPath ->
                        authViewModel.updateProfileName(newName)
                        authViewModel.updateProfilePhoto(newPhotoPath)
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    contentPadding = innerPadding
                )
            }

            composable<ChangePasswordRoute> {
                ChangePasswordScreen(
                    authViewModel = authViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    contentPadding = innerPadding
                )
            }
        }
    }
}