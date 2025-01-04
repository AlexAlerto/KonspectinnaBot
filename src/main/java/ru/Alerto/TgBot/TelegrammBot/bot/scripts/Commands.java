package ru.Alerto.TgBot.TelegrammBot.bot.scripts;

import jakarta.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.Alerto.TgBot.DataBase.repo.UserRepository;
import ru.Alerto.TgBot.TelegrammBot.bot.KonspectinnaBot;
import ru.Alerto.TgBot.TelegrammBot.bot.KonspectinnaBotHelper;
import ru.Alerto.TgBot.TelegrammBot.bot.interfaces.CommandsInterface;
import ru.Alerto.TgBot.TelegrammBot.bot.interfaces.MessagesInterface;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@NoArgsConstructor
public class Commands implements CommandsInterface {
    private static Commands instance;

    @Autowired
    private UserRepository userRepository;

    private static final MessagesInterface messages = new Messages();

    @Override
    public void startCommand(Long userId, String userName, int messageId) {
        KonspectinnaBot.LOG.info("@{} нажал на /start", userName);

        File directory = new File("./files/" + instance.userRepository.findByTgId(userId).orElseThrow().getDirection());
        messages.sendInlineKeyboardMessage(userId, KonspectinnaBotHelper.generateKeyboard(directory), directory.getPath());
    }

    @Override
    @SneakyThrows
    public void infoCommand(Long userId, String userName){
        KonspectinnaBot.LOG.info("@{} нажал на /info", userName);
        messages.sendMessage(userId, Files.readString(Paths.get("./info.txt")));
    }

    @PostConstruct
    public void init() {
        instance = this;
    }
}
