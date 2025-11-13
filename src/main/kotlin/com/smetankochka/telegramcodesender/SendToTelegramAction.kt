package com.smetankochka.telegramcodesender

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class SendToTelegramAction : AnAction() {

    companion object {
        // ЗАМЕНИТЕ НА СВОИ ЗНАЧЕНИЯ!
        private const val BOT_TOKEN = "your_bot_token"
        private const val CHAT_ID = "your_chat_id"
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val editor = event.getData(CommonDataKeys.EDITOR)

        if (editor == null) {
            Messages.showErrorDialog(project, "No editor found", "Error")
            return
        }

        val selectedText = editor.selectionModel.selectedText

        if (selectedText.isNullOrEmpty()) {
            Messages.showErrorDialog(project, "Please select some text first", "No Text Selected")
            return
        }

        // Обрамляем текст в ``` для форматирования кода
        val formattedText = "```\n$selectedText\n```"

        try {
            sendToTelegram(formattedText)
            Messages.showInfoMessage(project, "Code sent to Telegram successfully!", "Success")
        } catch (e: Exception) {
            Messages.showErrorDialog(
                project,
                "Failed to send to Telegram: ${e.message}\n\nPlease check your bot token and chat ID.",
                "Telegram Error"
            )
        }
    }

    override fun update(event: AnActionEvent) {
        // Показывать действие только когда есть выделенный текст
        val editor = event.getData(CommonDataKeys.EDITOR)
        val hasSelection = editor != null && editor.selectionModel.hasSelection()
        event.presentation.isEnabledAndVisible = hasSelection
    }

    private fun sendToTelegram(text: String) {
        val urlString = "https://api.telegram.org/bot$BOT_TOKEN/sendMessage"

        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 10000
        conn.readTimeout = 10000

        // Экранируем текст для JSON
        val escapedText = text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")

        val jsonBody = """
            {
                "chat_id": "$CHAT_ID",
                "text": "$escapedText",
                "parse_mode": "MarkdownV2"
            }
        """.trimIndent()

        conn.outputStream.use { os ->
            val input = jsonBody.toByteArray(StandardCharsets.UTF_8)
            os.write(input, 0, input.size)  // ← ИСПРАВЛЕНО: .size вместо .length
        }

        val responseCode = conn.responseCode
        if (responseCode != 200) {
            throw RuntimeException("Telegram API returned: $responseCode")
        }

        conn.disconnect()
    }
}