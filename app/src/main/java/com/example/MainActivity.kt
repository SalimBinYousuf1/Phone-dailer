package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.entity.ContactEntity
import com.example.telecom.CallManager
import com.example.telecom.CallState
import com.example.ui.screens.BlockedNumbersScreen
import com.example.ui.screens.ContactDetailScreen
import com.example.ui.screens.ContactEditScreen
import com.example.ui.screens.ContactsListScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.InCallScreen
import com.example.ui.screens.KeypadScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.RecentsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UnifiedSearchScreen
import com.example.ui.screens.VoicemailScreen
import com.example.ui.theme.LocalVercelColors
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VercelMono
import com.example.ui.theme.VercelSpacing
import com.example.ui.viewmodel.AppViewModelFactory
import com.example.ui.viewmodel.BlockedNumbersViewModel
import com.example.ui.viewmodel.ContactsViewModel
import com.example.ui.viewmodel.DialerViewModel
import com.example.ui.viewmodel.InCallViewModel
import com.example.ui.viewmodel.RecentsViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.VoicemailViewModel

enum class MainTab(val route: String, val title: String, val icon: ImageVector) {
    KEYPAD("keypad", "KEYPAD", Icons.Default.Dialpad),
    RECENTS("recents", "RECENTS", Icons.Default.History),
    CONTACTS("contacts", "CONTACTS", Icons.Default.People),
    FAVORITES("favorites", "FAVORITES", Icons.Default.Star),
    VOICEMAIL("voicemail", "VOICEMAIL", Icons.Default.Voicemail)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as DialerApplication).container
        val factory = AppViewModelFactory(appContainer)

        setContent {
            val settingsVm: SettingsViewModel = viewModel(factory = factory)
            val settingsState by settingsVm.uiState.collectAsState()

            val isSystemDark = isSystemInDarkTheme()
            val useDarkTheme = when (settingsState.themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemDark
            }

            MyApplicationTheme(darkTheme = useDarkTheme) {
                DialerApp(
                    factory = factory,
                    intent = intent
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
fun DialerApp(
    factory: AppViewModelFactory,
    intent: Intent?
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: MainTab.KEYPAD.route

    val dialerVm: DialerViewModel = viewModel(factory = factory)
    val recentsVm: RecentsViewModel = viewModel(factory = factory)
    val contactsVm: ContactsViewModel = viewModel(factory = factory)
    val voicemailVm: VoicemailViewModel = viewModel(factory = factory)
    val blockedVm: BlockedNumbersViewModel = viewModel(factory = factory)
    val settingsVm: SettingsViewModel = viewModel(factory = factory)
    val inCallVm: InCallViewModel = viewModel(factory = factory)

    val callState by inCallVm.callState.collectAsState()
    var selectedContact by remember { mutableStateOf<ContactEntity?>(null) }
    var editContactInitialPhone by remember { mutableStateOf("") }

    // Handle intent tel: numbers if launched via external dialer intent
    LaunchedEffect(intent) {
        intent?.data?.let { uri ->
            if (uri.scheme == "tel") {
                val number = uri.schemeSpecificPart
                if (!number.isNullOrBlank()) {
                    dialerVm.onPasteNumber(number)
                    navController.navigate(MainTab.KEYPAD.route) {
                        popUpTo(MainTab.KEYPAD.route) { inclusive = true }
                    }
                }
            }
        }
    }

    val vercelColors = LocalVercelColors.current
    val showBottomBar = currentRoute in MainTab.entries.map { it.route }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    VercelBottomNavigation(
                        currentRoute = currentRoute,
                        onTabSelected = { tab ->
                            if (currentRoute != tab.route) {
                                navController.navigate(tab.route) {
                                    popUpTo(MainTab.KEYPAD.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = MainTab.KEYPAD.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(MainTab.KEYPAD.route) {
                    KeypadScreen(
                        viewModel = dialerVm,
                        onNavigateToSearch = { navController.navigate("search") },
                        onNavigateToBlocked = { navController.navigate("blocked") },
                        onNavigateToSettings = { navController.navigate("settings") },
                        onAddContact = { number ->
                            selectedContact = null
                            editContactInitialPhone = number
                            navController.navigate("contact_edit")
                        },
                        onContactClick = { contact ->
                            selectedContact = contact
                            navController.navigate("contact_detail")
                        }
                    )
                }

                composable(MainTab.RECENTS.route) {
                    RecentsScreen(
                        viewModel = recentsVm,
                        onNavigateToSearch = { navController.navigate("search") }
                    )
                }

                composable(MainTab.CONTACTS.route) {
                    ContactsListScreen(
                        viewModel = contactsVm,
                        onContactClick = { contact ->
                            selectedContact = contact
                            navController.navigate("contact_detail")
                        },
                        onAddContactClick = {
                            selectedContact = null
                            editContactInitialPhone = ""
                            navController.navigate("contact_edit")
                        },
                        onNavigateToSearch = { navController.navigate("search") }
                    )
                }

                composable(MainTab.FAVORITES.route) {
                    FavoritesScreen(
                        viewModel = contactsVm,
                        onContactClick = { contact ->
                            selectedContact = contact
                            navController.navigate("contact_detail")
                        },
                        onNavigateToSearch = { navController.navigate("search") }
                    )
                }

                composable(MainTab.VOICEMAIL.route) {
                    VoicemailScreen(
                        viewModel = voicemailVm
                    )
                }

                composable("contact_detail") {
                    selectedContact?.let { contact ->
                        ContactDetailScreen(
                            contact = contact,
                            onBack = { navController.popBackStack() },
                            onEdit = { navController.navigate("contact_edit") },
                            onCall = { contactsVm.callContact(navController.context, contact) },
                            onDelete = {
                                contactsVm.deleteContact(contact)
                                navController.popBackStack()
                            },
                            onBlock = {
                                contactsVm.blockContact(contact)
                                navController.popBackStack()
                            },
                            onToggleFavorite = { contactsVm.toggleFavorite(contact) }
                        )
                    } ?: run {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }

                composable("contact_edit") {
                    ContactEditScreen(
                        contact = selectedContact,
                        initialPhone = editContactInitialPhone,
                        onCancel = { navController.popBackStack() },
                        onSave = { name, phone, label, email, company, notes, isFav ->
                            contactsVm.saveContact(name, phone, label, email, company, notes, isFav)
                            navController.popBackStack()
                        }
                    )
                }

                composable("search") {
                    UnifiedSearchScreen(
                        contactsViewModel = contactsVm,
                        recentsViewModel = recentsVm,
                        onContactClick = { contact ->
                            selectedContact = contact
                            navController.navigate("contact_detail")
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("blocked") {
                    BlockedNumbersScreen(
                        viewModel = blockedVm,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        viewModel = settingsVm,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("onboarding") {
                    OnboardingScreen(
                        onContinue = { navController.popBackStack() }
                    )
                }
            }
        }

        // Full Screen In-Call UI Overlay (appears automatically whenever a call is active or ringing)
        AnimatedVisibility(
            visible = callState !is CallState.Idle,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            InCallScreen(
                viewModel = inCallVm,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun VercelBottomNavigation(
    currentRoute: String,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val vercelColors = LocalVercelColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .border(width = 1.dp, color = vercelColors.border)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = VercelSpacing.Space8),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (tab in MainTab.entries) {
                val isSelected = currentRoute == tab.route
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onSurface else vercelColors.textMuted

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = VercelSpacing.Space8, vertical = VercelSpacing.Space4)
                        .testTag("nav_tab_${tab.route}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.title,
                        style = VercelMono.Badge.copy(fontSize = 9.sp),
                        color = contentColor
                    )
                }
            }
        }
    }
}
