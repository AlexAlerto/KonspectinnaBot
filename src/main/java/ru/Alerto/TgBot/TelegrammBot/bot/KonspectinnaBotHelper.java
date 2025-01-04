package ru.Alerto.TgBot.TelegrammBot.bot;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.Alerto.TgBot.DataBase.repo.FilesRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class KonspectinnaBotHelper {

    private static KonspectinnaBotHelper instance;

    private final FilesRepository filesRepository;

    public KonspectinnaBotHelper(FilesRepository filesRepository) {
        this.filesRepository = filesRepository;
    }

    public static InlineKeyboardMarkup generateKeyboard(File directory){
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        String filePath = directory.getPath().replace("\\", "/");

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            InlineKeyboardButton button = new InlineKeyboardButton();

            button.setText(file.getName());
            button.setCallbackData(String.valueOf(instance.filesRepository.findByPath(filePath.replace("\\", "/") + "/" + file.getName()).orElseThrow().getId()));

            rows.add(new ArrayList<>(List.of(button)));
        }

        if(filePath.split("/").length != 3){

            InlineKeyboardButton buttonBack = new InlineKeyboardButton();
            buttonBack.setText("◀ Back");
            buttonBack.setCallbackData(String.valueOf(instance.filesRepository.findByPath(filePath.replace( "/" + filePath.split("/")[filePath.split("/").length-1], "")).orElseThrow().getId()));

            InlineKeyboardButton buttonMain = new InlineKeyboardButton();
            buttonMain.setText("\uD83D\uDC7EMain menu");
            buttonMain.setCallbackData("Main");

            InlineKeyboardButton buttonInfo = new InlineKeyboardButton();
            buttonInfo.setText("❓Info");
            buttonInfo.setCallbackData("Info|" + filePath);

            rows.add(new ArrayList<>(List.of(buttonBack, buttonInfo, buttonMain)));
        }

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }


    public static Long getUserIdFromUpdate(Update update) {
        return update.hasMessage() ? update.getMessage().getFrom().getId() : update.getCallbackQuery().getFrom().getId();
    }


    public static String getUserNameFromUpdateFromUpdate(Update update) {
        return update.hasMessage() ? update.getMessage().getFrom().getUserName() : update.getCallbackQuery().getFrom().getUserName();
    }


    @PostConstruct
    public void init() {
        instance = this;
    }
}
