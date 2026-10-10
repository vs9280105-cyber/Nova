package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.example.model.ChatMessageEntity
import com.example.model.GroundingSource
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

data class GeminiResult(
    val text: String,
    val sources: List<GroundingSource> = emptyList(),
    val isError: Boolean = false
)

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    fun getEffectiveApiKey(customKey: String? = null): String {
        if (!customKey.isNullOrBlank()) {
            return customKey.trim()
        }
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
    }

    suspend fun sendPrompt(
        prompt: String,
        history: List<ChatMessageEntity> = emptyList(),
        systemInstructionText: String? = null,
        imageBase64: String? = null,
        imageMimeType: String? = null,
        model: String = "gemini-2.5-flash",
        enableSearchGrounding: Boolean = true,
        customApiKey: String? = null
    ): GeminiResult = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey(customApiKey)
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext GeminiResult(
                text = "⚠️ Gemini API key is missing or not configured. Please add your GEMINI_API_KEY in the Secrets panel or enter it in Nova Settings.",
                isError = true
            )
        }

        // Build content turns from history (limit to last 10 messages for token efficiency)
        val contents = mutableListOf<GeminiContent>()

        val recentHistory = history.takeLast(10)
        for (msg in recentHistory) {
            val role = if (msg.role == "user") "user" else "model"
            contents.add(
                GeminiContent(
                    role = role,
                    parts = listOf(GeminiPart(text = msg.content))
                )
            )
        }

        // Current turn
        val currentParts = mutableListOf<GeminiPart>()
        currentParts.add(GeminiPart(text = prompt))
        if (!imageBase64.isNullOrBlank() && !imageMimeType.isNullOrBlank()) {
            currentParts.add(
                GeminiPart(
                    inlineData = GeminiInlineData(
                        mimeType = imageMimeType,
                        data = imageBase64
                    )
                )
            )
        }
        contents.add(GeminiContent(role = "user", parts = currentParts))

        // System instruction
        val sysInstruction = systemInstructionText?.let {
            GeminiContent(parts = listOf(GeminiPart(text = it)))
        }

        // Search grounding tools if enabled
        val tools = if (enableSearchGrounding) {
            listOf(mapOf("googleSearch" to emptyMap<String, Any>()))
        } else null

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = sysInstruction,
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7f,
                topP = 0.95f
            ),
            tools = tools
        )

        try {
            val response = apiService.generateContent(
                model = model,
                apiKey = apiKey,
                request = request
            )

            if (response.isSuccessful) {
                val body = response.body()
                val candidate = body?.candidates?.firstOrNull()
                val candidateParts = candidate?.content?.parts ?: emptyList()
                val responseText = candidateParts.mapNotNull { it.text }.joinToString("\n").trim()

                // Extract citations / grounding chunks
                val sources = mutableListOf<GroundingSource>()
                candidate?.groundingMetadata?.groundingChunks?.forEach { chunk ->
                    val web = chunk.web
                    if (web != null && !web.uri.isNullOrBlank()) {
                        val title = if (!web.title.isNullOrBlank()) web.title else web.uri
                        sources.add(GroundingSource(title = title, url = web.uri))
                    }
                }

                if (responseText.isNotEmpty()) {
                    GeminiResult(text = responseText, sources = sources.distinctBy { it.url }, isError = false)
                } else {
                    GeminiResult(
                        text = "Nova received an empty response from Gemini. Please try again or rephrase.",
                        isError = true
                    )
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                Log.e(TAG, "API Error: ${response.code()} $errorBody")
                val friendlyMessage = when (response.code()) {
                    400 -> "Request Error (400): Bad parameters or invalid request syntax. $errorBody"
                    401, 403 -> "Authentication Error (${response.code()}): Invalid API key or quota unauthorized."
                    429 -> "Rate Limit Exceeded (429): Gemini quota limit reached. Please wait a moment."
                    500, 503 -> "Gemini Service Unavailable (${response.code()}): Google AI server temporary hiccup."
                    else -> "API Error (${response.code()}): $errorBody"
                }
                GeminiResult(text = friendlyMessage, isError = true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception", e)
            val message = e.localizedMessage ?: "Unknown network error"
            GeminiResult(
                text = "Connection failure: $message. Please check your internet connection.",
                isError = true
            )
        }
    }
}
