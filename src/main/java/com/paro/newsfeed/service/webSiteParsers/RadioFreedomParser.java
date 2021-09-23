package com.paro.newsfeed.service.webSiteParsers;

import com.paro.newsfeed.model.Comment;
import com.paro.newsfeed.model.News;
import com.paro.newsfeed.model.CommentNode;
import com.paro.newsfeed.service.utilityClasses.DocumentProvider;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Component
public class RadioFreedomParser implements WebSiteParser{
    private final String type = "RadioFreedom";
    @Value("${radiofreedom.mainPage}")
    private String mainPage;

    private final DocumentProvider documentProvider;
    @Autowired
    public RadioFreedomParser(DocumentProvider documentProvider){
        this.documentProvider = documentProvider;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Map<String, String> getNews() {
        Map<String, String> mapOfNews=new LinkedHashMap<>();
        // Could be saved in different place
        Path path = Path.of("src/main/resources/static/RadioFreedomLastNewsDate");
        String dateFromFile = LocalDate.now().minusDays(1).toString();
        try{
            dateFromFile = Files.readString(path);
        } catch (IOException e) {
            log.error("[RadioFreedomParser: getNews]: Couldn't read the last published news' date from {} for {}", path.toString(), type);
            e.printStackTrace();
        }

        ZonedDateTime savedLastPublishedNewsDate = ZonedDateTime.parse(dateFromFile);
        log.info("[RadioFreedomParser: getNews]: Retrieved last published news' date for {} is {}", type, savedLastPublishedNewsDate.toString());
        ZonedDateTime sessionLatestNewsDate = savedLastPublishedNewsDate;

        boolean parseNews = true;
        int i = 0;
        while (parseNews) {
            String webLinkFreedomRadio=mainPage+"?p="+i;
            Document doc=documentProvider.getDocument(webLinkFreedomRadio);
            doc.select("div[id='wrowblock-7657_21']").remove();
            Elements items = doc.getElementsByAttributeValueStarting("class","media-block__content media-block__content--h");
            if (items.size()==0)
                return mapOfNews;
            for (Element item : items) {
                String title=item.select("h4").attr("title");
                String link="https://rus.azathabar.com"+item.select("a").attr("href");
                String date=item.getElementsByAttributeValueStarting("class","date").text();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM uuuu").withLocale( new Locale("ru") );
                ZonedDateTime publishedDate = LocalDate.parse(date, dateTimeFormatter).atStartOfDay(ZoneOffset.UTC);
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
                    parseNews = false;
                    break;
                }

            }
            i++;
        }
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
        String title =doc.select("h1[class='title pg-title']").text();
        return title;
    }

    @Override
    public String getMainPicture(Element doc) {
        String mainPicture = doc.select("div[class='img-wrap']").select("img").attr("src");
        mainPicture = mainPicture.replace("w250", "w1023");
        return mainPicture;
    }


    @Override
    public ZonedDateTime getPublishedDate(Element doc) {
        String dateTime1=doc.select("span[class='date']").select("time").attr("datetime");
        ZonedDateTime lastSavedNewsDateHronika = ZonedDateTime.parse(dateTime1);
        return lastSavedNewsDateHronika;
    }

    @Override
    public List<String> getParagraphs(Element doc) {
        List<String> paragraphs=new ArrayList<>();
        Elements elements=doc.select("div[class='wsw']").select("p");
        for (Element element : elements) {
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
        String commentURL = "https://rus.azathabar.com"+doc.select("a[class='dropdown__item-link link-ajax']").attr("href");
        Document document = documentProvider.getDocument(commentURL);
        Elements commentElements=document.select("ul[class='comments__list']").select("div[class='comment comment--pangea']");
        List<CommentNode<Comment>> children= new ArrayList<>();
        CommentNode<Comment> comments =new CommentNode<>();
        if (commentElements!=null) {
            for (Element element : commentElements) {
                CommentNode<Comment> commentNode = new CommentNode<>();
                Comment comment = new Comment();
                comment.setAuthor(Optional.ofNullable(element.select("span[class='user']")).map(s->s.text()).orElse(""));
                comment.setText(Optional.ofNullable(element.select("div[class='comment__content']").select("p").first()).map(s->s.text()).orElse(""));
                comment.setDate(Optional.ofNullable(element.select("span[class='date']")).map(s->s.text()).orElse(""));
                comment.setLikeNumber("");
                comment.setDislikeNumber("");
                comment.setReplyToAuthor("");
                comment.setModifiedDate("");
                commentNode.setComment(comment);
                children.add(commentNode);
            }
        }
        return children;
    }

    @Override
    public List<CommentNode<Comment>> getCallbackComments(String commentsLink) {
        Document doc = documentProvider.getDocument(commentsLink);
        List<CommentNode<Comment>> comments = getComments(doc);
        return comments;
    }




}
