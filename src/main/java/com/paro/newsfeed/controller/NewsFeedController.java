/**
 * Entry point for the NewsFeed in case if the news
 * will be retrieved through API.
 *
 *
 * @author  Palvan Rozyyev
 * @version 1.0
 * @since   2021-09-17
 */

package com.paro.newsfeed.controller;

import com.paro.newsfeed.model.News;
import com.paro.newsfeed.service.NewsFeedService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/newsfeed")
public class NewsFeedController {

    private final NewsFeedService newsFeedService;
    @Autowired
    public NewsFeedController(NewsFeedService newsFeedService) {
        this.newsFeedService = newsFeedService;
    }

    @GetMapping("/allNews")
    public Map<String, List<News>> getAllNews() {
        log.info("[NewsFeedController: getAllNews]: Request to retrieve all the news has been received");
        Map<String, List<News>> newsList = newsFeedService.getAllNews();
        return Collections.unmodifiableMap(newsList);
    }

    @GetMapping("/hronikaNews")
    public List<News> getHronikaNews(){
        log.info("[NewsFeedController: getHronikaNews]: Request to retrieve the news from the HronikaTM has been received");
        List<News> hronikaNewsList = newsFeedService.getHronikaNews();
        return Collections.unmodifiableList(hronikaNewsList);
    }

    @GetMapping("/turkmenNews")
    public List<News> getTurkmenNews(){
        log.info("[NewsFeedController: getHronikaNews]: Request to retrieve the news from the TurkmenNews has been received");
        List<News> turkmenNewsList = newsFeedService.getTurkmenNews();
        return Collections.unmodifiableList(turkmenNewsList);
    }

    @GetMapping("/gundogarNews")
    public List<News> getGundogarNews(){
        log.info("[NewsFeedController: getGundogarNews]: Request to retrieve the news from the GundogarNews has been received");
        List<News> gundogarNewsList = newsFeedService.getGundogarNews();
        return Collections.unmodifiableList(gundogarNewsList);
    }

    @GetMapping("/radioFreedom")
    public List<News> getRadioFreedom(){
        log.info("[NewsFeedController: getRadioFreedom]: Request to retrieve the news from the RadioFreedom has been received");
        List<News> radioFreedomNewsList = newsFeedService.getRadioFreedomNews();
        return Collections.unmodifiableList(radioFreedomNewsList);
    }
}
