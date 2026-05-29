package com.example

import android.os.Bundle
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.content.Intent
import android.app.PendingIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AddCardScreen
import com.example.ui.screens.CardDetailsScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.OmniPassViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private var mViewModel: OmniPassViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize modern State ViewModel
                val viewModel: OmniPassViewModel = viewModel()
                mViewModel = viewModel
                
                val currentScreen by viewModel.currentScreen.collectAsState()
                val lang by viewModel.appLanguage.collectAsState()
                val themeAccentHex by viewModel.customAccentColor.collectAsState()
                val accentColor = remember(themeAccentHex) {
                    try {
                        Color(android.graphics.Color.parseColor(themeAccentHex))
                    } catch (_: Exception) {
                        Color(0xFFFF6B00)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Display customizable custom Bottom Navigation Bar only on core screens
                        if (currentScreen == Screen.Dashboard || currentScreen == Screen.Finance || currentScreen == Screen.Profile) {
                            NavigationBar(
                                modifier = Modifier
                                    .testTag("bottom_nav_bar")
                                    .windowInsetsPadding(WindowInsets.navigationBars),
                                containerColor = Color(0xFF0C1420),
                                tonalElevation = 8.dp
                            ) {
                                // Tab 1: Dashboard
                                NavigationBarItem(
                                    selected = currentScreen == Screen.Dashboard,
                                    onClick = { viewModel.navigateTo(Screen.Dashboard) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.CreditCard,
                                            contentDescription = "Cards"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = if (lang == "ru") "Карты" else "Cards",
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = accentColor,
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray,
                                        indicatorColor = accentColor
                                    ),
                                    modifier = Modifier.testTag("nav_cards_tab")
                                )

                                // Tab 2: Finance
                                NavigationBarItem(
                                    selected = currentScreen == Screen.Finance,
                                    onClick = { viewModel.navigateTo(Screen.Finance) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Wallet,
                                            contentDescription = "Finance"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = if (lang == "ru") "Финансы" else "Finance",
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = accentColor,
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray,
                                        indicatorColor = accentColor
                                    ),
                                    modifier = Modifier.testTag("nav_finance_tab")
                                )

                                // Tab 3: Profile Settings
                                NavigationBarItem(
                                    selected = currentScreen == Screen.Profile,
                                    onClick = { viewModel.navigateTo(Screen.Profile) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Profile Settings"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = if (lang == "ru") "Настройки" else "Profile",
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = accentColor,
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray,
                                        indicatorColor = accentColor
                                    ),
                                    modifier = Modifier.testTag("nav_profile_tab")
                                )
                            }
                        }
                    },
                    containerColor = Color(0xFF03070E)
                ) { innerPadding ->
                    // Dynamic Screen Content with slide/fade animation transition
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                bottom = if (currentScreen == Screen.Dashboard || currentScreen == Screen.Finance || currentScreen == Screen.Profile) {
                                    innerPadding.calculateBottomPadding()
                                } else {
                                    0.dp
                                }
                            )
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith
                                        fadeOut(animationSpec = tween(250))
                            },
                            label = "screenTransition"
                        ) { screen ->
                            when (screen) {
                                is Screen.Onboarding -> OnboardingScreen(viewModel)
                                is Screen.Dashboard -> HomeScreen(viewModel)
                                is Screen.AddCard -> AddCardScreen(viewModel)
                                is Screen.CardDetails -> CardDetailsScreen(viewModel)
                                is Screen.Finance -> FinanceScreen(viewModel)
                                is Screen.Profile -> ProfileScreen(viewModel)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.let { adapter ->
            val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            adapter.enableForegroundDispatch(this, pendingIntent, null, null)
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: Intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                val idBytes = it.id
                val idHex = idBytes.joinToString("") { byte -> "%02X".format(byte) }
                mViewModel?.onNfcTagScanned(idHex)
            }
        }
    }
}
