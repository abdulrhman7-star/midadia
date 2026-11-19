package com.example.ui.navigation

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.i18n.AppStringsProvider
import com.example.data.model.SupportedLanguage
import com.example.data.repository.GuidesRepository
import com.example.data.repository.MidaddRepository
import com.example.data.repository.TemplatesRepository
import com.example.data.repository.UserPreferencesStore
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.authStateFlow
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.GuidesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.TemplatesScreen
import com.example.ui.theme.MidaddTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable data object EditorRoute
@Serializable data object TemplatesRoute
@Serializable data object GuidesRoute
@Serializable data object ProfileRoute

@Composable
fun AppNavigation(
    prefsStore: UserPreferencesStore,
    auth: FirebaseAuth = Firebase.auth
) {
    val scope = rememberCoroutineScope()
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val unauthLanguage by prefsStore.languageFlow.collectAsStateWithLifecycle(initialValue = SupportedLanguage.AR)
    val unauthDark by prefsStore.darkModeFlow.collectAsStateWithLifecycle(initialValue = false)

    val user = currentUser
    if (user == null) {
        val dir = if (unauthLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
        CompositionLocalProvider(LocalLayoutDirection provides dir) {
            MidaddTheme(darkTheme = unauthDark, isArabic = unauthLanguage == SupportedLanguage.AR) {
                AuthScreen(
                    language = unauthLanguage,
                    isDark = unauthDark,
                    onLanguageSelected = { lang -> scope.launch { prefsStore.setLanguage(lang) } },
                    onToggleDarkMode = { scope.launch { prefsStore.setDarkMode(!unauthDark) } },
                    onAuthSuccess = { /* AuthStateListener drives transition automatically */ }
                )
            }
        }
    } else {
        key(user.uid) {
            AuthenticatedShell(
                currentUserId = user.uid,
                authEmail = user.email.orEmpty(),
                authDisplayName = user.displayName.orEmpty(),
                prefsStore = prefsStore
            )
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun AuthenticatedShell(
    currentUserId: String,
    authEmail: String,
    authDisplayName: String,
    prefsStore: UserPreferencesStore
) {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val repository = MidaddRepository(db)
                val templates = TemplatesRepository.loadTemplates(app)
                val guides = GuidesRepository.GUIDE_ARTICLES
                MainViewModel(
                    repository = repository,
                    prefsStore = prefsStore,
                    templates = templates,
                    guides = guides,
                    currentUserId = currentUserId,
                    authEmail = authEmail,
                    authDisplayName = authDisplayName
                )
            }
        }
    )

    val language by viewModel.language.collectAsStateWithLifecycle()
    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()
    val profileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val savedDocsState by viewModel.savedDocumentsState.collectAsStateWithLifecycle()

    val strings = remember(language) { AppStringsProvider.forLanguage(language) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.snackbarEvents.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination

    val activity = context as? Activity
    val windowWidthClass = if (activity != null) {
        calculateWindowSizeClass(activity).widthSizeClass
    } else {
        WindowWidthSizeClass.Compact
    }
    val useNavRail = windowWidthClass != WindowWidthSizeClass.Compact

    val profile = (profileState as? UiState.Success)?.data
    val savedDocs = (savedDocsState as? UiState.Success)?.data.orEmpty()
    val credits = profile?.credits ?: 25L
    val favoriteIds = profile?.favoriteTemplateIds.orEmpty()

    val layoutDir = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    val navItems = listOf(
        Triple(EditorRoute, strings.navEditor, Pair(Icons.Rounded.Description, Icons.Outlined.Description)),
        Triple(TemplatesRoute, strings.navTemplates, Pair(Icons.Rounded.LibraryBooks, Icons.Outlined.LibraryBooks)),
        Triple(GuidesRoute, strings.navGuides, Pair(Icons.Rounded.AutoStories, Icons.Outlined.AutoStories)),
        Triple(ProfileRoute, strings.navProfile, Pair(Icons.Rounded.AccountCircle, Icons.Outlined.AccountCircle))
    )

    fun navigateTopLevel(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
        MidaddTheme(darkTheme = darkMode, isArabic = language == SupportedLanguage.AR) {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    if (!useNavRail) {
                        NavigationBar {
                            navItems.forEach { (route, label, icons) ->
                                val selected = currentDest?.hasRoute(route::class) == true
                                val routeTag = when (route) {
                                    is EditorRoute -> "nav_editor"
                                    is TemplatesRoute -> "nav_templates"
                                    is GuidesRoute -> "nav_guides"
                                    else -> "nav_profile"
                                }
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTopLevel(route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) icons.first else icons.second,
                                            contentDescription = label
                                        )
                                    },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag(routeTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavRail) {
                        NavigationRail(
                            modifier = Modifier
                                .fillMaxHeight()
                                .windowInsetsPadding(WindowInsets(0))
                        ) {
                            navItems.forEach { (route, label, icons) ->
                                val selected = currentDest?.hasRoute(route::class) == true
                                val routeTag = when (route) {
                                    is EditorRoute -> "rail_editor"
                                    is TemplatesRoute -> "rail_templates"
                                    is GuidesRoute -> "rail_guides"
                                    else -> "rail_profile"
                                }
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTopLevel(route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) icons.first else icons.second,
                                            contentDescription = label
                                        )
                                    },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag(routeTag)
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        val profileError = (profileState as? UiState.Error)?.message
                        val docsError = (savedDocsState as? UiState.Error)?.message
                        val errorText = profileError ?: docsError
                        if (errorText != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                            ) {
                                Text(
                                    text = errorText,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        if (profileState is UiState.Loading && savedDocsState is UiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }

                        NavHost(
                            navController = navController,
                            startDestination = EditorRoute,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable<EditorRoute> {
                                EditorScreen(
                                    viewModel = viewModel,
                                    editorState = editorState,
                                    savedDocuments = savedDocs,
                                    credits = credits,
                                    language = language,
                                    isDark = darkMode
                                )
                            }
                            composable<TemplatesRoute> {
                                TemplatesScreen(
                                    viewModel = viewModel,
                                    favoriteIds = favoriteIds,
                                    language = language,
                                    onNavigateToEditor = { navigateTopLevel(EditorRoute) }
                                )
                            }
                            composable<GuidesRoute> {
                                GuidesScreen(
                                    viewModel = viewModel,
                                    language = language,
                                    onInsertExampleToEditor = { snippet ->
                                        viewModel.insertSnippet(snippet)
                                        navigateTopLevel(EditorRoute)
                                    }
                                )
                            }
                            composable<ProfileRoute> {
                                ProfileScreen(
                                    viewModel = viewModel,
                                    profile = profile,
                                    savedDocuments = savedDocs,
                                    language = language,
                                    isDark = darkMode,
                                    onOpenSavedDocument = { doc ->
                                        viewModel.loadSavedDocument(doc)
                                        navigateTopLevel(EditorRoute)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
