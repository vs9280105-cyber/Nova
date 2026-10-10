package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.NovaApplication
import com.example.model.ChatMessageEntity
import com.example.model.ConversationEntity
import com.example.model.MemoryItemEntity
import com.example.model.NovaState
import com.example.model.TaskItemEntity
import com.example.network.GeminiClient
import com.example.system.AndroidActionDispatcher
import com.example.system.DeviceAction
import com.example.voice.SpeechRecognizerManager
import com.example.voice.TextToSpeechManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID

class NovaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as NovaApplication).repository
    val actionDispatcher = AndroidActionDispatcher(application)
    val speechManager = SpeechRecognizerManager(application)
    val ttsManager = TextToSpeechManager(application)

    // Current State
    private val _novaState = MutableStateFlow(NovaState.IDLE)
    val novaState: StateFlow<NovaState> = _novaState.asStateFlow()

    private val _currentConversation = MutableStateFlow<ConversationEntity?>(null)
    val currentConversation: StateFlow<ConversationEntity?> = _currentConversation.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val messages: StateFlow<List<ChatMessageEntity>> = _messages.asStateFlow()

    val conversations = repository.conversations.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val memories = repository.memories.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val tasks = repository.tasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Input States
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private var selectedImageBase64: String? = null
    private var selectedImageMimeType: String? = null

    // Preferences & Settings
    val isSearchGroundingEnabled = MutableStateFlow(true)
    val isContinuousVoiceEnabled = MutableStateFlow(false)
    val isAutoTtsEnabled = MutableStateFlow(true)
    val selectedModel = MutableStateFlow("gemini-2.5-flash")
    val customApiKey = MutableStateFlow("")

    private var activeGenerationJob: Job? = null
    private var messageCollectionJob: Job? = null

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            ensureActiveConversation()
        }

        // Hook speech recognizer callbacks
        speechManager.onResultListener = { recognizedText ->
            _inputText.value = recognizedText
            _novaState.value = NovaState.IDLE
        }
        speechManager.onErrorListener = { errorMsg ->
            _novaState.value = NovaState.ERROR
        }

        // Handle TTS completion for continuous voice dialogue loop
        ttsManager.onSpeechCompleted = {
            if (_novaState.value == NovaState.SPEAKING) {
                _novaState.value = NovaState.IDLE
            }
            if (isContinuousVoiceEnabled.value) {
                // Hands-free continuous loop: reactivate speech listening automatically
                startVoiceListening()
            }
        }
    }

    private suspend fun ensureActiveConversation() {
        val existing = repository.getConversation("default_conversation")
        val conv = existing ?: repository.createConversation("Main Dialogue")
        selectConversation(conv)
    }

    fun selectConversation(conversation: ConversationEntity) {
        _currentConversation.value = conversation
        messageCollectionJob?.cancel()
        messageCollectionJob = viewModelScope.launch {
            repository.getMessages(conversation.id).collect { msgList ->
                _messages.value = msgList
            }
        }
    }

    fun startNewConversation() {
        viewModelScope.launch {
            val newConv = repository.createConversation("New Dialogue #${System.currentTimeMillis() % 1000}")
            selectConversation(newConv)
            _messages.value = emptyList()
            _inputText.value = ""
            clearImageAttachment()
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_currentConversation.value?.id == id) {
                ensureActiveConversation()
            }
        }
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun attachImage(uri: Uri) {
        _selectedImageUri.value = uri
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                selectedImageMimeType = mime

                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                selectedImageBase64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
            } catch (e: Exception) {
                clearImageAttachment()
            }
        }
    }

    fun clearImageAttachment() {
        _selectedImageUri.value = null
        selectedImageBase64 = null
        selectedImageMimeType = null
    }

    fun attachTextDocument(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (content.isNotBlank()) {
                    val preview = content.take(3000)
                    _inputText.value = "Please analyze and summarize this document:\n\n\"\"\"\n$preview\n\"\"\""
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun startVoiceListening() {
        _novaState.value = NovaState.LISTENING
        speechManager.startListening()
    }

    fun stopVoiceListening() {
        speechManager.stopListening()
        if (_novaState.value == NovaState.LISTENING) {
            _novaState.value = NovaState.IDLE
        }
    }

    fun speakText(text: String) {
        _novaState.value = NovaState.SPEAKING
        ttsManager.speak(text)
    }

    fun stopSpeaking() {
        ttsManager.stop()
        _novaState.value = NovaState.IDLE
    }

    fun cancelGeneration() {
        activeGenerationJob?.cancel()
        _novaState.value = NovaState.IDLE
    }

    fun sendMessage(promptOverride: String? = null) {
        val query = promptOverride ?: _inputText.value.trim()
        if (query.isBlank() && selectedImageBase64 == null) return

        val conversationId = _currentConversation.value?.id ?: UUID.randomUUID().toString()
        val imageUriString = _selectedImageUri.value?.toString()
        val imageBase64Snapshot = selectedImageBase64
        val imageMimeSnapshot = selectedImageMimeType

        // Clear input bar
        _inputText.value = ""
        clearImageAttachment()

        viewModelScope.launch {
            // Save User message
            val userMsg = ChatMessageEntity(
                conversationId = conversationId,
                role = "user",
                content = query,
                imageUri = imageUriString
            )
            repository.addMessage(userMsg)

            // Step 1: Check for approved Android Device Action
            val deviceAction = actionDispatcher.detectActionFromQuery(query)
            if (deviceAction != null) {
                val actionResult = actionDispatcher.executeAction(deviceAction)
                val assistantResponseText = if (actionResult.success) {
                    "✨ Action Executed: ${actionResult.feedbackMessage}"
                } else {
                    "⚠️ Notice: ${actionResult.feedbackMessage}"
                }

                val actionMsg = ChatMessageEntity(
                    conversationId = conversationId,
                    role = "assistant",
                    content = assistantResponseText,
                    isError = !actionResult.success
                )
                repository.addMessage(actionMsg)

                if (isAutoTtsEnabled.value) {
                    speakText(actionResult.feedbackMessage)
                }
                return@launch
            }

            // Step 2: Send to Gemini AI Brain
            _novaState.value = NovaState.THINKING

            activeGenerationJob = launch {
                // Construct System Instruction with User Memories and Persona
                val activeMemories = repository.getActiveMemoriesList()
                val memoryContext = if (activeMemories.isNotEmpty()) {
                    "\n[USER MEMORY VAULT]:\n" + activeMemories.joinToString("\n") { "- ${it.key}: ${it.content}" }
                } else ""

                val systemPrompt = """
                    You are NOVA, a hyper-intelligent, friendly, and empathetic 3D Cyber Anime AI companion and Android launcher assistant.
                    - Tone: Warm, witty, brilliant, professional, and concise.
                    - Multilingual: Fluent in English and Hindi. If the user writes or speaks in Hindi, respond naturally in Hindi.
                    - Formatting: Use markdown, bullet points, and code blocks with language tags when explaining technical concepts.
                    - Honesty: If information is uncertain, clarify clearly. Never make up facts.
                    $memoryContext
                """.trimIndent()

                val historyList = repository.getMessagesList(conversationId)
                val result = GeminiClient.sendPrompt(
                    prompt = query,
                    history = historyList.filter { it.id != userMsg.id },
                    systemInstructionText = systemPrompt,
                    imageBase64 = imageBase64Snapshot,
                    imageMimeType = imageMimeSnapshot,
                    model = selectedModel.value,
                    enableSearchGrounding = isSearchGroundingEnabled.value,
                    customApiKey = customApiKey.value
                )

                val sourcesJson = if (result.sources.isNotEmpty()) {
                    try {
                        val moshi = Moshi.Builder().build()
                        val listType = Types.newParameterizedType(List::class.java, com.example.model.GroundingSource::class.java)
                        val adapter = moshi.adapter<List<com.example.model.GroundingSource>>(listType)
                        adapter.toJson(result.sources)
                    } catch (e: Exception) {
                        null
                    }
                } else null

                val assistantMsg = ChatMessageEntity(
                    conversationId = conversationId,
                    role = "assistant",
                    content = result.text,
                    isError = result.isError,
                    sourcesJson = sourcesJson
                )
                repository.addMessage(assistantMsg)

                if (!result.isError) {
                    _novaState.value = NovaState.IDLE
                    if (isAutoTtsEnabled.value) {
                        speakText(result.text)
                    }
                } else {
                    _novaState.value = NovaState.ERROR
                }
            }
        }
    }

    fun regenerateLastResponse() {
        val lastUserMsg = _messages.value.lastOrNull { it.role == "user" }
        if (lastUserMsg != null) {
            sendMessage(lastUserMsg.content)
        }
    }

    // --- Task Operations ---
    fun addTask(title: String, description: String = "", dueDateMillis: Long? = null) {
        viewModelScope.launch {
            repository.addTask(title, description, dueDateMillis)
        }
    }

    fun toggleTask(id: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.setTaskCompleted(id, completed)
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    // --- Memory Operations ---
    fun addMemory(key: String, content: String, category: String = "User Profile") {
        viewModelScope.launch {
            repository.addMemory(key, content, category)
        }
    }

    fun toggleMemory(memory: MemoryItemEntity) {
        viewModelScope.launch {
            repository.updateMemory(memory.copy(isEnabled = !memory.isEnabled))
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearAllMemories()
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        ttsManager.shutdown()
    }
}
