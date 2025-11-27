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
        private const val BOT_TOKEN = "8585869914:AAHt5t8cGj2_70sOyRckLJA8HxSUP1Fc0Xs"
        private const val CHAT_ID = "1759734963"
    }
    /**
     * Выполняется при активации действия
     * Принимает:
     *     (AnActionEvent) event - событие, содержащее контекст выполнения действия
     * Возвращает:
     *     (void)
     */
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
    /**
     * Обновляет состояние действия (видимость и доступность)
     * Принимает:
     *     (AnActionEvent) event - событие, содержащее контекст для обновления представления
     * Возвращает:
     *     (void)
     */
    override fun update(event: AnActionEvent) {
        val editor = event.getData(CommonDataKeys.EDITOR)
        val hasSelection = editor != null && editor.selectionModel.hasSelection()
        event.presentation.isEnabledAndVisible = hasSelection
    }

    /**
     * Отправляет форматированный текст в Telegram чат через Bot API
     * Принимает:
     *     (String) text - текст для отправки в Telegram (уже форматированный с кодом)
     * Возвращает:
     *     (void)
     * Исключения:
     *     RuntimeException - если Telegram API возвращает код ответа отличный от 200
     */
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
            os.write(input, 0, input.size)
        }

        val responseCode = conn.responseCode
        if (responseCode != 200) {
            throw RuntimeException("Telegram API returned: $responseCode")
        }

        conn.disconnect()
    }
}
