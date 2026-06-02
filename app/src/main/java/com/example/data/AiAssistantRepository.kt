package com.example.data

interface AiAssistantRepository {
    // TODO: Implement Gemini API integration in the future
    suspend fun analyzeArgument(text: String): String
}
