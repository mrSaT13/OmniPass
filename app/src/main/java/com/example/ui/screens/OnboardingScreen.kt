package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.OmniPassViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun OnboardingScreen(viewModel: OmniPassViewModel) {
    val lang by viewModel.appLanguage.collectAsState()
    var currentStep by remember { mutableStateOf(0) }
    
    // Gradient choices representing premium dark space theme
    val darkSpaceGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF080C14), Color(0xFF141F30))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkSpaceGradient)
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Language Selection header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { viewModel.setLanguage("ru") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (lang == "ru") Color(0xFFFF6B00) else Color(0xFF202C3F)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("RU", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.setLanguage("en") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (lang == "en") Color(0xFFFF6B00) else Color(0xFF202C3F)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("EN", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Step contents with custom high-fidelity animation
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "onboardTransition"
                ) { step ->
                    when (step) {
                        0 -> OnboardWelcomeStep(lang)
                        1 -> OnboardPermissionStep(lang, "NFC", 
                            if (lang == "ru") "Бесконтактные карты" else "NFC Passes & Tags",
                            if (lang == "ru") "Приложение считывает ID ваших транспортных и пропускных карт для симуляции прохода через турникеты." 
                            else "The app reads magnetic badges and security tags to simulate and broadcast authentication at exit gates.",
                            Icons.Default.Nfc)
                        2 -> OnboardPermissionStep(lang, "CAMERA",
                            if (lang == "ru") "Камера и Сканирование" else "Camera & OCR Scan",
                            if (lang == "ru") "Требуется для моментального сканирования чеков, дисконтных штрихкодов и создания фото-фонов для карточек."
                            else "Required to dynamically scan shopping receipts and automatically extract expense totals via AI.",
                            Icons.Default.PhotoCamera)
                        3 -> OnboardPermissionStep(lang, "BIOMETRICS",
                            if (lang == "ru") "Безопасность" else "Biometrics Safe",
                            if (lang == "ru") "Локальное шифрование и датчик отпечатка пальца защищают ваши банковские балансы от посторонних глаз."
                            else "Secure vault storing payment limits, protected by cryptographic layers and native fingerprint sensor locks.",
                            Icons.Default.Lock)
                        4 -> OnboardPermissionStep(lang, "LOCATION",
                            if (lang == "ru") "Геолокация магазинов" else "Location Proximity",
                            if (lang == "ru") "Используется для отправки уведомлений с подходящей дисконтной картой, когда вы входите в зону магазина."
                            else "Notifies you with store coupon barcodes exactly when you walk past merchant locations.",
                            Icons.Default.CompassCalibration)
                    }
                }
            }

            // Bottom Navigation and Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Step Indicators (Dots)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    repeat(5) { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (currentStep == index) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (currentStep == index) Color(0xFFFF6B00) else Color(0xFF425672))
                        )
                    }
                }

                // Next Button
                Button(
                    onClick = {
                        if (currentStep < 4) {
                            currentStep++
                        } else {
                            viewModel.completeOnboarding()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("onboarding_next_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentStep == 4) {
                                if (lang == "ru") "Готово" else "Get Started"
                            } else {
                                if (lang == "ru") "Далее" else "Next"
                            },
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Arrow",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardWelcomeStep(lang: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        // Futuristic Card Mock Illustration using Canvas/Compose
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFFF416C), Color(0xFFFF4B2B)))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Nfc,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "OmniPass",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = 1.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = if (lang == "ru") "Добро пожаловать в OmniPass" else "Welcome to OmniPass",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (lang == "ru") {
                "Универсальное зашифрованное хранилище пропусков, банковских карт, дисконтов и умного бюджета с поддержкой сканирования ИИ."
            } else {
                "Zero-knowledge physical pass holder, secure loyalty drawer & automated AI expense reporter in one dynamic vault."
            },
            color = Color(0xFFA6BAC8),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun OnboardPermissionStep(
    lang: String,
    code: String,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        // Dynamic bouncing pulse container for details
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )

        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF202C3F)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF6B00).copy(alpha = 0.2f * pulseScale)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFFF6B00),
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = description,
            color = Color(0xFFA6BAC8),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        Surface(
            color = Color(0xFF131D2A),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00C853))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (lang == "ru") "Запрос будет показан при входе" else "System prompt context ready",
                    fontSize = 12.sp,
                    color = Color(0xFF819FB8),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
