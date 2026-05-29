package com.example.ui.screens
import androidx.compose.foundation.BorderStroke
import kotlin.math.roundToInt
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.CardGradients
import com.example.ui.viewmodel.OmniPassViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun CardDetailsScreen(viewModel: OmniPassViewModel) {
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
    val selectedCardId by viewModel.selectedCardId.collectAsState()
    val card = cards.find { it.id == selectedCardId }

    if (card == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF03070E)),
            contentAlignment = Alignment.Center
        ) {
            Text("Карта не найдена", color = Color.White)
        }
        return
    }

    var isNfcBroadcasting by remember { mutableStateOf(false) }
    var screenBrightnessBoosted by remember { mutableStateOf(false) }

    // Mock products catalog in the store for Wow feature #8
    val storeProducts = remember(card.storeName) {
        when {
            card.storeName.contains("Starbucks", ignoreCase = true) -> listOf(
                ProductMock("Капучино Гранде", 350.0, 270.0, "20% Скидка по карте"),
                ProductMock("Карамель Маккиато", 410.0, 360.0, "Скидка на сироп"),
                ProductMock("Круассан с миндалем", 240.0, 190.0, "Комбо дня")
            )
            card.storeName.contains("Магнит", ignoreCase = true) -> listOf(
                ProductMock("Молоко Простоквашино", 99.0, 79.0, "Акция 1+1"),
                ProductMock("Шоколад Ritter Sport", 180.0, 129.0, "Красный ценник"),
                ProductMock("Сыр Российский 200г", 210.0, 169.0, "Выгода по карте")
            )
            else -> listOf(
                ProductMock("Фирменный чай", 150.0, 120.0, "Акция заведения"),
                ProductMock("Печенье кукис", 90.0, 70.0, "Скидка 15%"),
                ProductMock("Набор пирожных", 500.0, 420.0, "Хит продаж")
            )
        }
    }

    val brush = CardGradients[card.gradientIndex % CardGradients.size]

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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
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

                Text(
                    text = card.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        viewModel.deleteCard(card.id)
                        viewModel.navigateTo(Screen.Dashboard)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }

            // Card Shape
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(brush)
            ) {
                if (card.customBgImage != null) {
                    AsyncImage(
                        model = card.customBgImage,
                        contentDescription = "Card Background Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = card.storeName,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = card.cardNumber,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "HOLDER",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = card.cardHolderName.uppercase(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (card.type == "BANK") {
                            Text(
                                text = "${String.format("%,.2f", card.balance)} ₽",
                                color = Color(0xFF00FF88),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Barcode Section (Boost Screen brightness on click)
            Surface(
                color = if (screenBrightnessBoosted) Color.White else Color(0xFF0F1723),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { screenBrightnessBoosted = !screenBrightnessBoosted }
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val codeColor = if (screenBrightnessBoosted) Color.Black else Color.White
                    Text(
                        text = if (screenBrightnessBoosted) {
                            if (lang == "ru") "Яркость Экрана Максимизирована" else "Brightness Boost Activated"
                        } else {
                            if (lang == "ru") "Нажмите чтобы увеличить яркость для сканера" else "Tap code to maximize screen scan brightness"
                        },
                        color = if (screenBrightnessBoosted) Color.Red else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Barcode Simulated Columns lines
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(64.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val pattern = listOf(4, 2, 6, 2, 1, 4, 1, 6, 2, 4, 1, 2, 1, 6, 4, 2, 4)
                        pattern.forEachIndexed { i, weight ->
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(weight.dp * 1.5f)
                                    .background(if (i % 2 == 0) codeColor else Color.Transparent)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = card.cardNumber,
                        color = codeColor,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // NFC Broadcast Wave Emulating Radar Animation (Wow feature #4)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (lang == "ru") "БЕСКОНТАКТНАЯ ТРАНСЛЯЦИЯ NFC" else "NFC SIMULATED EMULATION",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (isNfcBroadcasting) {
                        // Animated pulsing wave rings
                        val infiniteTransition = rememberInfiniteTransition(label = "nfc_pulse")
                        val progress by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1400, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "waveProgress"
                        )

                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = accentColor.copy(alpha = 0.4f * (1f - progress)),
                                        radius = size.width * 0.5f * progress
                                    )
                                    drawCircle(
                                        color = accentColor.copy(alpha = 0.2f * (1f - progress)),
                                        radius = size.width * 0.35f * progress
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Nfc,
                                contentDescription = "Pulsing NFC",
                                tint = accentColor,
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (lang == "ru") "Трансляция ID карты..." else "Transmitting card payload...",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        IconButton(
                            onClick = { isNfcBroadcasting = true },
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = "Broadcaster",
                                tint = accentColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { isNfcBroadcasting = !isNfcBroadcasting },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isNfcBroadcasting) Color.Red else Color(0xFF243B55)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isNfcBroadcasting) {
                                if (lang == "ru") "Остановить" else "Stop"
                            } else {
                                if (lang == "ru") "Передать по NFC" else "Broadcast Pass"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- REAL-TIME OFFERS / PRODUCT PRICES (Wow feature #8) ---
            Text(
                text = if (lang == "ru") "АКТУАЛЬНЫЕ ЦЕНЫ И СКИДКИ В ${card.storeName.uppercase()}" else "REAL-TIME DISCOUNTS AT ${card.storeName.uppercase()}",
                color = Color(0xFF627D98),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            storeProducts.forEach { product ->
                Surface(
                    color = Color(0xFF0F1723),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.name,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = product.discountLabel,
                                color = Color(0xFFFF9100),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${product.originalPrice.roundToInt()}₽",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                style = LocalTextStyle.current.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = "${product.discountPrice.roundToInt()}₽",
                                color = Color(0xFF00C853),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Notes info field
            if (card.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    color = Color.Black.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, Color(0xFF202C3F)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (lang == "ru") "Заметки" else "Notes",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = card.notes,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

data class ProductMock(
    val name: String,
    val originalPrice: Double,
    val discountPrice: Double,
    val discountLabel: String
)
