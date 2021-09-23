/**
 * This class defines the NewsFeedBot by extending from a TelegramLongPollingBot.
 * It receives updates from callback buttons for comments
 * and sends the message to the appropriate chat of TelegramBot.
 *
 *
 * @author  Palvan Rozyyev
 * @version 1.0
 * @since   2021-09-17
 */
package com.paro.newsfeed.bot;

import com.paro.newsfeed.service.CommentService;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Setter
@Log4j2
@Component
public class NewsFeedBot extends TelegramLongPollingBot {
    @Value("${telegrambot.botUsername}")
    private String botUsername;
    @Value("${telegrambot.botToken}")
    private String botToken;
    @Value("${telegrambot.chatId}")
    private int chatId;

    private final CommentService commentService;

    public NewsFeedBot(CommentService commentService) {
        this.commentService = commentService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("[NewsFeedBot: onUpdateReceived]: Received new update with id: {}", update.getUpdateId());
        SendMessage message = null;
        if (update.hasCallbackQuery()) {
            message = commentService.handleComments(update);
        }
        
        log.info("[NewsFeedBot: onUpdateReceived]: Sending comments content to chat: {}", chatId);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}

