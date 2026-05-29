package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CardGradients
import com.example.ui.viewmodel.OmniPassViewModel
import com.example.ui.viewmodel.Screen

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

    // Form inputs state
    var cardTitle by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var nfcTagSim by remember { mutableStateOf("") }
    var initialBalance by remember { mutableStateOf("") }

    // Dropdowns and Selectors
    var selectedCardType by remember { mutableStateOf("LOYALTY") } // "BANK", "LOYALTY", "ACCESS", "BUSINESS"
    var selectedGradientIndex by remember { mutableStateOf(0) }
    
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
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
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

            Spacer(modifier = Modifier.height(24.dp))

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
                        balance = bal
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
