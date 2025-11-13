# Telegram Code Sender Plugin for PyCharm

A PyCharm plugin that sends selected code to Telegram with proper formatting.

## Features

- Send selected code to Telegram with one click
- Automatic code formatting with backticks
- Context menu integration
- Keyboard shortcut (Ctrl+Alt+T)

## Setup

1. Create a Telegram bot via @BotFather
2. Get your bot token and chat ID
3. Replace placeholders in `SendToTelegramAction.kt`:
   ```kotlin
   private const val BOT_TOKEN = "your_bot_token"
   private const val CHAT_ID = "your_chat_id"