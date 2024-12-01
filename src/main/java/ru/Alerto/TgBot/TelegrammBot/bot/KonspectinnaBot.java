package ru.Alerto.TgBot.TelegrammBot.bot;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.Alerto.TgBot.DataBase.DataBase;
import ru.Alerto.TgBot.DataBase.repo.*;
import ru.Alerto.TgBot.TelegrammBot.bot.interfaces.CommandsInterface;
import ru.Alerto.TgBot.TelegrammBot.bot.interfaces.MessagesInterface;
import ru.Alerto.TgBot.TelegrammBot.bot.scripts.Commands;
import ru.Alerto.TgBot.TelegrammBot.bot.scripts.Messages;

import java.io.File;

import java.io.Serializable;
import java.util.List;

@Component
public class KonspectinnaBot extends TelegramLongPollingBot {

    private static KonspectinnaBot instance;

    public static final Logger LOG = LoggerFactory.getLogger(KonspectinnaBot.class);

    private static final String START = "/start";
    private static final String INFO = "/info";
    private static final String STOP = "/stop";
    private static final String RELOAD = "/reload";
    private static final String QQ = "/qq";

    private final UserRepository userRepository;
    private final BanUserRepository banUserRepository;
    private final FilesRepository filesRepository;

    private final MessagesInterface messages = new Messages();
    private final CommandsInterface commands = new Commands();
    private final MeterRegistry meterRegistry;

    public KonspectinnaBot(@Value("${bot.token}") String botToken, UserRepository userRepository, BanUserRepository banUserRepository, FilesRepository filesRepository, MeterRegistry meterRegistry) {
        super(botToken);
        this.userRepository = userRepository;
        this.banUserRepository = banUserRepository;
        this.filesRepository = filesRepository;
        this.meterRegistry = meterRegistry;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        Long userId = KonspectinnaBotHelper.getUserIdFromUpdate(update);
        String userName = KonspectinnaBotHelper.getUserNameFromUpdateFromUpdate(update);

        meterRegistry.counter("greetings_count", List.of()).increment();

        if (banUserRepository.findByTgId(userId).isPresent()){
            LOG.info("@{} нажал на /start но он находится в чёрном списке", userName);

            messages.sendMessage(userId, "You've been blocked by the administrator.");
            return;
        }

        else if (userRepository.findByTgId(userId).isEmpty()) {
            LOG.info("@{} нажал на /start но он не авторизован", userName);

            messages.sendMessage(userId, "To use the bot you need to be verified in the bot - @konspectinno_proxybot");
            return;
        }

        DataBase.userSaveOnDB(userId);

        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            Integer messageId = update.getMessage().getMessageId();

            switch (message.split(" ")[0]){
                case START -> commands.startCommand(userId, userName, messageId);
                case INFO -> commands.infoCommand(userId, userName);
                case RELOAD -> DataBase.searchAndSaveFilesOnDB(userId);
                case QQ -> commands.QQCommand(userId, message);
                case STOP -> System.exit(0);
            }
        }

        else if (update.hasCallbackQuery()){
            messages.deleteMessage(userId, update.getCallbackQuery().getMessage().getMessageId());

            String callBackData = update.getCallbackQuery().getData();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();

            if(callBackData.equals("Main")){
                commands.startCommand(userId, userName, messageId);
                return;
            } else if (callBackData.split("\\|")[0].equals("Info")) {
                commands.infoCommand(userId, userName);
                File directory = new File(callBackData.split("\\|")[1]);
                messages.sendInlineKeyboardMessage(userId, KonspectinnaBotHelper.generateKeyboard(directory), directory.getPath());
                return;
            }

            File directory = new File(filesRepository.findById(Long.valueOf(callBackData)).orElseThrow().getPath());

            if(directory.isDirectory()){
                LOG.info("@{} перешёл в папку {}", userName, filesRepository.findById(Long.valueOf(callBackData)).orElseThrow().getPath().replace("./", "").replace("/", " -> "));

                messages.sendInlineKeyboardMessage(userId, KonspectinnaBotHelper.generateKeyboard(directory), directory.getPath());
            }
            else {
                messages.sendMessage(userId, "The request is being processed...");

                messages.sendFile(userId, directory);

                messages.deleteMessage(userId, update.getCallbackQuery().getMessage().getMessageId() + 1);

                LOG.info("@{} скачал файл {}", userName, filesRepository.findById(Long.valueOf(callBackData)).orElseThrow().getPath().replace("./", "").replace("/", " -> "));
                meterRegistry.counter("download_count", List.of()).increment();

                meterRegistry.gauge(
                        "file",
                        List.of(Tag.of("fileName", directory.getName())),
                        12
                );

                String filePath = filesRepository.findById(Long.valueOf(callBackData)).orElseThrow().getPath();
                filePath = filePath.replace( "/" + filePath.split("/")[filePath.split("/").length - 1], "");
                messages.sendInlineKeyboardMessage(userId, KonspectinnaBotHelper.generateKeyboard(new File(filePath)), filePath);
            }
        }
    }

    public static <T> void executeMessage(T req) throws TelegramApiException {
        instance.execute((BotApiMethod<? extends Serializable>) req);
    }

    public static void executeFile(SendDocument req) throws TelegramApiException {
        instance.execute(req);
    }

    @PostConstruct
    public void init() {
        instance = this;
        DataBase.searchAndSaveFilesOnDB(1344071668L);
    }

    @Override
    public String getBotUsername() {
        return "Konspectinna";
    }

}