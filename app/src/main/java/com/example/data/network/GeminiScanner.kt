package com.example.data.network

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class ParsedItem(
    val name: String,
    val price: Double
)

@JsonClass(generateAdapter = true)
data class ParsedReceipt(
    val storeName: String,
    val amount: Double,
    val category: String, // "Food", "Shopping", "Transport", "Entertainment", "Services", "Salary", "Other"
    val items: List<ParsedItem> = emptyList()
)

object GeminiScanner {
    private const val TAG = "GeminiScanner"
    private const val MODEL = "gemini-3.5-flash"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    /**
     * Parses a base64 encoded receipt image using Gemini API or returns null on failure.
     */
    suspend fun scanReceipt(base64Image: String): ParsedReceipt? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        // If it is the default placeholder, don't execute real API log
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API Key is placeholder or blank. Skipping real request.")
            return@withContext null
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent?key=$apiKey"
        
        val systemInstruction = "Вы - профессиональный помощник по учету личных финансов. " +
                "Проанализируйте фотографию чека и верните строго корректный JSON-объект в следующем формате: " +
                "{\"storeName\": \"Название магазина\", \"amount\": 123.45, \"category\": \"Один из вариантов: Food, Shopping, Transport, Entertainment, Services, Other\", \"items\": [{\"name\": \"Наименование товара\", \"price\": 12.34}]}. " +
                "Не добавляйте никакого текста разметки Markdown, кроме самого JSON."

        // Construct raw JSON body for Gemini REST payload manually & robustly
        val jsonPayload = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "Распознай чек на изображении. Заполни JSON структуру. Если название магазина или товары на русском, напиши на русском."
                    },
                    {
                      "inlineData": {
                        "mimeType": "image/jpeg",
                        "data": "$base64Image"
                      }
                    }
                  ]
                }
              ],
              "generationConfig": {
                "responseMimeType": "application/json",
                "temperature": 0.2
              },
              "systemInstruction": {
                "parts": [
                  {
                    "text": "$systemInstruction"
                  }
                ]
              }
            }
        """.trimIndent()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonPayload.toRequestBody(mediaType)
        
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed [${response.code}]: $bodyStr")
                    return@withContext null
                }
                
                // Parse Gemini Response content structure
                // Response is: { "candidates": [ { "content": { "parts": [ { "text": "...\n" } ] } } ] }
                val outerAdapter = moshi.adapter(Map::class.java)
                val outerMap = outerAdapter.fromJson(bodyStr) as? Map<*, *>
                val candidates = outerMap?.get("candidates") as? List<*>
                val firstCandidate = candidates?.firstOrNull() as? Map<*, *>
                val content = firstCandidate?.get("content") as? Map<*, *>
                val parts = content?.get("parts") as? List<*>
                val firstPart = parts?.firstOrNull() as? Map<*, *>
                val extractedText = firstPart?.get("text") as? String ?: ""
                
                Log.d(TAG, "Raw returned text from Gemini: $extractedText")
                
                // Clean the text in case Gemini wraps in ```json ... ``` despite system instruction
                val cleanJson = extractedText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val receiptAdapter = moshi.adapter(ParsedReceipt::class.java)
                return@withContext receiptAdapter.fromJson(cleanJson)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing Gemini OCR parsing", e)
            return@withContext null
        }
    }
}
