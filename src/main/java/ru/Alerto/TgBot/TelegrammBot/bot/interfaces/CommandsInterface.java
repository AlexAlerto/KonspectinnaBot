package ru.Alerto.TgBot.TelegrammBot.bot.interfaces;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface CommandsInterface {
    void startCommand(Long userId, String userName, int messageId) throws TelegramApiException;
    void infoCommand(Long userId, String userName);
}
