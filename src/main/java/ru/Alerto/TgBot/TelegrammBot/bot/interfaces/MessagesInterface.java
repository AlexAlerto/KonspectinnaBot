package ru.Alerto.TgBot.TelegrammBot.bot.interfaces;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

public interface MessagesInterface {
    void sendFile(Long chatId, File file) throws TelegramApiException;
    void deleteMessage(Long chatId, Integer messageId) throws TelegramApiException;
    void sendInlineKeyboardMessage(Long chatId, InlineKeyboardMarkup inlineKeyboardMarkup, String directory);
    void sendMessage(Long chatId, String text) throws TelegramApiException;

}
