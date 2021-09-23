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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Component
public class HronikaTMHronikaParser implements WebSiteParser{
    private final String type = "HronikaTMHronika";
    @Value("${hronikaTM.hronika.mainPage}")
    private String mainPage;
    @Value("${hronikaTM.hronika.nextPage}")
    private String nextPage;



    private final DocumentProvider documentProvider;
    @Autowired
    public HronikaTMHronikaParser(DocumentProvider documentProvider){
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
        Path path = Path.of("src/main/resources/static/HronikaLastNewsDate");
        String dateFromFile = LocalDate.now().minusDays(1).toString();
        try{
            dateFromFile = Files.readString(path);
        } catch (IOException e) {
            log.error("[HronikaTMHronikaParser: getNews]: Couldn't read the last published news' date from {} for {}", path.toString(), type);
            e.printStackTrace();
        }

        ZonedDateTime savedLastPublishedNewsDate = ZonedDateTime.parse(dateFromFile);
        log.info("[HronikaTMHronikaParser: getNews]: Retrieved last published news' date for {} is {}", type, savedLastPublishedNewsDate.toString());
        ZonedDateTime sessionLatestNewsDate = savedLastPublishedNewsDate;



        boolean parseNews = true;
        int i = 1;
        while (parseNews) {
            String pageLink=mainPage;
            if (i > 1){
                pageLink=nextPage+i+"/";
            }
            Document doc=documentProvider.getDocument(pageLink);
            Elements items = doc.getElementsByClass("tdb_module_loop td_module_wrap td-animation-stack");
            items=items.select("div.td-module-meta-info");

            for (Element item : items) {
                String title=item.select("h3.entry-title.td-module-title").select("a").attr("title");
                String link=item.select("h3.entry-title.td-module-title").select("a").attr("href");
                String date=item.select("time.entry-date.updated.td-module-date").attr("datetime");
                DateTimeFormatter formatter=DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                ZonedDateTime publishedDate = ZonedDateTime.parse(date, formatter);
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
        String title =doc.select("article").attr("data-post-title");
        return title;
    }

    @Override
    public String getMainPicture(Element doc) {
        Elements imageElements = doc.select("img.entry-thumb.td-modal-image.lazy");
        String mainPicture=null;
        for (Element imageElement : imageElements) {
            mainPicture=imageElement.attr("data-src");
        }
        return mainPicture;
    }


    @Override
    public ZonedDateTime getPublishedDate(Element doc) {
        doc.select("div.tdb_module_related.td_module_wrap.td-animation-stack").remove();
        Elements metaElements=doc.select("div.tdb-block-inner.td-fix-index time");
        ZonedDateTime publishedDate = null;
        for (Element metaElement : metaElements) {
            String dateTime=metaElement.attr("datetime");
            DateTimeFormatter formatter=DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            publishedDate=ZonedDateTime.parse(dateTime, formatter);
        }
        return publishedDate;
    }

    @Override
    public List<String> getParagraphs(Element doc) {
        List<String> paragraphs=new ArrayList<>();
        doc.select("div.comments").remove();
        Elements elements=doc.select("div.tdb-block-inner.td-fix-index p");

        for (Element element : elements) {
            paragraphs.add(element.text());
        }
        return paragraphs;
    }

    @Override
    public List<String> getPictures(Element doc) {
        List<String> pictureList=new ArrayList<>();
        Elements titlePicture=doc.select("div.tdb-block-inner.td-fix-index a");
        for (Element element : titlePicture) {
            String picture=element.attr("href");
            if(picture.endsWith(".jpg")) {
                pictureList.add(picture);
            }
        }
        return pictureList;
    }

    @Override
    public List<CommentNode<Comment>> getComments(Element doc) {
        CommentNode<Comment> rootCommentNode =new CommentNode<>();
        List<CommentNode<Comment>> commentNodes = parseComments(rootCommentNode, doc, 1);
        return commentNodes;
    }

    private static List<CommentNode<Comment>> parseComments(CommentNode<Comment> rootCommentNode, Element doc, int level){
        String cssQuery = "div.wpd-comment.wpd_comment_level-"+level;

        Elements liComments=doc.select(cssQuery);

        List<CommentNode<Comment>> children= new ArrayList<>();
        if (liComments.isEmpty()) {
            return null;
        }
        for (Element element : liComments) {
            int commentLevel=level;
            CommentNode<Comment> commentNode=new CommentNode<>();
            Comment comment = new Comment();
            comment.setAuthor(Optional.ofNullable(element.select("div.wpd-comment-author ").first()).map(s->s.html()).orElse(""));
            comment.setText(Optional.ofNullable(element.select("div.wpd-comment-text p").first()).map(s->s.html()).orElse(""));
            comment.setDate(Optional.ofNullable(element.select("div[class='wpd-comment-date']").first()).map(s->s.attr("title")).orElse(""));
            comment.setLikeNumber(Optional.ofNullable(element.select("div[class^='wpd-vote-result wpd-vote-result-like']").first()).map(s->s.html()).orElse(""));
            comment.setDislikeNumber(Optional.ofNullable(element.select("div[class^='wpd-vote-result wpd-vote-result-dislike']").first()).map(s->s.html()).orElse(""));
            comment.setReplyToAuthor(Optional.ofNullable(element.select("div[class='wpd-comment-wrap wpd-blog-guest']").first().select("div[class='wpd-reply-to']")).map(s->s.text()+" ").orElse(""));
            comment.setModifiedDate(Optional.ofNullable(element.select("div[class='wpd-comment-last-edited']").first()).map(s->s.html()).orElse(""));
            commentNode.setComment(comment);

            commentLevel += 1;
            Elements nextLevelElements = element.select("div.wpd-comment.wpd_comment_level-" + commentLevel);
            if (nextLevelElements!=null&&!nextLevelElements.isEmpty()) {

                List<CommentNode<Comment>> commentCommentNode = parseComments(commentNode, element, commentLevel);
                commentNode.setChildren(commentCommentNode);
            }
            children.add(commentNode);





        }

        return children;
    }

/*    private static CommentNode<Comment> oldParseComments(CommentNode<Comment> rootCommentNode, Elements levelElements, int level){
        for (Element element : levelElements) {
            int commentLevel=level;
            CommentNode<Comment> commentNode=new CommentNode<>();
            Comment comment = new Comment();
//            comment.setAuthor(element.select("div.wpd-comment-author ").first().html());
            comment.setAuthor(Optional.ofNullable(element.select("div.wpd-comment-author ").first()).map(s->s.html()).orElse(""));
//            comment.setText(element.select("div.wpd-comment-text p").first().html());
            comment.setText(Optional.ofNullable(element.select("div.wpd-comment-text p").first()).map(s->s.html()).orElse(""));
//            comment.setDate(element.select("div[class='wpd-comment-date']").first().attr("title"));
            comment.setDate(Optional.ofNullable(element.select("div[class='wpd-comment-date']").first()).map(s->s.attr("title")).orElse(""));
//            comment.setLikeNumber(Optional.ofNullable(element.select("div[class='wpd-vote-result wpd-vote-result-like wpd-up']").first()).map(s->s.html()).orElse(""));
            comment.setLikeNumber(Optional.ofNullable(element.select("div[class^='wpd-vote-result wpd-vote-result-like']").first()).map(s->s.html()).orElse(""));
//            comment.setDislikeNumber(Optional.ofNullable(element.select("div[class='wpd-vote-result wpd-vote-result-dislike wpd-down']").first()).map(s->s.html()).orElse(""));
            comment.setDislikeNumber(Optional.ofNullable(element.select("div[class^='wpd-vote-result wpd-vote-result-dislike']").first()).map(s->s.html()).orElse(""));
            comment.setReplyToAuthor(Optional.ofNullable(element.select("div[class='wpd-comment-wrap wpd-blog-guest']").first().select("div[class='wpd-reply-to']")).map(s->s.text()+" ").orElse(""));
            comment.setModifiedDate(Optional.ofNullable(element.select("div[class='wpd-comment-last-edited']").first()).map(s->s.html()).orElse(""));
            commentNode.setComment(comment);
            commentLevel += 1;
            Elements nextLevelElements = element.select("div.wpd-comment.wpd_comment_level-" + commentLevel);
            if (!nextLevelElements.isEmpty()) {
                parseComments(commentNode, nextLevelElements, commentLevel);
            }else {
                commentLevel--;
            }
            commentNode.setParent(rootCommentNode);
        }
        level--;
        return rootCommentNode;
    }*/

    @Override
    public List<CommentNode<Comment>> getCallbackComments(String commentsLink) {
        Document doc = documentProvider.getDocument(commentsLink);
        List<CommentNode<Comment>> comments = getComments(doc);
        return comments;
    }


}
