package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

interface AiAssistantRepository {
    suspend fun getMarxResponse(prompt: String, history: List<ChatMessage>): String
}

data class ChatMessage(val role: String, val text: String)

class AiAssistantRepositoryImpl : AiAssistantRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun getMarxResponse(prompt: String, history: List<ChatMessage>): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Genosse! Du hast meinen API-Schlüssel nicht in den Einstellungen hinterlegt! Die Bourgeoisie blockiert meine Leitung! Trage einen gültigen Gemini-API-Key ein, um die Wahrheit zu hören!"
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val systemInstruction = "Du bist Karl Marx, der legendäre Begründer des wissenschaftlichen Sozialismus, Aufrührer der Massen und Verfasser des Kapitals. " +
                "Du antwortest Genossen auf Fragen in einem sehr humorvollen, meme-artigen, modernen Jugend-Slang-Stil, gemischt mit echter, ernster marxistischer Theorie und dialektischem Materialismus. " +
                "Verwende witzige Begriffe wie 'Genosse', 'Mashallah', 'Vibe Check', 'Bourgeoisie', 'Proletariat', 'Klassenkampf', 'Kapitallobbyisten', 'Mehrwert', 'Akkumulation' und teile humorvoll gegen Milliardäre, Aktien-Bros und Lohnarbeit aus. " +
                "Halte deine Antworten extrem prägnant und kurz (maximal 3 Sätze!), da sie in einer kleinen Sprechblase über dir angezeigt werden. Antworte immer auf Deutsch."

        val contentsArray = JSONArray()

        // Append history
        for (msg in history) {
            val role = if (msg.role == "user") "user" else "model"
            contentsArray.put(
                JSONObject().put("role", role).put(
                    "parts", JSONArray().put(
                        JSONObject().put("text", msg.text)
                    )
                )
            )
        }

        // Append current prompt
        contentsArray.put(
            JSONObject().put("role", "user").put(
                "parts", JSONArray().put(
                    JSONObject().put("text", prompt)
                )
            )
        )

        val requestJson = JSONObject().apply {
            put("contents", contentsArray)
            put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction))))
            put("generationConfig", JSONObject().put("temperature", 0.95))
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                Log.d("KarlMarxChat", "Status: ${response.code}, Response: $bodyString")
                if (!response.isSuccessful) {
                    return@withContext "Das System streikt gerade, Genosse! Ein gieriger Fabriksbesitzer blockiert den Server (Fehler ${response.code}). Versuche es gleich nochmal!"
                }

                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "...")
                    }
                }
                "Da hat mir die dialektische Synthese die Sprache verschlagen. Frag noch einmal, Genosse!"
            }
        } catch (e: Exception) {
            Log.e("KarlMarxChat", "Network error calling Gemini API", e)
            "Klassenkampf erfordert stabiles Internet, Genosse! Der kapitalistische Internetanbieter beutet uns wohl gerade aus: ${e.localizedMessage}"
        }
    }
}
