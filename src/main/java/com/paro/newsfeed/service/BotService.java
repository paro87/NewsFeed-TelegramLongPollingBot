package com.paro.newsfeed.service;

import com.paro.newsfeed.bot.NewsFeedChannel;
import com.paro.newsfeed.model.News;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class BotService {
    private final NewsFeedChannel newsFeedChannel;
    private final NewsFeedService newsFeedService;

    public BotService(NewsFeedChannel newsFeedChannel, NewsFeedService newsFeedService) {
        this.newsFeedChannel = newsFeedChannel;
        this.newsFeedService = newsFeedService;
    }

    //@Scheduled(initialDelay = 1000)
    public void getAllNews(){
        Map<String, List<News>> allNews = newsFeedService.getAllNews();
        newsFeedChannel.startNewsFeedChannel(allNews);
    }

    @SneakyThrows
    public void getByNews(){
        getTurkmenNews();
    }
    @Scheduled(cron = "0 0 11,19,3 * * *")
//    @Scheduled(cron = "5 * * * * *")
    public void getHronika(){
        log.info("Parsing https://www.hronikatm.com at: {}", LocalDate.now());
        List<News> hronikaNewsList = newsFeedService.getHronikaNews();
        Map<String, List<News>> allNewsList = new HashMap<>();
        allNewsList.put("Хроника Туркменистана", hronikaNewsList);
        newsFeedChannel.startNewsFeedChannel(allNewsList);
    }

    @Scheduled(cron = "0 0 13,21,5 * * *")
//    @Scheduled(cron = "7 * * * * *")
    public void getTurkmenNews(){
        log.info("Parsing https://turkmen.news at: {}", LocalDate.now());
        List<News> turkmenNewsList = newsFeedService.getTurkmenNews();
        Map<String, List<News>> allNewsList = new HashMap<>();
        allNewsList.put("Turkmen.news", turkmenNewsList);
        newsFeedChannel.startNewsFeedChannel(allNewsList);
    }

    @Scheduled(cron = "0 0 15,23,7 * * *")
//    @Scheduled(cron = "9 * * * * *")
    public void getGundogar(){
        log.info("Parsing http://gundogar.org at: {}", LocalDate.now());
        List<News> gundogarNewsList = newsFeedService.getGundogarNews();
        Map<String, List<News>> allNewsList = new HashMap<>();
        allNewsList.put("Гундогар", gundogarNewsList);
        newsFeedChannel.startNewsFeedChannel(allNewsList);
    }

    @Scheduled(cron = "0 0 17,01,9 * * *")
//    @Scheduled(cron = "11 * * * * *")
    public void getRadioFreedom(){
        log.info("Parsing https://www.azathabar.com at: {}", LocalDate.now());
        List<News> radioFreedomNewsList = newsFeedService.getRadioFreedomNews();
        Map<String, List<News>> allNewsList = new HashMap<>();
        allNewsList.put("Радио Азатлык", radioFreedomNewsList);
        newsFeedChannel.startNewsFeedChannel(allNewsList);
    }


}
