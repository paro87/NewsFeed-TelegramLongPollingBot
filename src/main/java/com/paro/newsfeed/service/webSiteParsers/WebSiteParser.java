package com.paro.newsfeed.service.webSiteParsers;

import com.paro.newsfeed.model.Comment;
import com.paro.newsfeed.model.News;
import com.paro.newsfeed.model.CommentNode;
import org.jsoup.nodes.Element;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public interface WebSiteParser {
    String getType();
    Map<String, String> getNews();
    List<News> parseByNews(Map<String, String> mapOfNews);
    ZonedDateTime getPublishedDate(Element doc);
    String getTitle(Element doc);
    String getMainPicture(Element doc);
    List<String> getParagraphs(Element doc);
    List<String> getPictures(Element doc);
//    CommentNode<Comment> getComments(Element doc);
//    CommentNode<Comment> getCallbackComments(String commentsLink);
    List<CommentNode<Comment>> getComments(Element doc);
    List<CommentNode<Comment>> getCallbackComments(String commentsLink);
}
