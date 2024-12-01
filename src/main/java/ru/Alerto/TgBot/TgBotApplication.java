package ru.Alerto.TgBot;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.Alerto.TgBot.TelegrammBot.bot.KonspectinnaBot;

@SpringBootApplication
public class TgBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(TgBotApplication.class, args);
		KonspectinnaBot.LOG.warn("Бот запущен");
	}

	@Bean
	MeterBinder meterBinder(){
		return registry -> {
			Counter.builder("greetings_count")
					.description("Количество обращений")
					.register(registry);
		};
	}

}
