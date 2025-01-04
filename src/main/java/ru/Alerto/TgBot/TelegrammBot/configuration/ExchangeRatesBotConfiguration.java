package ru.Alerto.TgBot.TelegrammBot.configuration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.Alerto.TgBot.TelegrammBot.bot.KonspectinnaBot;

@Configuration
@EnableCaching
@EnableScheduling
public class ExchangeRatesBotConfiguration {

    @Bean
    public TelegramBotsApi telegramBotsApi(KonspectinnaBot konspectinnaBot) throws TelegramApiException {
        var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(konspectinnaBot);
        return api;
    }

}
