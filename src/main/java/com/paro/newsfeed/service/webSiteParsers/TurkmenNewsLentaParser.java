package com.paro.newsfeed.service.webSiteParsers;

import com.paro.newsfeed.model.Comment;
import com.paro.newsfeed.model.CommentNode;
import com.paro.newsfeed.model.News;
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
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Component
public class TurkmenNewsLentaParser implements WebSiteParser{
    private final String type = "TurkmenNewsLenta";
    @Value("${turkmenNews.lenta.mainPage}")
    private String mainPage;
    @Value("${turkmenNews.lenta.nextPage}")
    private String nextPage;

    private final DocumentProvider documentProvider;
    @Autowired
    public TurkmenNewsLentaParser(DocumentProvider documentProvider){
        this.documentProvider = documentProvider;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Map<String, String> getNews() {
        Map<String, String> mapOfNews=new LinkedHashMap<>();
        String dateFromFile = getLastNewsDateFromFile();
        ZonedDateTime savedLastPublishedNewsDate = ZonedDateTime.parse(dateFromFile);
        log.info("[TurkmenNewsLentaParser: getNews]: Retrieved last published news' date for {} is {}", type, savedLastPublishedNewsDate.toString());
        ZonedDateTime thisSessionsLatestNewsDate = savedLastPublishedNewsDate;

        boolean parseNews = true;
        int i = 1;
        while (parseNews) {

            String pageLink=mainPage;
            if (i > 1){
                pageLink=nextPage+i+"/";
            }
            Document doc=documentProvider.getDocument(pageLink);

            Elements items = doc.select("article[id^='post-']");
            if (items.isEmpty())
                return mapOfNews;
            for (Element item : items) {
                String title = item.select("h2[class='entry-title']").select("a").text();
                String link = item.select("h2[class='entry-title']").select("a").attr("href");
                String date = item.select("p[class='post-meta']").select("span[class='published']").text();
                ZonedDateTime publishedDate = formatAndGetDate(date);
                if (publishedDate.isAfter(savedLastPublishedNewsDate)) {
                    mapOfNews.put(title, link);
                    if (publishedDate.isAfter(thisSessionsLatestNewsDate)){
                        thisSessionsLatestNewsDate =publishedDate;
                    }
                }else {
                    writeLastNewsDateToFile(thisSessionsLatestNewsDate);
                    parseNews = false;
                    break;
                }

            }
            i++;
        }
        return mapOfNews;
    }

    private String getLastNewsDateFromFile(){
        Path path = Path.of("src/main/resources/static/TurkmenNewsLentaLastNewsDate");
        String dateFromFile = LocalDate.now().minusDays(1).toString();
        try{
            dateFromFile = Files.readString(path);
        } catch (IOException e) {
            log.error("[TurkmenNewsLentaParser: getgetLastNewsDateFromFileNews]: Couldn't read the last published news' date from {} for {}", path.toString(), type);
            e.printStackTrace();
        }
        return dateFromFile;
    }

    private void writeLastNewsDateToFile(ZonedDateTime sessionLatestNewsDate){
        Path path = Path.of("src/main/resources/static/TurkmenNewsLentaLastNewsDate");
        String dateToBeSaved = sessionLatestNewsDate.toString();
        try {
            Files.writeString(path, dateToBeSaved);
            log.info("[TurkmenNewsLentaParser: writeLastNewsDateToFile]: Date has been saved: {}",dateToBeSaved);
        } catch (IOException e) {
            log.error("[TurkmenNewsLentaParser: writeLastNewsDateToFile]: Date couldn't be saved: {}", dateToBeSaved);
            e.printStackTrace();
        }
    }

    private ZonedDateTime formatAndGetDate(String date) {
        //Argument date will be in "19.Сен.2021" format.
        String test = date.replace("."," ");

        //DateTimeFormatter solution
        Map<Long, String> lookup = new HashMap<>();
        lookup.put(1L, "Янв");
        lookup.put(2L, "Фев");
        lookup.put(3L, "Мар");
        lookup.put(4L, "Апр");
        lookup.put(5L, "Май");
        lookup.put(6L, "Июн");
        lookup.put(7L, "Июл");
        lookup.put(8L, "Авг");
        lookup.put(9L, "Сен");
        lookup.put(10L, "Окт");
        lookup.put(11L, "Ноя");
        lookup.put(12L, "Дек");

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("dd ")
                .appendText(ChronoField.MONTH_OF_YEAR, lookup)
                .appendPattern(" yyyy")
                .toFormatter();
        LocalDate localDate = LocalDate.parse(test,formatter);


        //SimpleDateFormat solution
        /*String[] months = {"Янв", "Фев", "Мар", "Апр", "Мая", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"};
        Locale ru = new Locale("ru");
        DateFormatSymbols symbols = DateFormatSymbols.getInstance(ru);
        symbols.setMonths(months);
        SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy", ru);
        format.setDateFormatSymbols(symbols);
        try {
            LocalDate localDate = LocalDate.ofInstant(format.parse(test).toInstant(), ZoneId.systemDefault());
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        return localDate.atStartOfDay(ZoneOffset.UTC);
    }

    @Override
    public List<News> parseByNews(Map<String, String> mapOfNews){
        List<News> newsList=new ArrayList<>();
        if (mapOfNews.isEmpty())
            return newsList;
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
        String title =doc.select("h1[class='entry-title']").text();
        return title;
    }

    @Override
    public String getMainPicture(Element doc) {
        String mainPicture = doc.select("div[id='single-post-custom']").select("img").first().attr("src");
        return mainPicture;
    }

    @Override
    public ZonedDateTime getPublishedDate(Element doc) {
        String date=doc.select("div[class='et_pb_title_container']").select("span[class='published']").text();
        ZonedDateTime zdt = formatAndGetDate(date);
        return zdt;
    }

    @Override
    public List<String> getParagraphs(Element doc) {
        List<String> paragraphs=new ArrayList<>();
        Elements elements=doc.select("div[id='single-post-custom']");
        Elements elementsph3 = elements.select("p,h3");
        for (Element element : elementsph3) {
            String paragraph = element.text();
            if (!paragraph.isEmpty())
                paragraphs.add(paragraph);
        }
        return paragraphs;
    }

    @Override
    public List<String> getPictures(Element doc) {
        Elements pictureElements = doc.select("div[id='single-post-custom']").select("img");
        List<String> pictures = new ArrayList<>();
        if (!pictureElements.isEmpty()){
            for (Element pictureElement : pictureElements) {
                String pic = pictureElement.attr("src");
                pictures.add(pic);
            }
        }
        if (pictures.size()>0)
            pictures.remove(0);
        return pictures;
    }

    @Override
    public List<CommentNode<Comment>> getComments(Element doc) {
        CommentNode<Comment> rootCommentNode =new CommentNode<>();
        List<CommentNode<Comment>> commentNodes = parseComments(rootCommentNode, doc, 1);
        return commentNodes;
    }

    private static List<CommentNode<Comment>> parseComments(CommentNode<Comment> rootCommentNode, Element doc, int level){
        String cssQuery = "li[class$='depth-" + level + " et-pb-non-builder-comment']";
        Elements liComments=doc.select(cssQuery);
        List<CommentNode<Comment>> children= new ArrayList<>();
        if (liComments.isEmpty()) {
            return children;
        }
        for (Element liComment : liComments) {
            int commentLevel=level;
            CommentNode<Comment> commentNode=new CommentNode<>();
            Comment comment = new Comment();
            Element onlyCommentWithoutSubComments= liComment.selectFirst("article[class='comment-body clearfix']");

            comment.setAuthor(Optional.ofNullable(onlyCommentWithoutSubComments.select("span[class='fn']").first()).map(s->s.html()).orElse(""));
            comment.setText(Optional.ofNullable(onlyCommentWithoutSubComments.select("div[class='comment_area']").first().select("p")).map(s->s.html().replace("<br>","")).orElse(""));
            comment.setDate(Optional.ofNullable(onlyCommentWithoutSubComments.select("span[class='comment_date']").first()).map(s->s.html()).orElse(""));
            comment.setLikeNumber(Optional.ofNullable(onlyCommentWithoutSubComments.select("div[class='cld-like-wrap  cld-common-wrap']").select("span[class='cld-like-count-wrap cld-count-wrap']").first()).map(s->s.text()).orElse(""));
            comment.setDislikeNumber(Optional.ofNullable(onlyCommentWithoutSubComments.select("div[class='cld-dislike-wrap  cld-common-wrap']").select("span[class='cld-dislike-count-wrap cld-count-wrap']").first()).map(s->s.text()).orElse(""));

            if (rootCommentNode.getComment()!=null){
                comment.setReplyToAuthor("Ответ для "+rootCommentNode.getComment().getAuthor());
            }

            commentNode.setComment(comment);


            Element subcomments = liComment.selectFirst("ul[class='children']");
            if (subcomments!=null) {
                commentLevel += 1;
                List<CommentNode<Comment>> commentCommentNode = parseComments(commentNode, subcomments, commentLevel);
                commentNode.setChildren(commentCommentNode);

            }
            children.add(commentNode);
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
