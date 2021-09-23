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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@Log4j2
@Component
public class GundogarNewsParser implements WebSiteParser{
    private final String type = "GundogarNews";
    @Value("${gundogar.news}")
    private String mainPage;
    @Value("${gundogar.baseURL}")
    private String baseURL;

    private final DocumentProvider documentProvider;

    public GundogarNewsParser(DocumentProvider documentProvider) {
        this.documentProvider = documentProvider;
    }

    @Override
    public String getType() {
        return type;
    }

    public String getBaseURL(){
        return baseURL;
    }

    @Override
    public Map<String, String> getNews() {
        Map<String, String> mapOfNews=new LinkedHashMap<>();
        return mapOfNews;
    }

    @Override
    public List<News> parseByNews(Map<String, String> mapOfNews){
        List<News> newsList=new ArrayList<>();

        // TODO: Save lastNewsDate in different location
        Path path = Path.of("src/main/resources/static/GundogarNewsLastNewsDate");
        // Setting yesterday's date in case of absence of the last news date
        String dateFromFile = LocalDate.now().minusDays(1).toString();
        try{
            dateFromFile = Files.readString(path);
        } catch (IOException e) {
            log.error("[GundogarNewsParser: parseByNews]: Couldn't read the last published news' date from {}", path.toString());
            e.printStackTrace();
        }

        ZonedDateTime savedLastPublishedNewsDate = ZonedDateTime.parse(dateFromFile);
        log.info("[GundogarNewsParser: parseByNews]: Retrieved last published news' date is {}", savedLastPublishedNewsDate.toString());
        ZonedDateTime sessionLatestNewsDate = savedLastPublishedNewsDate;

        String pageLink=mainPage;
        Document doc=documentProvider.getDocument(pageLink);
        log.info("[GundogarNewsParser: parseByNews]: Retrieved document for {}", pageLink);
        Elements items = doc.getElementsByAttributeValue("valign","TOP");
        Element element = items.get(2).getElementById("CONTENT_COLUMN");
        Element table;
        try {
            table = element.select("table").get(0);
        } catch (IndexOutOfBoundsException exception){
            return new ArrayList<>();
        }
        Elements rows = table.select("tr");

        News news = null;

        List<String> paragraphList=null;
        for (Element row : rows) {

            Elements dateElement = row.select("td[style='font-size: 12px; color: #4D4D4D;']");
            Elements titleElement = row.select("td[style='font-size: 12px; color: #000000; font-weight: bold; padding-bottom: 9px']");
            Elements paragraphElement = row.select("td[style='font-size: 12px; color: #000000; font-weight: normal; padding-left: 18px; padding-bottom: 8px; font-family: verdana;']");
            Elements paragraphEndElement = row.select("td[style='font-size: 12px; color: #000000; font-weight: normal; padding-left: 18px; padding-bottom: 13px; font-family: verdana;']");

            if (!dateElement.isEmpty()) {
                ZonedDateTime publishedDate = getPublishedDate(dateElement.get(0));
                if (publishedDate.isAfter(savedLastPublishedNewsDate)) {
                    news = new News();
                    news.setSource(getType());
                    news.setPublishedDate(publishedDate);
                    paragraphList = new ArrayList<>();
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
            } else if (!titleElement.isEmpty()) {
                String newsTitle = getTitle(titleElement.get(0));
                news.setTitle(newsTitle);
            } else if (!paragraphElement.isEmpty()) {
                String paragraph = paragraphElement.get(0).text();
                paragraphList.add(paragraph);
            } else if (!paragraphEndElement.isEmpty()) {
                news.setParagraphs(paragraphList);
                newsList.add(news);
            }



        }
        try {
            Files.writeString(path, sessionLatestNewsDate.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newsList;
    }

    @Override
    public String getTitle(Element element) {
        String title =element.text();
        return title;
    }

    @Override
    public String getMainPicture(Element doc) {
        return "";
    }


    @Override
    public ZonedDateTime getPublishedDate(Element element) {
        String[] date = element.text().split(",")[0].split("\\.");
        String[] time = element.text().split(",")[1].strip().split(":");
        int year = Integer.parseInt(date[2]);
        int month = Integer.parseInt(date[1]);
        int day = Integer.parseInt(date[0]);
        int hour = Integer.parseInt(time[0]);
        int minute = Integer.parseInt(time[1]);
        LocalDate publishedDate = LocalDate.of(year, month, day);
        LocalDateTime publishedDateTime = publishedDate.atTime(hour, minute);
        ZonedDateTime zdt = publishedDateTime.atZone(ZoneOffset.UTC);
        return zdt;
    }

    @Override
    public List<String> getParagraphs(Element doc) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getPictures(Element doc) {
        return new ArrayList<>();
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
