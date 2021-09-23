package com.paro.newsfeed.model;

import java.time.ZonedDateTime;
import java.util.List;

public class News {
    private String source;
    private String title;
    private String link;
    private ZonedDateTime publishedDate;
    private String mainPicture;
    private List<String> pictures;
    private List<String> paragraphs;
    private List<CommentNode<Comment>> comments;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public News setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getLink() {
        return link;
    }

    public News setLink(String link) {
        this.link = link;
        return this;
    }

    public ZonedDateTime getPublishedDate() {
        return publishedDate;
    }

    public News setPublishedDate(ZonedDateTime publishedDate) {
        this.publishedDate = publishedDate;
        return this;
    }

    public String getMainPicture() {
        return mainPicture;
    }

    public News setMainPicture(String mainPicture) {
        this.mainPicture = mainPicture;
        return this;
    }

    public List<String> getPictures() {
        return pictures;
    }

    public News setPictures(List<String> pictures) {
        this.pictures = pictures;
        return this;
    }

    public List<String> getParagraphs() {
        return paragraphs;
    }

    public News setParagraphs(List<String> paragraphs) {
        this.paragraphs = paragraphs;
        return this;
    }

    public List<CommentNode<Comment>> getComments(){
        return comments;
    }

    public News setComments(List<CommentNode<Comment>> comments){
        this.comments=comments;
        return this;
    }

    @Override
    public String toString() {
        return "News{" +
                "title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", publishedDate=" + publishedDate +
                ", mainPicture='" + mainPicture + '\'' +
                ", pictures=" + pictures +
                ", paragraphs=" + paragraphs +
                '}';
    }
}
