package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CardGradients
import com.example.ui.viewmodel.OmniPassViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun ProfileScreen(viewModel: OmniPassViewModel) {
    val lang by viewModel.appLanguage.collectAsState()
    val rawAccentHex by viewModel.customAccentColor.collectAsState()
    val accentColor = remember(rawAccentHex) {
        try {
            Color(android.graphics.Color.parseColor(rawAccentHex))
        } catch (_: Exception) {
            Color(0xFFFF6B00)
        }
    }

    val profile by viewModel.profile.collectAsState()
    val isDynamicBgTheme by viewModel.isDynamicBgTheme.collectAsState()

    // Form inputs
    var activeUsername by remember { mutableStateOf("") }
    var nextcloudHost by remember { mutableStateOf(profile?.nextcloudHost ?: "") }
    var nextcloudUser by remember { mutableStateOf(profile?.nextcloudUser ?: "") }
    var caldavUrl by remember { mutableStateOf(profile?.caldavUrl ?: "") }
    var jellyfinUrl by remember { mutableStateOf(profile?.jellyfinUrl ?: "") }

    // Backup feedback
    var showBackupFeedbackMsg by remember { mutableStateOf<String?>(null) }

    // List of premium themes available
    val presetAccents = listOf(
        "#FF6B00" to (if (lang == "ru") "Тыква (Стандарт)" else "Pumpkin"),
        "#00E676" to (if (lang == "ru") "Мята" else "Mint"),
        "#00E5FF" to (if (lang == "ru") "Неон Бриз" else "Cyber Breeze"),
        "#FF1744" to (if (lang == "ru") "Красный Рубин" else "Ruby"),
        "#D500F9" to (if (lang == "ru") "Аметист" else "Amethyst"),
        "#FFFF00" to (if (lang == "ru") "Золотой" else "Sol")
    )

    // Help Guides Popups state
    var showHelpDialog by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        profile?.let {
            activeUsername = it.userName
            nextcloudHost = it.nextcloudHost
            nextcloudUser = it.nextcloudUser
            caldavUrl = it.caldavUrl
            jellyfinUrl = it.jellyfinUrl
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF03070E))
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header Title
            Text(
                text = if (lang == "ru") "Настройки и Профиль" else "Settings & Profile",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // --- USER INFORMATION SECTION ---
            Text(
                text = if (lang == "ru") "УЧЕТНАЯ ЗАПИСЬ" else "USER IDENTITY",
                color = Color(0xFF627D98),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Surface(
                color = Color(0xFF0F1723),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeUsername.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = activeUsername,
                                onValueChange = {
                                    activeUsername = it
                                    viewModel.updateProfileName(it)
                                },
                                label = { Text(if (lang == "ru") "Ваше имя" else "My Name") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- BILINGUAL SWITCHER ---
            Text(
                text = if (lang == "ru") "ЯЗЫК ИНТЕРФЕЙСА" else "APPLICATION LANGUAGE",
                color = Color(0xFF627D98),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F1723), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                listOf(
                    "ru" to (if (lang == "ru") "Русский (по умолчанию)" else "Russian (Default)"),
                    "en" to (if (lang == "ru") "Английский" else "English")
                ).forEach { (code, label) ->
                    val isSel = lang == code
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) accentColor else Color.Transparent)
                            .clickable { viewModel.setLanguage(code) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) Color.White else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- ACCENT THEMES (Fully customizable color choices) ---
            Text(
                text = if (lang == "ru") "АКЦЕНТ И ОФОРМЛЕНИЕ" else "ACCENT THEMES & DECK COLORS",
                color = Color(0xFF627D98),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Toggle for dynamic palette based on dominant card photo color
            Surface(
                color = Color(0xFF0F1723),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (lang == "ru") "Динамический цвет страниц" else "Extract Dynamic Page Color",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (lang == "ru") "Вычисление темы на основе цвета активной карты" else "Extracts backdrop tones based on selected active card cover",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = isDynamicBgTheme,
                        onCheckedChange = { viewModel.setDynamicBgThemeEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grid of preset accents ( pumpkins, cyan breeze, etc. )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                presetAccents.forEach { (hex, title) ->
                    val isSel = rawAccentHex == hex
                    Surface(
                        color = if (isSel) Color(android.graphics.Color.parseColor(hex)).copy(alpha = 0.15f) else Color(0xFF0F1723),
                        border = BorderStroke(
                            width = if (isSel) 2.dp else 1.dp,
                            color = if (isSel) Color(android.graphics.Color.parseColor(hex)) else Color(0xFF202C3F)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .clickable { viewModel.setAccentColor(hex) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- NEXTCLOUD, WEBDAV, BAIDAL CALDAV SYNC CONFIGURATION (Wow features #9) ---
            Text(
                text = if (lang == "ru") "ОБЛАЧНАЯ СИНХРОНИЗАЦИЯ" else "CLOUD SYNCHRONIZATION",
                color = Color(0xFF627D98),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Surface(
                color = Color(0xFF0F1723),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Nextcloud parameters
                    Text(
                        text = "1. Nextcloud & WebDAV backups",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = nextcloudHost,
                        onValueChange = { nextcloudHost = it },
                        label = { Text("WebDAV Server URL (e.g., https://next.cloud/remote.php)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nextcloudUser,
                        onValueChange = { nextcloudUser = it },
                        label = { Text("Username") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Baikal CalDAV (used for posting expense calendar schedules)
                    Text(
                        text = "2. Baikal CalDAV (Expenses to Calendar)",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = caldavUrl,
                        onValueChange = { caldavUrl = it },
                        label = { Text("Baikal CalDAV Server Endpoint URL") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Jellyfin client library options
                    Text(
                        text = "3. Jellyfin Content Storage Server",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = jellyfinUrl,
                        onValueChange = { jellyfinUrl = it },
                        label = { Text("Jellyfin Server Port URL (for media vouchers list)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Run simulated backup backup
                    Button(
                        onClick = {
                            showBackupFeedbackMsg = if (lang == "ru") {
                                "Синхронизация успешно выполнена! Данные карт выгружены на Nextcloud WebDAV, расходы успешно импортированы в календарь Baikal CalDAV."
                            } else {
                                "WebDAV sync completed! Saved card vault limits updated on Nextcloud server, expenses synced with CalDAV calendars!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Sync", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lang == "ru") "Синхронизировать сейчас" else "Execute Sync Engine", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- INTERACTIVE SYSTEM HELP USER GUIDE MODAL (Wow features #10) ---
            Text(
                text = if (lang == "ru") "ИНТЕРАКТИВНОЕ РУКОВОДСТВО" else "GUIDELINES & DOCUMENTATION",
                color = Color(0xFF627D98),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Button(
                onClick = { showHelpDialog = true.also { showHelpDialog = true } },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF131D2A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.HelpCenter, contentDescription = "Manual help", tint = accentColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (lang == "ru") "Прочитать встроенный гайд" else "Open Interactive User Guide", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Clear all data reset bypass
            Button(
                onClick = {
                    viewModel.helperClearAllFinance()
                    viewModel.navigateTo(Screen.Onboarding)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("profile_reset_button")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (lang == "ru") "Сбросить данные и гайд первого запуска" else "Emergency profile reset", color = Color.Red, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(70.dp))
        }

        // --- BACKUP POPUP FEEDBACK DIALOG ---
        if (showBackupFeedbackMsg != null) {
            AlertDialog(
                onDismissRequest = { showBackupFeedbackMsg = null },
                title = { Text(if (lang == "ru") "Статус трансляции облака" else "Cloud Sync Complete!", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text(showBackupFeedbackMsg ?: "", color = Color(0xFFB0BEC5)) },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        onClick = { showBackupFeedbackMsg = null }
                    ) {
                        Text("ОК")
                    }
                },
                containerColor = Color(0xFF0C1420),
                shape = RoundedCornerShape(20.dp)
            )
        }

        // --- INTERACTIVE HELP POPUP MODAL ---
        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                title = { Text(if (lang == "ru") "Инструкция OmniPass" else "OmniPass Guided Mastery", color = Color.White, fontWeight = FontWeight.Black) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (lang == "ru") "1. Хранение Карт и Визиток:" else "1. Card & Badge Keeper Vault:",
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (lang == "ru") {
                                "Превратите свой телефон в карманный картхолдер! Интегрируйте дисконтные, дисконтные, штрих-код или бесконтактные транспортные пропуска."
                            } else {
                                "Holds barcodes, client numbers, loyalty vouchers, and contact records all in one fully styled digital physical pocket."
                            },
                            color = Color(0xFFCFD8DC),
                            fontSize = 12.sp
                        )

                        Text(
                            text = if (lang == "ru") "2. Распознавание Чеков ИИ (Gemini API):" else "2. Gemini Smart Receipt Scanning:",
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (lang == "ru") {
                                "Кликните на кнопку 'Чек-ИИ' во вкладке Финансы, чтобы активировать ИИ. Нейросеть считает позиции в магазине и мгновенно пополнит статью расходов."
                            } else {
                                "Upload or capture a shopping ticket - Gemini parses store details and fills the dynamic expense list instantly."
                            },
                            color = Color(0xFFCFD8DC),
                            fontSize = 12.sp
                        )

                        Text(
                            text = if (lang == "ru") "3. NFC RFID Трансляция тумблер:" else "3. NFC Passive RFID Emulation:",
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (lang == "ru") {
                                "Откройте сохраненный пропуск, нажмите кнопка 'Передать по NFC' для симуляции волны прохода на пропускных турникетах."
                            } else {
                                "Simulates security credentials, broadcasting dynamic wave animations representing typical RFID checkins."
                            },
                            color = Color(0xFFCFD8DC),
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        onClick = { showHelpDialog = false }
                    ) {
                        Text(if (lang == "ru") "Закрыть" else "Understood")
                    }
                },
                containerColor = Color(0xFF0C1420),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
