package ru.Alerto.TgBot.TelegrammBot.bot;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.Alerto.TgBot.TelegrammBot.models.*;
import ru.Alerto.TgBot.TelegrammBot.repo.*;

import java.io.File;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class ExchangeRatesBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeRatesBot.class);

    private static final String START = "/start";
    private static final String STOP = "/stop";
    private static final String RELOAD = "/reload";
    private static final String QQ = "/qq";

    public ExchangeRatesBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BanUserRepository banUserRepository;

    @Autowired
    private FilesRepository filesRepository;

    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = getUserIdFromUpdate(update);
        String chatName = getUserNameFromUpdateFromUpdate(update);

        if (banUserRepository.findByTgId(chatId).isPresent()){
            SendLog("@" + chatName + " нажал на /start но он находится в чёрном списке");
            sendMessage(chatId, "You've been blocked by the administrator.");
            return;
        }

        else if (userRepository.findByTgId(chatId).isEmpty()) {
            SendLog("@" + chatName + " нажал на /start но он не авторизован");
            sendMessage(chatId, "To use the bot you need to be verified in the bot - @konspectinno_proxybot");
            return;
        }

        userRepository.findByTgId(chatId).ifPresent(user -> {
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
        });

        if (update.hasMessage() && update.getMessage().hasText()) {

            String message = update.getMessage().getText();

            switch (message.split(" ")[0]){
                case START -> {
                    SendLog("@" + update.getMessage().getFrom().getUserName() + " нажал на /start ");
                    deleteMessage(chatId, update.getMessage().getMessageId());
                    startCommand(chatId);
                }

                case RELOAD -> {
                    if (chatId == 1344071668L) searchAndSaveFilesOnDB();
                }

                case QQ -> {
                    if (chatId == 1344071668L){
                        if(message.split(" ")[1].equals("all")){
                            QQAllCommand(message.replace(QQ + " " +  message.split(" ")[1], ""));
                        }
                        else {
                            QQCommand(message.split(" ")[1], message.replace(QQ + " " +  message.split(" ")[1], ""));
                        }
                    }
                }

                case STOP -> System.exit(0);
            }
        }

        else if (update.hasCallbackQuery()){

            deleteMessage(update.getCallbackQuery().getMessage().getChatId(), update.getCallbackQuery().getMessage().getMessageId());

            String callBackData = update.getCallbackQuery().getData();
            File directory = new File(filesRepository.findById(Long.valueOf(callBackData)).orElseThrow().getPath());

            if(callBackData.equals("Main")){
                startCommand(chatId);
                return;
            }

            if(directory.isDirectory()){
                SendLog("@" + update.getCallbackQuery().getFrom().getUserName() + " перешёл в папку " + filesRepository.findById(Long.valueOf(update.getCallbackQuery().getData())).orElseThrow().getPath().replace("./", "").replace("/", " -> "));

                sendInlineKeyboardMessage(chatId, generateKeyboard(directory), directory.getPath());
            }
            else {
                sendMessage(chatId, "The request is being processed...");
                sendFile(chatId, directory);

                deleteMessage(update.getCallbackQuery().getMessage().getChatId(), update.getCallbackQuery().getMessage().getMessageId() + 1);
                SendLog("@" + update.getCallbackQuery().getFrom().getUserName() + " скачал файл " + filesRepository.findById(Long.valueOf(update.getCallbackQuery().getData())).orElseThrow().getPath().replace("./", "").replace("/", " -> "));

                String filePath = filesRepository.findById(Long.valueOf(callBackData)).orElseThrow().getPath();
                filePath = filePath.replace( "/" + filePath.split("/")[filePath.split("/").length - 1], "");
                sendInlineKeyboardMessage(chatId, generateKeyboard(new File(filePath)), filePath);
            }
        }
    }

    private void QQCommand(String direction, String message) {
        userRepository.findAllByDirection(direction).forEach(
                user -> sendMessage(user.getTgId(), message)
        );
    }

    private void QQAllCommand(String message) {
        userRepository.findAll().forEach(
                user -> sendMessage(user.getTgId(), message)
        );
    }

    private void startCommand(Long chatId){
        File directory = new File("./files/" + userRepository.findByTgId(chatId).orElseThrow().getDirection());

        sendInlineKeyboardMessage(chatId, generateKeyboard(directory), directory.getPath());
    }

    private void SendLog(String message) {
        sendMessage(1104443126L, message);
        sendMessage(1344071668L, message);
    }

    private InlineKeyboardMarkup generateKeyboard(File directory){
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        String filePath = directory.getPath().replace("\\", "/");

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            InlineKeyboardButton button = new InlineKeyboardButton();

            button.setText(file.getName());
            button.setCallbackData(String.valueOf(filesRepository.findByPath(filePath.replace("\\", "/") + "/" + file.getName()).orElseThrow().getId()));

            rows.add(new ArrayList<>(List.of(button)));
        }

        if(filePath.split("/").length != 3){

            InlineKeyboardButton buttonBack = new InlineKeyboardButton();
            buttonBack.setText("◀ Back");
            buttonBack.setCallbackData(String.valueOf(filesRepository.findByPath(filePath.replace( "/" + filePath.split("/")[filePath.split("/").length-1], "")).orElseThrow().getId()));

            InlineKeyboardButton buttonMain = new InlineKeyboardButton();
            buttonMain.setText("\uD83D\uDC7EMain menu");
            buttonMain.setCallbackData("Main");

            rows.add(new ArrayList<>(List.of(buttonBack, buttonMain)));

        }

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    private void sendMessage(Long chatId, String text) {
        try {
            execute(new SendMessage(String.valueOf(chatId), text));
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки сообщения", e);
        }
    }

    private void sendInlineKeyboardMessage(Long chatId, InlineKeyboardMarkup inlineKeyboardMarkup, String directory) {
        directory = directory
                .replace("./files/", "")
                .replace("/", " -> ");

        SendMessage message = new SendMessage(chatId.toString(), "\uD83D\uDCCD Select the required" +
                "\n" +
                "You are in: " + directory);

        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки сообщения", e);
        }
    }

    public void deleteMessage(Long chatId, Integer messageId) {
        try {
            execute(new DeleteMessage(chatId.toString(), messageId));
        } catch (TelegramApiException e) {
            LOG.error("Ошибка удаления сообщения", e);
        }
    }

    private Long getUserIdFromUpdate(Update update) {
        return update.hasMessage() ? update.getMessage().getFrom().getId() : update.getCallbackQuery().getFrom().getId();
    }
    private String getUserNameFromUpdateFromUpdate(Update update) {
        return update.hasMessage() ? update.getMessage().getFrom().getUserName() : update.getCallbackQuery().getFrom().getUserName();
    }
    public void searchAndSaveFilesOnDB() {
        filesRepository.deleteAll();
        Path rootDir = Paths.get("./files");
        List<DirectoryOrFile> directoryOrFiles = new ArrayList<>();

        try {
            Files.walkFileTree(rootDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    directoryOrFiles.add(new DirectoryOrFile("./files/" + rootDir.relativize(dir).toString().replace("\\", "/")));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    directoryOrFiles.add(new DirectoryOrFile("./files/" + rootDir.relativize(file).toString().replace("\\", "/")));
                    return FileVisitResult.CONTINUE;
                }
            });

            savePathToDB(directoryOrFiles);

        } catch (IOException e) {
            LOG.error("Ошибка сканирования файлов и папок", e);
        }
    }

    private void savePathToDB(Iterable<DirectoryOrFile> directoryOrFiles) {
        filesRepository.saveAll(directoryOrFiles);
    }

    public void sendFile(Long chatId, File file) {
        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(chatId);

        InputFile inputFile = new InputFile();
        inputFile.setMedia(file);
        sendDocumentRequest.setDocument(inputFile);
        sendDocumentRequest.setCaption(file.getPath().replace("./files/", "")
                .replace("/", " -> ") + "\n\n" + "@konspectinnobot");

        try {
            execute(sendDocumentRequest);
        } catch (TelegramApiException e) {
            sendMessage(chatId, "File sending error: " + file.getPath().replace("./files/", "")
                    .replace("/", " -> "));
            LOG.error("Ошибка отправки файла");
        }
    }

    @PostConstruct
    public void init() {
        this.searchAndSaveFilesOnDB();
    }

    @Override
    public String getBotUsername() {
        return "Konspectinna-proxy";
    }
}
