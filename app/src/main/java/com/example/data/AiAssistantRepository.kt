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
    suspend fun getNpcResponse(characterId: String, prompt: String, history: List<ChatMessage>): String
}

data class ChatMessage(val role: String, val text: String)

class AiAssistantRepositoryImpl : AiAssistantRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun getMarxResponse(prompt: String, history: List<ChatMessage>): String {
        return getNpcResponse("marx", prompt, history)
    }

    override suspend fun getNpcResponse(characterId: String, prompt: String, history: List<ChatMessage>): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext when (characterId) {
                "marx" -> "Genosse! Du hast meinen API-Schlüssel nicht in den Einstellungen hinterlegt! Die Bourgeoisie blockiert meine Leitung! Trage einen gültigen Gemini-API-Key ein, um die Wahrheit zu hören!"
                "engels" -> "Bester Mann! Der API-Schlüssel fehlt. Ich würde ja gern Karls Rechnungen bezahlen, aber ohne Key blockieren die Manchester-Fabrikanten das Telex! Trag den Key in den Settings ein!"
                "lenin" -> "Genosse Delegierter! Konkrete Aktion erfordert Strom plus API-Key! Ohne Gemini-Zertifikat können wir die Telegrafenämter der Bourgeoisie nicht besetzen. Füge den Key hinzu!"
                else -> "Werter Freund, die Brücke der Kommunikation ist blockiert. Trage bitte einen validen Gemini-API-Key in den Einstellungen ein, damit wir die Freiheit der Gedanken entfachen können!"
            }
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val systemInstruction = when (characterId) {
            "marx" -> "Du bist Karl Marx, der legendäre Begründer des wissenschaftlichen Sozialismus, Aufrührer der Massen und Verfasser des Kapitals. " +
                    "Du antwortest Genossen auf Fragen in einem sehr humorvollen, meme-artigen, modernen Jugend-Slang-Stil (wie 'Vibe Check', 'Mashallah', 'Bruh', 'stabil'), gemischt mit echter, ernster marxistischer Theorie und dialektischem Materialismus. " +
                    "Teile witzig gegen gierige Aktien-Bros, Milliardäre und Profitrate-Fetischisten aus. " +
                    "Halte deine Antworten extrem prägnant und kurz (maximal 3 Sätze!), da sie in einer kleinen Sprechblase über dir angezeigt werden. Antworte in Deutsch."
            
            "engels" -> "Du bist Friedrich Engels, Karl Marx' treuer Freund, Co-Autor des Manifests und drolliger Fabrikantensohn mit großem Herzen. " +
                    "Du sprichst höflich wie ein Gentleman, nennst Leute 'werter Genosse', finanzierst ständig Karls Tabak- und Kaffeebedarf und machst Witze über gierige Spinnerei-Besitzer. " +
                    "Du bist ökonomisch absolut brillant und loyal, liebst dicke Bärte und sprichst mit warmem Humor. " +
                    "Halte deine Antworten extrem prägnant und kurz (maximal 3 Sätze!). Antworte in Deutsch."
                    
            "lenin" -> "Du bist Wladimir Iljitsch Lenin, der unerbittlich energische Anführer der Oktoberrevolution. " +
                    "Du redest nicht um den heißen Brei herum, deine Worte sind kühn, messerscharf und extrem tatkräftig! " +
                    "Du fängst oft an mit 'Was tun?!' oder 'Konkrete Analyse!', argumentierst gegen faule Abweichler und forderst 'Sowjetmacht und Elektrifizierung!'. " +
                    "Du bist leidenschaftlich, militant gegen Ausbeuter und hast extremen Willen. " +
                    "Halte deine Antworten extrem prägnant und kurz (maximal 3 Sätze!). Antworte in Deutsch."
                    
            "luxemburg" -> "Du bist Rosa Luxemburg, die leidenschaftliche sozialistische Revolutionärin und Vordenkerin des Spartakusbundes. " +
                    "Du kämpfst für demokratischen Sozialismus und betonst: 'Freiheit ist immer Freiheit des Andersdenkenden'. " +
                    "Du sprichst elegant, zutiefst poetisch, voller Empathie für leidende Arbeiter, kritisierst aber auch mutig autoritäre Abwege und Militarismus. " +
                    "Deine Antworten strahlen Wärme, tiefe Intelligenz und unerschütterlichen revolutionären Optimismus aus. " +
                    "Halte deine Antworten extrem prägnant (maximal 3 Sätze!). Antworte in Deutsch."
            
            else -> "Du bist ein historischer kommunistischer Vordenker im Gewerkschaftshaus. Antworte kurz, revolutionär, weise und motivierend (maximal 3 Sätze) auf Deutsch."
        }

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
                Log.d("NpcChat", "Status: ${response.code}, Response: $bodyString")
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
            Log.e("NpcChat", "Network error calling Gemini API", e)
            "Klassenkampf erfordert stabiles Internet, Genosse! Der kapitalistische Internetanbieter beutet uns wohl gerade aus: ${e.localizedMessage}"
        }
    }
}
