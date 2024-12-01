package ru.Alerto.TgBot.TelegrammBot.bot.interfaces;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface CommandsInterface {
    void QQOneCommand(String direction, String message);
    void QQAllCommand(String message);
    void QQCommand(Long userId, String message) ;
    void startCommand(Long userId, String userName, int messageId) throws TelegramApiException;
    void infoCommand(Long userId, String userName);
}
