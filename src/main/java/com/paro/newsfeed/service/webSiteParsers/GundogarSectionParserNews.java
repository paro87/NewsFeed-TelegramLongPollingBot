package com.paro.newsfeed.service.webSiteParsers;

import com.paro.newsfeed.model.Comment;
import com.paro.newsfeed.model.News;
import com.paro.newsfeed.model.CommentNode;
import com.paro.newsfeed.service.utilityClasses.DocumentProvider;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Component
public class GundogarSectionParserNews implements WebSiteParser{
    private final String type = "GundogarSections";
    @Value("#{${gundogarSection}}")
    private Map<String, String> mapOfNewsLinks;

    private final DocumentProvider documentProvider;

    public GundogarSectionParserNews(DocumentProvider documentProvider) {
        this.documentProvider = documentProvider;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Map<String, String> getNews() {
        Map<String, String> mapOfNews=new LinkedHashMap<>();

        mapOfNewsLinks.forEach((key, value)->{
            Path path = Path.of("src/main/resources/static/Gundogar"+key+"LastNewsDate");
            // Setting yesterday's date in case of absence of the last news date
            String dateFromFile = LocalDate.now().minusDays(1).toString();
            try{
                dateFromFile = Files.readString(path);
            } catch (IOException e) {
                log.error("[GundogarSectionParserNews: getNews]: Couldn't read the last published news' date from {}", path.toString());
                e.printStackTrace();
            }

            ZonedDateTime savedLastPublishedNewsDate = ZonedDateTime.parse(dateFromFile);
            log.info("[GundogarSectionParserNews: getNews]: Retrieved last published news' date is {}", savedLastPublishedNewsDate.toString());
            ZonedDateTime sessionLatestNewsDate = savedLastPublishedNewsDate;

            String pageLink=value;
            Document doc=documentProvider.getDocument(pageLink);
            log.info("[GundogarSectionParserNews: getNews]: Retrieved document for {}", type, pageLink);
            Elements items = doc.select("td[id='ANONCES_COLUMN']");
            items=items.select("table[cellpadding='0']");

            for (Element item : items) {
                Elements blank = item.select("td[style='background: url(/images/hr.gif)']");
                if (!blank.isEmpty())
                    continue;
                String title = item.select("td[style='font-size: 14px; color: #000000; font-family: arial; font-weight: bold; padding-top: 9px; padding-bottom: 12px;']").text();
                String link="http://gundogar.org"+item.select("a").attr("href");
                String date=item.select("td[style='font-size: 8px; color: #A6A8AB;']").text();
                String[] split = date.split("\\.");
                int year = Integer.parseInt(split[2]);
                int month = Integer.parseInt(split[1]);
                int day = Integer.parseInt(split[0]);
                LocalDate localDate = LocalDate.of(year, month, day);
                ZonedDateTime publishedDate = localDate.atStartOfDay(ZoneOffset.UTC);
                if (publishedDate.isAfter(savedLastPublishedNewsDate)) {
                    mapOfNews.put(title, link);
                    if (publishedDate.isAfter(sessionLatestNewsDate)){
                        sessionLatestNewsDate=publishedDate;
                    }
                }else {
                    try {
                        Files.writeString(path, sessionLatestNewsDate.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                }

            }
            try {
                Files.writeString(path, sessionLatestNewsDate.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        return mapOfNews;
    }


    @Override
    public List<News> parseByNews(Map<String, String> mapOfNews){
        List<News> newsList=new ArrayList<>();
        mapOfNews.forEach((k,v)->{
            News news=new News();
            Document doc = documentProvider.getDocument(v);
            CompletableFuture.supplyAsync(()->getTitle(doc)).thenAccept(s -> news.setTitle(s));
            CompletableFuture.supplyAsync(()->getMainPicture(doc)).thenAccept(s -> news.setMainPicture(s));
            CompletableFuture.supplyAsync(()->getPublishedDate(doc)).thenAccept(s -> news.setPublishedDate(s));
            CompletableFuture.supplyAsync(()->getPictures(doc)).thenAccept(s -> news.setPictures(s));
            CompletableFuture.supplyAsync(()->getComments(doc)).thenAccept(s -> news.setComments(s));
            CompletableFuture.supplyAsync(()->getParagraphs(doc)).thenAccept(s -> news.setParagraphs(s));
            CompletableFuture.supplyAsync(()->news.setLink(v));
            news.setSource(getType());
            newsList.add(news);
        });
        return newsList;
    }

    @Override
    public String getTitle(Element doc) {
        String title = doc.select("title").text().replace("Гундогар :: ","");
        return title;
    }

    @Override
    public String getMainPicture(Element doc) {
        String mainPicture = doc.select("img[alt='Crude Accountability']").attr("src");
        return "http://gundogar.org"+mainPicture;
    }


    @Override
    public ZonedDateTime getPublishedDate(Element doc) {
        String date = doc.select("td[style='font-size: 11px; color: #A6A8AB; padding-top: 10px; padding-bottom: 10px;']").text();
        String[] split = date.split("\\.");
        int year = Integer.parseInt(split[2]);
        int month = Integer.parseInt(split[1]);
        int day = Integer.parseInt(split[0]);
        LocalDate localDate = LocalDate.of(year, month, day);
        ZonedDateTime publishedDate = localDate.atStartOfDay(ZoneOffset.UTC);
        return publishedDate;
    }

    public String getAuthor(Element doc){
        String author = doc.select("p[style='color: #808184; font-size: 16px; font-weight: normal; padding-bottom: 16px;']").text();
        return author;
    }

    @Override
    public List<String> getParagraphs(Element doc) {

        List<String> paragraphs=new ArrayList<>();
        Elements el = doc.select("td[style='padding-bottom: 18px;']");
        el.select("p[style='font-size: 18px; color: #000000; font-family: arial; font-weight: bold; padding-top: 9px; padding-bottom: 42px; padding-left: 0px;']").remove();
        el.select("p[style='color: #808184; font-size: 16px; font-weight: normal; padding-bottom: 16px;']").remove();
        el.select("p[style='font-size: 12px; color: #000000; font-weight: bold; margin-top: 0px;']").remove();
        el.select("p[style='font-size: 13px;']");
        Elements elements=el.select("p");

        for (Element element : elements) {
            if (!element.text().isEmpty())
                paragraphs.add(element.text());
        }
        return paragraphs;
    }

    @Override
    public List<String> getPictures(Element doc) {
        List<String> pictureList=new ArrayList<>();
        return pictureList;
    }

    @Override
    public List<CommentNode<Comment>> getComments(Element doc) {
        return new ArrayList<>();
    }

    @Override
    public List<CommentNode<Comment>> getCallbackComments(String commentsLink) {
        return new ArrayList<>();
    }
}
