package ru.Alerto.TgBot.TelegrammBot.bot.scripts;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.Alerto.TgBot.TelegrammBot.bot.KonspectinnaBot;
import ru.Alerto.TgBot.TelegrammBot.bot.interfaces.MessagesInterface;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@AllArgsConstructor
public class Messages implements MessagesInterface {

    @Override
    public void sendFile(Long chatId, File file) throws TelegramApiException {
        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(chatId);

        InputFile inputFile = new InputFile();
        inputFile.setMedia(file);
        sendDocumentRequest.setDocument(inputFile);

        String beautifulPath = file.getPath().replace("./files/", "")
                .replace("/", " -> ");

        sendDocumentRequest.setCaption(beautifulPath + "\n\n" + "@konspectinnobot");

        try {
            if(file.getName().endsWith(".txt")){
                sendMessage(chatId, Files.readString(Paths.get(file.getPath())));
            }else{
                KonspectinnaBot.executeFile(sendDocumentRequest);
            }
        } catch (TelegramApiException | IOException e) {
            sendMessage(chatId, "File sending error: " + beautifulPath);
            KonspectinnaBot.LOG.error("Ошибка отправки файла");
        }
    }

    @Override
    public void deleteMessage(Long chatId, Integer messageId) throws TelegramApiException {
        KonspectinnaBot.executeMessage(new DeleteMessage(chatId.toString(), messageId));
    }

    @Override
    public void sendInlineKeyboardMessage(Long chatId, InlineKeyboardMarkup inlineKeyboardMarkup, String directory) {
        directory = directory
                .replace("./files/", "")
                .replace("/", " -> ");

        SendMessage message = new SendMessage(chatId.toString(), "\uD83D\uDCCD Select the required" +
                "\n" +
                "You are in: " + directory);

        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            KonspectinnaBot.executeMessage(message);
        } catch (TelegramApiException e) {
            KonspectinnaBot.LOG.error("Ошибка отправки сообщения", e);
        }
    }

    @Override
    public void sendMessage(Long chatId, String text) throws TelegramApiException {
        KonspectinnaBot.executeMessage(new SendMessage(String.valueOf(chatId), text));
    }

}
