package com.example.data.repository

import com.example.data.database.AppDao
import com.example.data.database.CardEntity
import com.example.data.database.FinanceEntity
import com.example.data.database.ProfileEntity
import kotlinx.coroutines.flow.Flow

class CardRepository(private val appDao: AppDao) {
    val allCards: Flow<List<CardEntity>> = appDao.getAllCards()
    val allFinances: Flow<List<FinanceEntity>> = appDao.getAllFinances()
    val profile: Flow<ProfileEntity?> = appDao.getProfileFlow()

    suspend fun getCardById(id: Int): CardEntity? {
        return appDao.getCardById(id)
    }

    suspend fun insertCard(card: CardEntity) {
        appDao.insertCard(card)
    }

    suspend fun deleteCardById(id: Int) {
        appDao.deleteCardById(id)
    }

    suspend fun insertFinance(finance: FinanceEntity) {
        appDao.insertFinance(finance)
    }

    suspend fun deleteFinanceById(id: Int) {
        appDao.deleteFinanceById(id)
    }

    suspend fun clearAllFinances() {
        appDao.clearAllFinances()
    }

    suspend fun getProfileDirect(): ProfileEntity? {
        return appDao.getProfileDirect()
    }

    suspend fun updateProfile(profile: ProfileEntity) {
        appDao.insertProfile(profile)
    }
}
