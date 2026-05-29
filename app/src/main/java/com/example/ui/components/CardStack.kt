package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.CardEntity
import kotlin.math.roundToInt

// Gradient definitions matching Card gradients index
val CardGradients = listOf(
    // 0: Gold Deluxe
    Brush.linearGradient(listOf(Color(0xFFF12711), Color(0xFFF5AF19))),
    // 1: Starbucks Emerald
    Brush.linearGradient(listOf(Color(0xFF006241), Color(0xFF003F2D))),
    // 2: Sunset Coral Red
    Brush.linearGradient(listOf(Color(0xFFFF416C), Color(0xFFFF4B2B))),
    // 3: Cobalt Space Blue
    Brush.linearGradient(listOf(Color(0xFF00B4DB), Color(0xFF0083B0))),
    // 4: Matte Dark Carbon
    Brush.linearGradient(listOf(Color(0xFF141E30), Color(0xFF243B55))),
    // 5: Toxic Purple-Cyan
    Brush.linearGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
)

@Composable
fun CardStack(
    cards: List<CardEntity>,
    lang: String,
    onCardSelected: (Int) -> Unit,
    isBiometricUnlocked: Boolean,
    onRequestUnlock: (CardEntity, () -> Unit) -> Unit
) {
    if (cards.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color(0xFF131D2A), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (lang == "ru") "Добавьте карты с экрана настроек или плюсом!" 
                       else "Add cards to begin!",
                color = Color.Gray,
                fontSize = 15.sp
            )
        }
        return
    }

    var selectedIndex by remember { mutableStateOf(0) }
    val cardList = cards.take(6) // limit to top 6 beautiful stack

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(310.dp),
        contentAlignment = Alignment.Center
    ) {
        cardList.forEachIndexed { index, card ->
            val isCurrent = index == selectedIndex
            key(card.id) {
                Interactive3DCard(
                    card = card,
                    lang = lang,
                    isCurrent = isCurrent,
                    indexInStack = index - selectedIndex,
                    isBiometricUnlocked = isBiometricUnlocked,
                    onSwipedLeft = {
                        selectedIndex = (selectedIndex + 1) % cardList.size
                    },
                    onSwipedRight = {
                        selectedIndex = if (selectedIndex > 0) selectedIndex - 1 else cardList.size - 1
                    },
                    onCardTap = {
                        if (card.type == "BANK") {
                            onRequestUnlock(card) {
                                onCardSelected(card.id)
                            }
                        } else {
                            onCardSelected(card.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun Interactive3DCard(
    card: CardEntity,
    lang: String,
    isCurrent: Boolean,
    indexInStack: Int,
    isBiometricUnlocked: Boolean,
    onSwipedLeft: () -> Unit,
    onSwipedRight: () -> Unit,
    onCardTap: () -> Unit
) {
    // Gestures and Drags offsets
    var dragX by remember { mutableStateOf(0f) }
    var dragY by remember { mutableStateOf(0f) }

    // Smooth physics strings animations
    val animX by animateFloatAsState(
        targetValue = if (isCurrent) dragX else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "animX"
    )
    val animY by animateFloatAsState(
        targetValue = if (isCurrent) dragY else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "animY"
    )

    // Stack scaling and rotation based on distance from current card
    val targetScale = if (isCurrent) 1.0f else (1.0f - (Math.abs(indexInStack) * 0.06f)).coerceAtLeast(0.7f)
    val animScale by animateFloatAsState(targetValue = targetScale, label = "animScale")
    
    val targetAlpha = if (isCurrent) 1.0f else (1.0f - (Math.abs(indexInStack) * 0.25f)).coerceAtMost(1f).coerceAtLeast(0.2f)
    val animAlpha by animateFloatAsState(targetValue = targetAlpha, label = "animAlpha")

    val targetOffsetY = if (isCurrent) 0f else (indexInStack * 14f)
    val animOffsetY by animateFloatAsState(targetValue = targetOffsetY, label = "animOffsetY")

    val brush = CardGradients[card.gradientIndex % CardGradients.size]

    // Foil Highlight tilt calculation
    val gleamCenterX = (animX / 300f) + 0.5f
    val gleamCenterY = (animY / 200f) + 0.5f

    Box(
        modifier = Modifier
            .offset { IntOffset(animX.roundToInt(), (animOffsetY + animY * 0.1f).roundToInt()) }
            .graphicsLayer {
                scaleX = animScale
                scaleY = animScale
                alpha = animAlpha
                // Real tilt effect based on current coordinates
                rotationY = (animX * 0.05f).coerceIn(-15f, 15f)
                rotationX = -(animY * 0.04f).coerceIn(-10f, 10f)
                cameraDistance = 16f
            }
            .fillMaxWidth(0.92f)
            .height(205.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(brush)
            .drawBehind {
                // Shiny luxury hologram overlay shifting with tilt drag coordinates
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.14f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(
                            size.width * gleamCenterX,
                            size.height * gleamCenterY
                        ),
                        radius = size.width * 0.55f
                    )
                )
            }
            .pointerInput(isCurrent) {
                if (isCurrent) {
                    detectDragGestures(
                        onDragEnd = {
                            if (dragX > 250f) onSwipedRight()
                            else if (dragX < -250f) onSwipedLeft()
                            dragX = 0f
                            dragY = 0f
                        },
                        onDragCancel = {
                            dragX = 0f
                            dragY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragX += dragAmount.x
                            dragY += dragAmount.y
                        }
                    )
                }
            }
            .clickable { onCardTap() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Store/Bank and Connection chip Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = card.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                
                // Holographic plastic Chip illustration
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!card.nfcTagId.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = "NFC Enabled",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 6.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp, 18.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE5C158).copy(alpha = 0.85f))
                    )
                }
            }

            // Middle: Card Number or code details representation
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (card.type == "BANK") {
                        card.cardNumber
                    } else {
                        // Spaced digits for barcode codes
                        card.cardNumber.chunked(4).joinToString(" ")
                    },
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.95f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Balance display (Bank only, securely protected!)
                if (card.type == "BANK") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isBiometricUnlocked) {
                                    Text(
                                        text = "${String.format("%,.2f", card.balance)} ₽",
                                        color = Color(0xFF00FF88),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = Color(0xFFFFCC00),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (lang == "ru") "Баланс заблокирован" else "Balance locked",
                                        color = Color(0xFFFFCC00),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Footer: Card Holder & Date/Type Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (lang == "ru") "ВЛАДЕЛЕЦ" else "CARDHOLDER",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = card.cardHolderName.uppercase(),
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Type details badge
                Surface(
                    color = Color.White.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    val label = when (card.type) {
                        "BANK" -> if (lang == "ru") "БАНК" else "BANK"
                        "LOYALTY" -> if (lang == "ru") "КАРТА" else "LOYALTY"
                        "ACCESS" -> if (lang == "ru") "ПРОПУСК" else "PASS"
                        else -> if (lang == "ru") "ВИЗИТКА" else "CONTACT"
                    }
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
