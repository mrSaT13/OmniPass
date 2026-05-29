package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.CardEntity
import com.example.ui.components.CardGradients
import com.example.ui.components.CardStack
import com.example.ui.viewmodel.OmniPassViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun HomeScreen(viewModel: OmniPassViewModel) {
    val lang by viewModel.appLanguage.collectAsState()
    val rawAccentHex by viewModel.customAccentColor.collectAsState()
    val accentColor = remember(rawAccentHex) {
        try {
            Color(android.graphics.Color.parseColor(rawAccentHex))
        } catch (_: Exception) {
            Color(0xFFFF6B00)
        }
    }

    val cards by viewModel.cards.collectAsState()
    val finances by viewModel.finances.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val isAppLocked by viewModel.isAppLocked.collectAsState()

    var showQuickAddMenu by remember { mutableStateOf(false) }
    var showBiometricAuthDialog by remember { mutableStateOf(false) }
    var targetCardForAuth by remember { mutableStateOf<CardEntity?>(null) }
    var completeAuthCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Proximity variables for simulation
    var showProximityAlert by remember { mutableStateOf(true) }

    // Solid custom radial gradient as backing atmosphere (Dynamic if enabled)
    val dominantCard = cards.firstOrNull()
    val dynamicThemeActive by viewModel.isDynamicBgTheme.collectAsState()
    val parsedDomColor = if (dynamicThemeActive && dominantCard != null) {
        val idx = dominantCard.gradientIndex % CardGradients.size
        // Extracting dominant colors from gradients array
        when (idx) {
            0 -> Color(0xFFF5AF19)
            1 -> Color(0xFF006241)
            2 -> Color(0xFFFF4B2B)
            3 -> Color(0xFF0083B0)
            4 -> Color(0xFF243B55)
            else -> Color(0xFF8E2DE2)
        }
    } else {
        accentColor
    }

    val atmosphereBg = Brush.verticalGradient(
        colors = listOf(
            parsedDomColor.copy(alpha = 0.14f),
            Color(0xFF080D16)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF02060C))
            .background(atmosphereBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (lang == "ru") "Добро пожаловать в" else "Welcome to",
                        color = Color(0xFF8B9FB4),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = (profile?.userName ?: (if (lang == "ru") "Александр" else "Alexander")) + " 👋",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Profile Avatar choice button leading to Profile Screen
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .clickable { viewModel.navigateTo(Screen.Profile) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (profile?.userName ?: "A").take(1).uppercase(),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // --- MAIN LIST SCROLLABLE CONTENT ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // 1. Proximity Alert banner if any
                if (showProximityAlert) {
                    item {
                        AnimatedVisibility(
                            visible = showProximityAlert,
                            enter = slideInVertically() + fadeIn(),
                            exit = slideOutVertically() + fadeOut()
                        ) {
                            Surface(
                                color = accentColor.copy(alpha = 0.12f),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(accentColor, accentColor.copy(alpha = 0.2f)))),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(accentColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Near",
                                            tint = accentColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (lang == "ru") "Вы рядом с магазином!" else "Proximity Alert!",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = if (lang == "ru") {
                                                " Starbucks Сити-Молл в 50м. Открыть карту на кассе?"
                                            } else {
                                                "Starbucks is only 50m away! Tap to access rewards."
                                            },
                                            color = Color(0xFF90A4AE),
                                            fontSize = 12.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            // Open Starbucks Card details or first loyalty card directly in simulated alert
                                            val sbc = cards.firstOrNull { it.storeName.contains("Starbucks", ignoreCase = true) }
                                            if (sbc != null) {
                                                viewModel.selectCard(sbc.id)
                                            } else if (cards.isNotEmpty()) {
                                                viewModel.selectCard(cards.first().id)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = "Open",
                                            tint = accentColor
                                        )
                                    }
                                    IconButton(onClick = { showProximityAlert = false }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Interactive Card Stack Slider Container
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CardStack(
                            cards = cards,
                            lang = lang,
                            onCardSelected = { cardId -> viewModel.selectCard(cardId) },
                            isBiometricUnlocked = !isAppLocked,
                            onRequestUnlock = { card, onUnlockSuccess ->
                                targetCardForAuth = card
                                completeAuthCallback = onUnlockSuccess
                                showBiometricAuthDialog = true
                            }
                        )
                    }
                }

                // 3. Horizontal Grid of Store Quick Shortcuts
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = if (lang == "ru") "БЫСТРЫЙ ДОСТУП" else "QUICK ACCESS",
                            color = Color(0xFF627D98),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(cards) { card ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable {
                                            if (card.type == "BANK") {
                                                targetCardForAuth = card
                                                completeAuthCallback = { viewModel.selectCard(card.id) }
                                                showBiometricAuthDialog = true
                                            } else {
                                                viewModel.selectCard(card.id)
                                            }
                                        }
                                        .width(76.dp)
                                ) {
                                    val gradBrush = CardGradients[card.gradientIndex % CardGradients.size]
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(gradBrush),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (card.type) {
                                                "BANK" -> Icons.Default.CreditCard
                                                "ACCESS" -> Icons.Default.Nfc
                                                "LOYALTY" -> Icons.Default.LocalMall
                                                else -> Icons.Default.ContactMail
                                            },
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = card.title,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Quick finance summary tracker preview
                item {
                    val expTotal = finances.filter { it.isExpense }.sumOf { it.amount }
                    val incTotal = finances.filter { !it.isExpense }.sumOf { it.amount }
                    val progressRatio = if (incTotal > 0) (expTotal / incTotal).toFloat().coerceIn(0f, 1f) else 1f

                    Surface(
                        color = Color(0xFF0F1723),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang == "ru") "Ежемесячный баланс" else "Monthly Balance",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = if (lang == "ru") "Перейти" else "View",
                                    color = accentColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { viewModel.navigateTo(Screen.Finance) }
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            // Visual Progression line bar
                            LinearProgressIndicator(
                                progress = { progressRatio },
                                color = if (progressRatio > 0.8f) Color.Red else Color(0xFF00E676),
                                trackColor = Color(0xFF263238),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = if (lang == "ru") "Расходы" else "Expenses",
                                        color = Color(0xFF78909C),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "${String.format("%,.0f", expTotal)} ₽",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (lang == "ru") "Доходы" else "Income",
                                        color = Color(0xFF78909C),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "${String.format("%,.0f", incTotal)} ₽",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SPRING FLOATING ACTION BUTTON OVERLAY ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 24.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                // Expanded addition mini menu paths
                AnimatedVisibility(
                    visible = showQuickAddMenu,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        // Quick Check receipt SCAN Button
                        FloatingActionButton(
                            onClick = {
                                showQuickAddMenu = false
                                viewModel.navigateTo(Screen.Finance)
                                // Trigger instant mock receipt camera scan transition logic in finance view!
                            },
                            containerColor = Color(0xFF132D4E),
                            contentColor = Color.White,
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Scan")
                        }

                        // Add new Card card Button
                        FloatingActionButton(
                            onClick = {
                                showQuickAddMenu = false
                                viewModel.navigateTo(Screen.AddCard)
                            },
                            containerColor = accentColor,
                            contentColor = Color.White,
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AddCard, contentDescription = "Add Card")
                        }
                    }
                }

                // Core Main FAB toggle button
                FloatingActionButton(
                    onClick = { showQuickAddMenu = !showQuickAddMenu },
                    containerColor = accentColor,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("home_fab")
                ) {
                    Icon(
                        imageVector = if (showQuickAddMenu) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Toggle Add Menu"
                    )
                }
            }
        }

        // --- SIMULATED BIOMETRIC SECURE LOCK AUTH OVERLAY ---
        if (showBiometricAuthDialog) {
            AlertDialog(
                onDismissRequest = { showBiometricAuthDialog = false },
                title = {
                    Text(
                        text = if (lang == "ru") "Безопасный доступ OmniPass" else "Biometric Unlock Required",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (lang == "ru") {
                                "Пожалуйста, подтвердите личность для открытия защищенной карты ${targetCardForAuth?.title}."
                            } else {
                                "Verify biometric credentials to access secured bank vault limits for ${targetCardForAuth?.title}."
                            },
                            color = Color(0xFFB0BEC5),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        // Fingerprint Glowing Radar Graphic Wave Icon
                        val infiniteTransition = rememberInfiniteTransition(label = "fingerprintPulse")
                        val progress by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1100, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "fingerprintPulse"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = accentColor.copy(alpha = 0.15f * progress),
                                        radius = size.width * 0.5f * progress
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Fingerprint",
                                tint = accentColor,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clickable {
                                        // Simulate fingerprint tap authentication bypass
                                        viewModel.isAppLocked.value = false
                                        showBiometricAuthDialog = false
                                        completeAuthCallback?.invoke()
                                    }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (lang == "ru") "Коснитесь сканера для входа" else "Tap screen biometric scanner",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        // Bypass simulation
                        viewModel.isAppLocked.value = false
                        showBiometricAuthDialog = false
                        completeAuthCallback?.invoke()
                    }) {
                        Text(if (lang == "ru") "Демо-Вход" else "Demo Bypass", color = accentColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBiometricAuthDialog = false }) {
                        Text(if (lang == "ru") "Отмена" else "Cancel", color = Color.Gray)
                    }
                },
                containerColor = Color(0xFF0C1420),
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}
