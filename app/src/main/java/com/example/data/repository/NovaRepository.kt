package com.example.data.repository

import com.example.data.local.NovaDao
import com.example.model.ChatMessageEntity
import com.example.model.ConversationEntity
import com.example.model.MemoryItemEntity
import com.example.model.TaskItemEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class NovaRepository(private val dao: NovaDao) {

    // --- Conversations ---
    val conversations: Flow<List<ConversationEntity>> = dao.getAllConversations()

    suspend fun getConversation(id: String): ConversationEntity? = dao.getConversationById(id)

    suspend fun createConversation(title: String = "New Dialogue"): ConversationEntity {
        val conversation = ConversationEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.insertConversation(conversation)
        return conversation
    }

    suspend fun updateConversationTitle(id: String, newTitle: String) {
        val existing = dao.getConversationById(id)
        if (existing != null) {
            dao.updateConversation(existing.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deleteConversation(id: String) {
        dao.deleteConversationById(id)
    }

    suspend fun clearAllConversations() {
        dao.clearAllConversations()
    }

    // --- Messages ---
    fun getMessages(conversationId: String): Flow<List<ChatMessageEntity>> =
        dao.getMessagesForConversation(conversationId)

    suspend fun getMessagesList(conversationId: String): List<ChatMessageEntity> =
        dao.getMessagesListForConversation(conversationId)

    suspend fun addMessage(message: ChatMessageEntity) {
        dao.insertMessage(message)
        // Update conversation's updatedAt
        val existing = dao.getConversationById(message.conversationId)
        if (existing != null) {
            dao.updateConversation(existing.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deleteMessage(id: String) {
        dao.deleteMessageById(id)
    }

    // --- Memories ---
    val memories: Flow<List<MemoryItemEntity>> = dao.getAllMemories()

    suspend fun getActiveMemoriesList(): List<MemoryItemEntity> = dao.getEnabledMemoriesList()

    suspend fun addMemory(key: String, content: String, category: String = "Preferences") {
        dao.insertMemory(
            MemoryItemEntity(
                key = key,
                content = content,
                category = category,
                isEnabled = true,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateMemory(memory: MemoryItemEntity) {
        dao.updateMemory(memory)
    }

    suspend fun deleteMemory(id: Long) {
        dao.deleteMemoryById(id)
    }

    suspend fun clearAllMemories() {
        dao.clearAllMemories()
    }

    // --- Tasks ---
    val tasks: Flow<List<TaskItemEntity>> = dao.getAllTasks()

    suspend fun addTask(title: String, description: String = "", dueDateMillis: Long? = null) {
        dao.insertTask(
            TaskItemEntity(
                title = title,
                description = description,
                dueDateMillis = dueDateMillis,
                isCompleted = false,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateTask(task: TaskItemEntity) {
        dao.updateTask(task)
    }

    suspend fun setTaskCompleted(id: Long, completed: Boolean) {
        dao.setTaskCompleted(id, completed)
    }

    suspend fun deleteTask(id: Long) {
        dao.deleteTaskById(id)
    }

    suspend fun seedInitialDataIfEmpty() {
        val currentMemories = dao.getEnabledMemoriesList()
        if (currentMemories.isEmpty()) {
            dao.insertMemory(
                MemoryItemEntity(
                    key = "Assistant Persona",
                    content = "Nova is an advanced 3D cyber anime AI companion who is friendly, brilliant, concise, and caring.",
                    category = "Persona"
                )
            )
            dao.insertMemory(
                MemoryItemEntity(
                    key = "Language Skill",
                    content = "Fluent in English, Hindi, and technical code. Speaks Hindi whenever addressed in Hindi.",
                    category = "Language"
                )
            )
            dao.insertMemory(
                MemoryItemEntity(
                    key = "Launcher Authority",
                    content = "Authorized to open system apps, shortcuts, and toggle device settings on request.",
                    category = "Device"
                )
            )
        }
    }
}
