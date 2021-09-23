/**
 * Entry point for the NewsFeed Bot in case if the news
 * will be retrieved through telegram bot.
 *
 *
 * @author  Palvan Rozyyev
 * @version 1.0
 * @since   2021-09-17
 */

package com.paro.newsfeed.controller;

import com.paro.newsfeed.service.BotService;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequestMapping("/bot")
public class BotController {
    private final BotService botService;

    public BotController(BotService botService) {
        this.botService = botService;
    }
    @GetMapping("/start")
    public void startBot(){
        log.info("[BotController: startBot]: Request from the TelegramBot has been received");
        botService.getByNews();
    }

}
