package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.database.AppDatabase
import com.example.data.database.CardEntity
import com.example.data.database.FinanceEntity
import com.example.data.database.ProfileEntity
import com.example.data.network.GeminiScanner
import com.example.data.network.ParsedReceipt
import com.example.data.network.ParsedItem
import com.example.data.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

sealed class Screen {
    object Onboarding : Screen()
    object Dashboard : Screen()
    object AddCard : Screen()
    object CardDetails : Screen()
    object Finance : Screen()
    object Profile : Screen()
}

class OmniPassViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "OmniPassViewModel"

    private val prefs = application.getSharedPreferences("omnipass_prefs", android.content.Context.MODE_PRIVATE)

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "omnipass_database"
    ).fallbackToDestructiveMigration().build()

    private val repository = CardRepository(db.appDao())

    // --- State Expositions ---
    val cards: StateFlow<List<CardEntity>> = repository.allCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val finances: StateFlow<List<FinanceEntity>> = repository.allFinances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<ProfileEntity?> = repository.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Visual state controllers ---
    val currentScreen = MutableStateFlow<Screen>(
        if (application.getSharedPreferences("omnipass_prefs", android.content.Context.MODE_PRIVATE)
            .getBoolean("onboarding_finished", false)) Screen.Dashboard else Screen.Onboarding
    )
    val selectedCardId = MutableStateFlow<Int?>(null)
    
    // Scanned NFC RFID Tags
    val scannedNfcTagId = MutableStateFlow<String?>(null)
    
    // Custom Accent/Theme setup (User customizable gradients and buttons)
    val customAccentColor = MutableStateFlow("#FF6B00") // Default warm orange
    val isDynamicBgTheme = MutableStateFlow(true) // Dynamic page coloring based on photo
    val isAppLocked = MutableStateFlow(false) // Safe biometric lock
    val appLanguage = MutableStateFlow("ru") // Default Russian ("ru" or "en")
    
    // Receipt scanning progress overlay
    val isScanningReceipt = MutableStateFlow(false)
    val scanningError = MutableStateFlow<String?>(null)

    init {
        // Prepare default profiles block and pre-populate if empty
        viewModelScope.launch {
            repository.profile.collect { currentProfile ->
                if (currentProfile == null) {
                    val defaultProfile = ProfileEntity(
                        id = 1,
                        userName = "Александр",
                        appLanguage = "ru",
                        activeThemeAccentHex = "#FF4500",
                        isDynamicPaletteEnabled = true
                    )
                    repository.updateProfile(defaultProfile)
                    appLanguage.value = "ru"
                    customAccentColor.value = "#FF4500"
                    isDynamicBgTheme.value = true
                } else {
                    appLanguage.value = currentProfile.appLanguage
                    customAccentColor.value = currentProfile.activeThemeAccentHex
                    isDynamicBgTheme.value = currentProfile.isDynamicPaletteEnabled
                }
            }
        }

        viewModelScope.launch {
            repository.allCards.collect { list ->
                if (list.isEmpty()) {
                    populateDefaultCards()
                }
            }
        }
    }

    private fun populateDefaultCards() {
        viewModelScope.launch {
            // Tinkoff Bank Gold Card
            repository.insertCard(
                CardEntity(
                    title = "T-Bank Gold",
                    cardHolderName = "ALEXANDER SMIRNOV",
                    cardNumber = "2200 4481 9901 3241",
                    type = "BANK",
                    gradientIndex = 0,
                    storeName = "Т-Банк",
                    notes = "Основная зарплатная карта с повышенным кэшбэком",
                    isFavorite = true,
                    balance = 145820.50,
                    nfcTagId = "NFC-TBANK-882"
                )
            )

            // Starbucks loyalty
            repository.insertCard(
                CardEntity(
                    title = "Starbucks Card",
                    cardHolderName = "Alex S.",
                    cardNumber = "6045129983716",
                    type = "LOYALTY",
                    gradientIndex = 1,
                    storeName = "Starbucks",
                    notes = "Бесплатный сироп за каждые 5 звезд",
                    isFavorite = true,
                    balance = 1250.0,
                    nfcTagId = "NFC-STAR-1102"
                )
            )

            // Magnet card
            repository.insertCard(
                CardEntity(
                    title = "Магнит Семья",
                    cardHolderName = "Александр",
                    cardNumber = "7789310022931293",
                    type = "LOYALTY",
                    gradientIndex = 2,
                    storeName = "Магнит",
                    notes = "Дисконт у дома",
                    isFavorite = false,
                    balance = 340.0
                )
            )

            // Office PASS badge
            repository.insertCard(
                CardEntity(
                    title = "Пропуск Офис",
                    cardHolderName = "Департамент IT",
                    cardNumber = "SEC-PASS-0912",
                    type = "ACCESS",
                    gradientIndex = 3,
                    storeName = "БЦ Крокус",
                    notes = "Круглосуточный вход через турникет Б",
                    nfcTagId = "NFC-ACCESS-881263-KROCUS"
                )
            )

            // Pre-seed mock finances
            repository.insertFinance(
                FinanceEntity(
                    title = "Starbucks Сити-Молл",
                    amount = 450.0,
                    isExpense = true,
                    category = "Food"
                )
            )
            repository.insertFinance(
                FinanceEntity(
                    title = "Перевод Зарплаты",
                    amount = 85000.0,
                    isExpense = false,
                    category = "Salary"
                )
            )
            repository.insertFinance(
                FinanceEntity(
                    title = "Магнит У дома",
                    amount = 1230.50,
                    isExpense = true,
                    category = "Shopping"
                )
            )
            repository.insertFinance(
                FinanceEntity(
                    title = "Метрополитен проезд",
                    amount = 54.0,
                    isExpense = true,
                    category = "Transport"
                )
            )
        }
    }

    // --- Action implementations ---
    fun navigateTo(screen: Screen) {
        currentScreen.value = screen
    }

    fun selectCard(cardId: Int) {
        selectedCardId.value = cardId
        navigateTo(Screen.CardDetails)
    }

    fun setLanguage(lang: String) {
        appLanguage.value = lang
        viewModelScope.launch {
            val prof = repository.getProfileDirect() ?: ProfileEntity()
            repository.updateProfile(prof.copy(appLanguage = lang))
        }
    }

    fun setAccentColor(hex: String) {
        customAccentColor.value = hex
        viewModelScope.launch {
            val prof = repository.getProfileDirect() ?: ProfileEntity()
            repository.updateProfile(prof.copy(activeThemeAccentHex = hex))
        }
    }

    fun setDynamicBgThemeEnabled(enabled: Boolean) {
        isDynamicBgTheme.value = enabled
        viewModelScope.launch {
            val prof = repository.getProfileDirect() ?: ProfileEntity()
            repository.updateProfile(prof.copy(isDynamicPaletteEnabled = enabled))
        }
    }

    fun updateProfileName(name: String) {
        viewModelScope.launch {
            val prof = repository.getProfileDirect() ?: ProfileEntity()
            repository.updateProfile(prof.copy(userName = name))
        }
    }

    fun deleteCard(cardId: Int) {
        viewModelScope.launch {
            repository.deleteCardById(cardId)
        }
    }

    fun checkCardExistsAndAddIfNeeded(number: String, onAlreadyExists: (CardEntity) -> Unit, onAdded: () -> Unit) {
        viewModelScope.launch {
            // Check in list
            val match = cards.value.firstOrNull { it.cardNumber == number || (it.nfcTagId != null && it.nfcTagId == number) }
            if (match != null) {
                onAlreadyExists(match)
            } else {
                // Not exists, add a quick scanned card
                val newCard = CardEntity(
                    title = "Карта " + (100..999).random(),
                    cardHolderName = profile.value?.userName ?: "Владелец",
                    cardNumber = number,
                    type = "LOYALTY",
                    gradientIndex = (0..5).random(),
                    storeName = "Распознанный Скан"
                )
                repository.insertCard(newCard)
                onAdded()
            }
        }
    }

    fun saveCard(
        title: String,
        holder: String,
        number: String,
        type: String,
        gradient: Int,
        store: String,
        notes: String,
        nfc: String? = null,
        balance: Double = 0.0,
        tempBgImage: String? = null
    ) {
        viewModelScope.launch {
            val newCard = CardEntity(
                title = title.ifBlank { "Карта" },
                cardHolderName = holder.ifBlank { "OmniHolder" },
                cardNumber = number.ifBlank { (1000..9999).random().toString() },
                type = type,
                gradientIndex = gradient,
                storeName = store.ifBlank { "Мой Магазин" },
                notes = notes,
                nfcTagId = nfc?.ifBlank { null },
                balance = balance,
                customBgImage = tempBgImage
            )
            repository.insertCard(newCard)
        }
    }

    fun addManualTransaction(title: String, amount: Double, isExpense: Boolean, category: String) {
        viewModelScope.launch {
            repository.insertFinance(
                FinanceEntity(
                    title = title.ifBlank { "Транзакция" },
                    amount = amount,
                    isExpense = isExpense,
                    category = category
                )
            )
        }
    }

    fun helperDeleteFinance(id: Int) {
        viewModelScope.launch {
            repository.deleteFinanceById(id)
        }
    }

    fun helperClearAllFinance() {
        viewModelScope.launch {
            repository.clearAllFinances()
        }
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("onboarding_finished", true).apply()
        navigateTo(Screen.Dashboard)
    }

    fun helperResetFullApp() {
        viewModelScope.launch {
            repository.clearAllCards()
            repository.clearAllFinances()
            prefs.edit().putBoolean("onboarding_finished", false).apply()
            navigateTo(Screen.Onboarding)
        }
    }

    fun onNfcTagScanned(tagId: String) {
        scannedNfcTagId.value = tagId
        viewModelScope.launch {
            val matchedCard = cards.value.firstOrNull { it.nfcTagId?.equals(tagId, ignoreCase = true) == true }
            if (matchedCard != null) {
                selectCard(matchedCard.id)
            }
        }
    }

    // --- Gemini OCR Receipt scanner processing ---
    fun parseReceiptImageWithGemini(bitmap: Bitmap, onCompleted: (ParsedReceipt?) -> Unit) {
        isScanningReceipt.value = true
        scanningError.value = null
        
        viewModelScope.launch {
            try {
                // Convert bitmap to Base64 String
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                val base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                
                // Call Gemini scanner endpoint
                val result: com.example.data.network.ParsedReceipt? = GeminiScanner.scanReceipt(base64Image)
                
                if (result != null) {
                    val catText = result.category // Maps direct categories safely
                    
                    // Map result to our App finance model
                    val mapped = ParsedReceipt(
                        storeName = result.storeName,
                        amount = result.amount,
                        category = when (catText.trim()) {
                            "Food" -> "Food"
                            "Shopping" -> "Shopping"
                            "Transport" -> "Transport"
                            "Entertainment" -> "Entertainment"
                            "Services" -> "Services"
                            else -> "Other"
                        },
                        items = result.items.map { ParsedItem(it.name, it.price) }
                    )
                    
                    // Insert parsed finance directly into db
                    val formattedDescriptionStr = if (mapped.items.isNotEmpty()) {
                        mapped.items.joinToString { "${it.name} (${it.price}₽)" }
                    } else {
                        "Пожизненный учет чека"
                    }
                    
                    repository.insertFinance(
                        FinanceEntity(
                            title = mapped.storeName,
                            amount = mapped.amount,
                            isExpense = true,
                            category = mapped.category,
                            itemsJson = formattedDescriptionStr
                        )
                    )
                    
                    onCompleted(mapped)
                } else {
                    scanningError.value = "Не удалось распознать ИИ (проверьте API-ключ)"
                    onCompleted(null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "OCR exception processing receipt Image", e)
                scanningError.value = "Ошибка сканирования чека: " + e.localizedMessage
                onCompleted(null)
            } finally {
                isScanningReceipt.value = false
            }
        }
    }
}
