package app.xiguang.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.xiguang.R
import app.xiguang.XiguangApplication
import app.xiguang.collection.CollectionRoute
import app.xiguang.collection.detail.CollectionDetailRoute
import app.xiguang.collection.edit.CollectionEditRoute
import app.xiguang.collection.folder.FolderManagementRoute
import app.xiguang.collection.list.CollectionListRoute
import app.xiguang.collection.reader.CollectionReaderRoute
import app.xiguang.collection.search.CollectionSearchRoute
import app.xiguang.domain.model.ThemePreference
import app.xiguang.projects.ProjectsRoute
import app.xiguang.settings.SettingsRoute
import app.xiguang.today.TodayRoute
import app.xiguang.ui.theme.XiguangTheme
import androidx.compose.foundation.isSystemInDarkTheme

private enum class TopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    TODAY(
        route = AppDestination.TODAY,
        labelRes = R.string.nav_today,
        icon = Icons.Outlined.Home,
    ),
    PROJECTS(
        route = AppDestination.PROJECTS,
        labelRes = R.string.nav_projects,
        icon = Icons.Outlined.Subscriptions,
    ),
    COLLECTION(
        route = AppDestination.COLLECTION,
        labelRes = R.string.nav_collection,
        icon = Icons.Outlined.BookmarkBorder,
    ),
    SETTINGS(
        route = AppDestination.SETTINGS,
        labelRes = R.string.nav_settings,
        icon = Icons.Outlined.Settings,
    ),
}

@Composable
fun XiguangApp() {
    val application = LocalContext.current.applicationContext as XiguangApplication
    val settings by application.settingsRepository.settings.collectAsState(initial = app.xiguang.domain.model.AppSettings())
    val darkTheme = when (settings.theme) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }
    XiguangTheme(darkTheme = darkTheme) { XiguangAppContent() }
}

@Composable
private fun XiguangAppContent() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppDestination.COLLECTION
    val selectedRoute = AppDestination.topLevelRoute(currentRoute)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (AppDestination.showsBottomBar(currentRoute)) {
                XiguangBottomBar(
                    selectedRoute = selectedRoute,
                    onNavigate = navController::navigateToTopLevel,
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.COLLECTION,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppDestination.TODAY) {
                TodayRoute { collectionId -> navController.navigate(AppDestination.collectionDetail(collectionId)) }
            }
            composable(AppDestination.PROJECTS) {
                ProjectsRoute()
            }
            composable(AppDestination.COLLECTION) {
                CollectionRoute(
                    onSearch = {
                        navController.navigate(AppDestination.COLLECTION_SEARCH) {
                            launchSingleTop = true
                        }
                    },
                    onOpenGroup = { group ->
                        navController.navigate(
                            AppDestination.collectionList(
                                groupKey = group.key,
                                title = group.title,
                            ),
                        )
                    },
                    onManageFolders = {
                        navController.navigate(AppDestination.FOLDER_MANAGEMENT)
                    },
                )
            }
            composable(
                route = AppDestination.COLLECTION_LIST,
                arguments = listOf(
                    navArgument("groupKey") { defaultValue = "" },
                    navArgument("title") { defaultValue = "" },
                ),
            ) { entry ->
                CollectionListRoute(
                    onBack = navController::popBackStack,
                    onOpenCollection = { collectionId ->
                        navController.navigate(AppDestination.collectionDetail(collectionId))
                    },
                )
            }
            composable(AppDestination.COLLECTION_SEARCH) {
                CollectionSearchRoute(
                    onBack = navController::popBackStack,
                    onOpenCollection = { collectionId ->
                        navController.navigate(AppDestination.collectionDetail(collectionId))
                    },
                )
            }
            composable(
                route = AppDestination.COLLECTION_DETAIL,
                arguments = listOf(navArgument("collectionId") { defaultValue = -1L }),
            ) {
                CollectionDetailRoute(
                    onBack = navController::popBackStack,
                    onReadInApp = { collectionId ->
                        navController.navigate(AppDestination.collectionReader(collectionId))
                    },
                    onEdit = { collectionId ->
                        navController.navigate(AppDestination.collectionEdit(collectionId))
                    },
                )
            }
            composable(
                route = AppDestination.COLLECTION_READER,
                arguments = listOf(navArgument("collectionId") { defaultValue = -1L }),
            ) {
                CollectionReaderRoute(onBack = navController::popBackStack)
            }
            composable(
                route = AppDestination.COLLECTION_EDIT,
                arguments = listOf(navArgument("collectionId") { defaultValue = -1L }),
            ) {
                CollectionEditRoute(
                    onBack = navController::popBackStack,
                    onSaved = { navController.popBackStack() },
                )
            }
            composable(AppDestination.FOLDER_MANAGEMENT) {
                FolderManagementRoute(onBack = navController::popBackStack)
            }
            composable(AppDestination.SETTINGS) {
                SettingsRoute()
            }
        }
    }
}

private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    if (currentDestination?.route == destination.route) return

    navigate(destination.route) {
        popUpTo(AppDestination.COLLECTION) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun FeaturePlaceholderScreen(
    title: String,
    description: String,
    onBack: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 28.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back),
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(top = 44.dp),
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 36.dp),
            color = MaterialTheme.colorScheme.outline,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.Construction,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.navigation_ready),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 18.dp),
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun XiguangBottomBar(
    selectedRoute: String?,
    onNavigate: (TopLevelDestination) -> Unit,
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
                .selectableGroup()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            TopLevelDestination.entries.forEach { destination ->
                BottomDestination(
                    destination = destination,
                    selected = selectedRoute == destination.route,
                    onClick = { onNavigate(destination) },
                )
            }
        }
    }
}

@Composable
private fun BottomDestination(
    destination: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = stringResource(destination.labelRes)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
            )
            .semantics {
                contentDescription = label
            }
            .padding(vertical = 4.dp),
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = null,
            tint = color,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
        )
    }
}
