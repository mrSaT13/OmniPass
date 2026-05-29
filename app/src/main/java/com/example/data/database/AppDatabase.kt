package com.example.data.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cards")
data class CardEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val cardHolderName: String,
    val cardNumber: String,
    val type: String, // "BANK", "LOYALTY", "ACCESS", "BUSINESS"
    val gradientIndex: Int = 0,
    val customBgImage: String? = null,
    val accentColor: String? = null,
    val nfcTagId: String? = null,
    val storeName: String,
    val notes: String = "",
    val isFavorite: Boolean = false,
    val expiryDate: String? = null,
    val balance: Double = 0.0,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Entity(tableName = "finances")
data class FinanceEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val isExpense: Boolean, // true = expense, false = income
    val category: String, // "Food", "Shopping", "Transport", "Entertainment", "Services", "Salary", "Other"
    val timestamp: Long = System.currentTimeMillis(),
    val associatedCardId: Int? = null,
    val itemsJson: String? = null // parsed items list from receipt
)

@Entity(tableName = "profiles")
data class ProfileEntity(
    @androidx.room.PrimaryKey val id: Int = 1, // Fixed ID for single user profile
    val userName: String = "Александр",
    val profilePhotoUri: String? = null,
    val appLanguage: String = "ru", // "ru" or "en"
    val activeThemeAccentHex: String = "#FF6B00",
    val isDynamicPaletteEnabled: Boolean = true,
    val nextcloudHost: String = "",
    val nextcloudUser: String = "",
    val caldavUrl: String = "",
    val jellyfinUrl: String = ""
)

@Dao
interface AppDao {
    // --- Cards ---
    @Query("SELECT * FROM cards ORDER BY isFavorite DESC, title ASC")
    fun getAllCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: Int): CardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity)

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteCardById(id: Int)

    // --- Finance ---
    @Query("SELECT * FROM finances ORDER BY timestamp DESC")
    fun getAllFinances(): Flow<List<FinanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinance(finance: FinanceEntity)

    @Query("DELETE FROM finances WHERE id = :id")
    suspend fun deleteFinanceById(id: Int)

    @Query("DELETE FROM finances")
    suspend fun clearAllFinances()

    // --- Profile ---
    @Query("SELECT * FROM profiles WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE id = 1 LIMIT 1")
    suspend fun getProfileDirect(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)
}

@Database(entities = [CardEntity::class, FinanceEntity::class, ProfileEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
