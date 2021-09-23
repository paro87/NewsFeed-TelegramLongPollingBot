
package com.paro.newsfeed.config;

import com.paro.newsfeed.bot.NewsFeedBot;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
@Log4j2
public class BotRegister {
    private final NewsFeedBot newsFeedBot;

    public BotRegister(NewsFeedBot telegrambot) {
        this.newsFeedBot = telegrambot;
        try {
            TelegramBotsApi botApi = new TelegramBotsApi(DefaultBotSession.class);
            botApi.registerBot(newsFeedBot);
            log.info("NewsFeedBot registered");
        } catch (TelegramApiException e) {
            log.error("Couldn't register the NewsFeedBot");
            e.printStackTrace();
        }
    }

}

