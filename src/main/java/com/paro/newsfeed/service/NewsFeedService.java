package com.paro.newsfeed.service;

import com.paro.newsfeed.model.Comment;
import com.paro.newsfeed.model.News;
import com.paro.newsfeed.model.CommentNode;
import com.paro.newsfeed.service.webSiteParsers.WebSiteParser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NewsFeedService {

    private Map<String, WebSiteParser> webSiteParserMap;

    public NewsFeedService(List<WebSiteParser> webSiteParserList){
        webSiteParserMap = webSiteParserList.stream().collect(Collectors.toMap(WebSiteParser::getType, Function.identity()));
    }

    public Map<String, List<News>> getAllNews() {
        List<News> hronikaNewsList = getHronikaNews();
        List<News> turkmenNewsList = getTurkmenNews();
        List<News> gundogarNewsList = getGundogarNews();
        List<News> radioFreedomNewsList = getRadioFreedomNews();
        Map<String, List<News>> allNewsList = new HashMap<>();

        allNewsList.put("Хроника Туркменистана", hronikaNewsList);
        allNewsList.put("Turkmen.news", turkmenNewsList);
        allNewsList.put("Гундогар", gundogarNewsList);
        allNewsList.put("Радио Азатлык", radioFreedomNewsList);
        return allNewsList;
    }

    public List<News> getHronikaNews() {
        List<WebSiteParser> webSiteParsers = new ArrayList<>();
        WebSiteParser hronikaTmHronika=webSiteParserMap.get("HronikaTMHronika");
        WebSiteParser hronikaTMLenta = webSiteParserMap.get("HronikaTMLenta");
        webSiteParsers.add(hronikaTmHronika);
        webSiteParsers.add(hronikaTMLenta);

        List<News> newsList = new ArrayList<>();
        for (WebSiteParser webSiteParser : webSiteParsers) {
            Map<String, String> newsMap=webSiteParser.getNews();
            List<News> news = webSiteParser.parseByNews(newsMap);
            newsList.addAll(news);
        }
        return newsList;
    }

    public List<News> getTurkmenNews() {
        List<WebSiteParser> webSiteParsers = new ArrayList<>();
        WebSiteParser turkmenNews=webSiteParserMap.get("TurkmenNews");
        WebSiteParser turkmenNewsLenta = webSiteParserMap.get("TurkmenNewsLenta");
        webSiteParsers.add(turkmenNews);
        webSiteParsers.add(turkmenNewsLenta);

        List<News> newsList = new ArrayList<>();
        for (WebSiteParser webSiteParser : webSiteParsers) {
            Map<String, String> newsMap=webSiteParser.getNews();
            List<News> news = webSiteParser.parseByNews(newsMap);
            newsList.addAll(news);
        }
        return newsList;

    }

    public List<News> getGundogarNews() {
        List<WebSiteParser> webSiteParsers = new ArrayList<>();
        WebSiteParser gundogarNews=webSiteParserMap.get("GundogarNews");
        WebSiteParser gundogarSections = webSiteParserMap.get("GundogarSections");
        webSiteParsers.add(gundogarNews);
        webSiteParsers.add(gundogarSections);

        List<News> newsList = new ArrayList<>();
        for (WebSiteParser webSiteParser : webSiteParsers) {
            Map<String, String> newsMap=webSiteParser.getNews();
            List<News> news = webSiteParser.parseByNews(newsMap);
            newsList.addAll(news);
        }
        return newsList;
    }

    public List<News> getRadioFreedomNews() {
        WebSiteParser radioFreedomNews =webSiteParserMap.get("RadioFreedom");
        Map<String, String> newsMap= radioFreedomNews.getNews();
        List<News> news = radioFreedomNews.parseByNews(newsMap);
        return news;
    }

    public List<CommentNode<Comment>> getComments(String parser, String commentsLink) {
        WebSiteParser webSiteParser = webSiteParserMap.get(parser);
        List<CommentNode<Comment>> callbackComments = webSiteParser.getCallbackComments(commentsLink);
        return callbackComments;
    }
}
