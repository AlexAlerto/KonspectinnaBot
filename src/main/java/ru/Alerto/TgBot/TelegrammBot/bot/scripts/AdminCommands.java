package ru.Alerto.TgBot.TelegrammBot.bot.scripts;

import jakarta.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.Alerto.TgBot.DataBase.DataBase;
import ru.Alerto.TgBot.DataBase.repo.UserRepository;
import ru.Alerto.TgBot.Libs.ExtendVariables;
import ru.Alerto.TgBot.TelegrammBot.bot.interfaces.MessagesInterface;

@Component
@NoArgsConstructor
public class AdminCommands {
    private static AdminCommands instance;

    @Autowired
    private UserRepository userRepository;

    private static final MessagesInterface messages = new Messages();

    public void Command(Long userId, String request) throws TelegramApiException {
        if (userId != 1344071668L && userId != 1104443126L) return;
        switch (request.split(" ")[0]){
            case "/qq" -> QQCommand(request);
            case "/stop" -> System.exit(0);
            case "/reload" -> DataBase.searchAndSaveFilesOnDB();
        }
    }

    private void QQCommand(String request) throws TelegramApiException {
        String command = request.split(" ")[1];
        String message =  request.replace("/qq" + " " + request.split(" ")[1], "");

        if (ExtendVariables.StrIsLong(command)) {
            messages.sendMessage(Long.parseLong(command), message);
        }
        else if (command.equals("all")){
            instance.userRepository.findAll().forEach(
                    user -> {
                        try {
                            messages.sendMessage(user.getTgId(), message);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }
        else {
            instance.userRepository.findAllByDirection(command).forEach(
                    user -> {
                        try {
                            messages.sendMessage(user.getTgId(), message);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }
    }

    @PostConstruct
    public void init() {
        instance = this;
    }
}
