package com.paro.newsfeed.model;

import lombok.Data;

@Data
public class Comment {
    private String author;
    private String text;
    private String replyToAuthor;
    private String date;
    private String modifiedDate;
    private String likeNumber;
    private String dislikeNumber;
}
