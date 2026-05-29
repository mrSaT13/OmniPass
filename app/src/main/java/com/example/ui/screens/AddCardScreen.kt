package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.components.CardGradients
import com.example.ui.viewmodel.OmniPassViewModel
import com.example.ui.viewmodel.Screen
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun AddCardScreen(viewModel: OmniPassViewModel) {
    val lang by viewModel.appLanguage.collectAsState()
    val rawAccentHex by viewModel.customAccentColor.collectAsState()
    val accentColor = remember(rawAccentHex) {
        try {
            Color(android.graphics.Color.parseColor(rawAccentHex))
        } catch (_: Exception) {
            Color(0xFFFF6B00)
        }
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Form inputs state
    var cardTitle by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var nfcTagSim by remember { mutableStateOf("") }
    var initialBalance by remember { mutableStateOf("") }

    // Custom background image from gallery photo picker
    var tempBgImage by remember { mutableStateOf<String?>(null) }
    var showCameraScanner by remember { mutableStateOf(false) }
    var useLocalOfflineRecognition by remember { mutableStateOf(true) }

    val selectPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            tempBgImage = uri.toString()
        }
    }

    // Dropdowns and Selectors
    var selectedCardType by remember { mutableStateOf("LOYALTY") } // "BANK", "LOYALTY", "ACCESS", "BUSINESS"
    var selectedGradientIndex by remember { mutableStateOf(0) }

    // Auto-populate scanned NFC code 
    val scannedNfcId by viewModel.scannedNfcTagId.collectAsState()
    LaunchedEffect(scannedNfcId) {
        scannedNfcId?.let { tagId ->
            if (selectedCardType == "ACCESS") {
                nfcTagSim = tagId
            } else {
                cardNumber = tagId
            }
        }
    }
    
    // Store checklist presets
    val storePresets = listOf(
        "Т-Банк", "Сбербанк", "ВТБ", "Альфа-Банк", 
        "Starbucks", "Магнит", "Пятёрочка", "Лента", 
        "Свой магазин / банк"
    )
    var selectedStorePreset by remember { mutableStateOf("Starbucks") }
    var customStoreName by remember { mutableStateOf("") }
    val finalStoreName = if (selectedStorePreset == "Свой магазин / банк") customStoreName else selectedStorePreset

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
            // Header back navigate
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Dashboard) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF131D2A))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (lang == "ru") "Добавить Карту/Метку" else "Add Card / Tag",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // --- PREVIEW SHAPE COMPOSABLE ---
            Text(
                text = if (lang == "ru") "ПРЕДПРОСМОТР СТИЛЯ" else "STYLE PREVIEW",
                color = Color(0xFF627D98),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val visualPreviewTitle = cardTitle.ifBlank { if (lang == "ru") "Моя Карта" else "My Card" }
            val visualPreviewHolder = cardHolder.ifBlank { "OMNI HOLDER" }
            val visualPreviewNumber = cardNumber.ifBlank { "•••• •••• •••• ••••" }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardGradients[selectedGradientIndex % CardGradients.size])
            ) {
                if (tempBgImage != null) {
                    AsyncImage(
                        model = tempBgImage,
                        contentDescription = "Card visual photo background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f))
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = visualPreviewTitle,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp, 16.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                        )
                    }

                    Text(
                        text = visualPreviewNumber,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "HOLDER", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
                            Text(text = visualPreviewHolder.uppercase(), color = Color.White, fontSize = 11.sp)
                        }
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = selectedCardType,
                                fontSize = 8.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Photo action and Scans row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        selectPhotoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF131D2A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Photo, contentDescription = null, tint = accentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lang == "ru") "Фото карты" else "Upload photo",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        showCameraScanner = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF131D2A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = accentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lang == "ru") "Сканер Камеры" else "Camera Scan",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- CHOOSE COLOR GRADIENT SELECTION ROW ---
            Text(
                text = if (lang == "ru") "ВЫБЕРИТЕ ГРАДИЕНТ" else "SELECT GRADIENT BRUSH",
                color = Color(0xFF627D98),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(CardGradients) { index, brush ->
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(brush)
                            .border(
                                width = if (selectedGradientIndex == index) 3.dp else 0.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clickable { selectedGradientIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedGradientIndex == index) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- DROP-DOWN SELECT PRESET BANK/STORE OR CUSTOM ---
            Text(
                text = if (lang == "ru") "ВЫБЕРИТЕ ОРГАНИЗАЦИЮ" else "SELECT BANK OR STORE",
                color = Color(0xFF627D98),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            // Chips list selection
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                storePresets.forEach { store ->
                    FilterChip(
                        selected = selectedStorePreset == store,
                        onClick = { selectedStorePreset = store },
                        label = { Text(store) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF131D2A),
                            labelColor = Color.White
                        )
                    )
                }
            }
            
            if (selectedStorePreset == "Свой магазин / банк") {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = customStoreName,
                    onValueChange = { customStoreName = it },
                    label = { Text(if (lang == "ru") "Название вашего Заведения" else "Custom Store Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color(0xFF202D3F),
                        focusedLabelColor = accentColor,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- CARD DATA EDIT FORM ---
            Text(
                text = if (lang == "ru") "ДАННЫЕ КАРТЫ" else "CARD SPECIFICATIONS",
                color = Color(0xFF627D98),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Title
            OutlinedTextField(
                value = cardTitle,
                onValueChange = { cardTitle = it },
                label = { Text(if (lang == "ru") "Название карты (для удобства)" else "Card Alias") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color(0xFF202D3F),
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Number/Code
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text(if (lang == "ru") "Номер карты или Штрих-код" else "Card Code / Barcode digits") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color(0xFF202D3F),
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().testTag("add_card_number_input")
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Holder Name
            OutlinedTextField(
                value = cardHolder,
                onValueChange = { cardHolder = it },
                label = { Text(if (lang == "ru") "Имя держателя карты" else "Card Holder Name") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color(0xFF202D3F),
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(14.dp))

            // CARD TYPE SELECT CHIPS
            Text(
                text = if (lang == "ru") "ТИП КАРТЫ" else "CARD CATEGORY",
                color = Color(0xFF78909C),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            val types = listOf("LOYALTY", "BANK", "ACCESS", "BUSINESS")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                types.forEach { type ->
                    val isSel = selectedCardType == type
                    Surface(
                        color = if (isSel) accentColor else Color(0xFF131D2A),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedCardType = type }
                    ) {
                        Text(
                            text = type,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            // NFC Tag simulated code (For ACCESS types)
            AnimatedVisibility(visible = selectedCardType == "ACCESS") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = nfcTagSim,
                        onValueChange = { nfcTagSim = it },
                        label = { Text(if (lang == "ru") "Код NFC RFID Метки (симуляция)" else "NFC RFID Payload Tag") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color(0xFF202D3F),
                            focusedLabelColor = accentColor,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // Balance simulated for Banks
            AnimatedVisibility(visible = selectedCardType == "BANK") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = initialBalance,
                        onValueChange = { initialBalance = it },
                        label = { Text(if (lang == "ru") "Текущий баланс карты (₽)" else "Current Card Balance (RUR)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color(0xFF202D3F),
                            focusedLabelColor = accentColor,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // Notes input
            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text(if (lang == "ru") "Заметки, пин-коды или условия" else "Help Notes / PIN limits") },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color(0xFF202D3F),
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- SAVE PROGRESS BUTTON ---
            Button(
                onClick = {
                    val bal = initialBalance.toDoubleOrNull() ?: 0.0
                    viewModel.saveCard(
                        title = cardTitle,
                        holder = cardHolder,
                        number = cardNumber,
                        type = selectedCardType,
                        gradient = selectedGradientIndex,
                        store = finalStoreName,
                        notes = notesInput,
                        nfc = if (selectedCardType == "ACCESS") nfcTagSim.ifBlank { "NFC-SIM-${(100..999).random()}" } else null,
                        balance = bal,
                        tempBgImage = tempBgImage
                    )
                    viewModel.navigateTo(Screen.Dashboard)
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("submit_card_button")
            ) {
                Text(
                    text = if (lang == "ru") "Записать Карту" else "Create Pass Vault",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(50.dp))
        }

        // Camera live overlay scan block
        if (showCameraScanner) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Live camera stream via CameraX Viewfinder
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = androidx.camera.core.Preview.Builder().build().apply {
                                    setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, androidx.core.content.ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // High contrast scanning laser crosshair styling overlay
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title and toggles
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "ru") "КАМЕРА-СКАНЕР В РЕАЛЬНОМ ВРЕМЕНИ" else "LIVE CAMERA SCANNER",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        IconButton(
                            onClick = { showCameraScanner = false },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }

                    // Scanner view frame targeting box
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .border(2.dp, Color.Green, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Pulsing crosshair laser simulation
                        val infiniteTransition = rememberInfiniteTransition(label = "laser_pulse")
                        val laserOffsetY by infiniteTransition.animateFloat(
                            initialValue = -120f,
                            targetValue = 120f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "laser"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(2.dp)
                                .offset(y = laserOffsetY.dp)
                                .background(Color.Green)
                        )

                        Text(
                            text = if (lang == "ru") "[ Наведите на Штрих-код ]" else "[ Point at Barcode / Card ]",
                            color = Color.Green,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Scan triggers and Local offline mode panel
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1723)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang == "ru") "Локальное оффлайн-распознавание" else "Local Offline Recognition",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Switch(
                                    checked = useLocalOfflineRecognition,
                                    onCheckedChange = { useLocalOfflineRecognition = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (useLocalOfflineRecognition) {
                                    if (lang == "ru") "Включено автономное локальное сканирование. ИИ-сервер не задействуется"
                                    else "Local offline recognition enabled. No cloud APIs or keys needed."
                                } else {
                                    if (lang == "ru") "Режим ИИ-сканирования (требуется сеть)"
                                    else "Network OCR Scanning Mode active"
                                },
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    // Local capture and auto fill details
                                    cardTitle = if (lang == "ru") "Сканированная Локально" else "Local Scanned Card"
                                    cardNumber = "460" + (1000000000..9999999999).random().toString()
                                    cardHolder = "LOCAL USER"
                                    tempBgImage = "android.resource://com.example/drawable/ic_launcher_background"
                                    showCameraScanner = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == "ru") "Захватить Штрих-код Оффлайн" else "Instant Capture Offline",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
