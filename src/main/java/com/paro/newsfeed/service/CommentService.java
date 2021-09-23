package com.paro.newsfeed.service;

import com.paro.newsfeed.model.Comment;
import com.paro.newsfeed.model.CommentNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegraph.ExecutorOptions;
import org.telegram.telegraph.TelegraphContext;
import org.telegram.telegraph.TelegraphContextInitializer;
import org.telegram.telegraph.api.methods.CreateAccount;
import org.telegram.telegraph.api.methods.CreatePage;
import org.telegram.telegraph.api.objects.*;
import org.telegram.telegraph.exceptions.TelegraphException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Log4j2
public class CommentService {
    @Value("${telegrambot.chatId}")
    private int chatId;


    private final NewsFeedService newsFeedService;

    public CommentService(NewsFeedService newsFeedService) {
        this.newsFeedService = newsFeedService;
    }

    public SendMessage handleComments(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        log.info("CallbackQuery received from name:{}, username: {}, userId: {} with data: {}",
                callbackQuery.getFrom().getFirstName()+" "+callbackQuery.getFrom().getLastName(),
                callbackQuery.getFrom().getUserName(),
                callbackQuery.getFrom().getId(),
                callbackQuery.getData());
        String parser = callbackQuery.getData().split(" ")[0];
        String commentsLink = callbackQuery.getData().split(" ")[1];
        //Optional
        List<CommentNode<Comment>> comments = newsFeedService.getComments(parser, commentsLink);
        return processComments(comments, parser, commentsLink);
    }

    private SendMessage processComments(List<CommentNode<Comment>> comments, String parser, String commentsLink) {
        TelegraphContextInitializer.init();
        TelegraphContext.registerInstance(ExecutorOptions.class, new ExecutorOptions());
        List<CommentNode<Comment>> commentNodeList = comments;

        Account account = null;
        try {
            account = new CreateAccount("The Turkmenistan News")
                    .setAuthorName("The Turkmenistan News")
                    .setAuthorUrl("https://www.google.com/")
                    .execute();
        } catch (TelegraphException e) {
            e.printStackTrace();
        }
        Page page =null;
        try {
            List<Node> listContent = new ArrayList<>();
            page = new CreatePage(account.getAccessToken(), "Комментарии", getCommentContent(listContent, 0, commentNodeList))
                    .setAuthorName(parser)                               //Default author name used when creating new articles.
                    .setAuthorUrl(commentsLink)                           //Default profile link, opened when users click on the author's name below the title. Can be any link, not necessarily to a Telegram profile or channel.
                    .execute();
        } catch (TelegraphException e) {
            e.printStackTrace();
        }
        String url = page.getUrl();

        SendMessage message = new SendMessage();

        message.setChatId(String.valueOf(chatId));
        message.setText(url);
        return message;
    }

    private void displayComments(List<CommentNode<Comment>> commentNodeList) {
        for (CommentNode<Comment> commentNode : commentNodeList) {
            System.out.println(commentNode.getComment().getAuthor()+" "+commentNode.getComment().getText());
            if (commentNode.getChildren().size()>0)
                displayComments(commentNode.getChildren());
        }
    }



    private List<Node> getCommentContent(List<Node> listContent, int level, List<CommentNode<Comment>> commentNodeList){
        if (commentNodeList.isEmpty()){
            List<Node> noCommentContent = new ArrayList<>();
            Node authorNode = new NodeText("Комментариев нет.");
            List<Node> authorNodeList = new ArrayList<>();
            authorNodeList.add(authorNode);
            Node paragraphTagNode = new NodeElement("p", new HashMap<>(), authorNodeList);
            List<Node> authorNodeList2 = new ArrayList<>();
            authorNodeList2.add(paragraphTagNode);
            Node boldTagNode = new NodeElement("b", new HashMap<>(), authorNodeList2);
            noCommentContent.add(boldTagNode);
            return noCommentContent;
        }


        StringBuffer shift = new StringBuffer();
        for (int i = 0; i < level; i++) {
            shift.append("       ");
        }

        for (CommentNode<Comment> commentNode : commentNodeList) {

            //Comment author
            Node authorNode = new NodeText(shift+commentNode.getComment().getAuthor());
            List<Node> authorNodeList = new ArrayList<>();
            authorNodeList.add(authorNode);
            Node paragraphTagNode = new NodeElement("p", new HashMap<>(), authorNodeList);
            List<Node> authorNodeList2 = new ArrayList<>();
            authorNodeList2.add(paragraphTagNode);
            Node boldTagNode = new NodeElement("b", new HashMap<>(), authorNodeList2);

            //Reply to //Comment date
            Node replyToNode = new NodeText(shift+commentNode.getComment().getReplyToAuthor());
            Node dateNode = new NodeText(commentNode.getComment().getDate());
            List<Node> replyToNodeList = new ArrayList<>();
            replyToNodeList.add(replyToNode);
            replyToNodeList.add(dateNode);

            Node italicTagNode1 = new NodeElement("i", new HashMap<>(), replyToNodeList);
            List<Node> replyToAndDateList2 = new ArrayList<>();
            replyToAndDateList2.add(italicTagNode1);
            Node italicTagNode = new NodeElement("p", new HashMap<>(), replyToAndDateList2);

            //Comment
            Node textNode = new NodeText(shift+commentNode.getComment().getText());
            List<Node> textNodeList = new ArrayList<>();
            textNodeList.add(textNode);
            Node paragraphTagCommentNode = new NodeElement("p", new HashMap<>(), textNodeList);

            //Vote За: 29 | Против: 1
            Node upNode = new NodeText(shift+"За: ");
            List<Node> upNodeList = new ArrayList<>();
            upNodeList.add(upNode);
            Node boldTagUpVoteNode = new NodeElement("b", new HashMap<>(), upNodeList);
            Node upQuantityNode = new NodeText(commentNode.getComment().getLikeNumber());

            Node downNode = new NodeText("  |  Против: ");
            List<Node> downNodeList = new ArrayList<>();
            downNodeList.add(downNode);
            Node boldTagDownVoteNode = new NodeElement("b", new HashMap<>(), downNodeList);
            Node downQuantityNode = new NodeText(commentNode.getComment().getDislikeNumber());

            List<Node> upNodeList2 = new ArrayList<>();
            upNodeList2.add(boldTagUpVoteNode);
            upNodeList2.add(upQuantityNode);
            upNodeList2.add(boldTagDownVoteNode);
            upNodeList2.add(downQuantityNode);
            Node voteNode = new NodeElement("p", new HashMap<>(), upNodeList2);


            List<Node> tagContent = new ArrayList<>();
            tagContent.add(boldTagNode);
            tagContent.add(italicTagNode);
            tagContent.add(paragraphTagCommentNode);
            tagContent.add(voteNode);

            Node preNode1 = new NodeElement("p", new HashMap<>(), tagContent);


            listContent.add(preNode1);
            if (commentNode.getChildren().size()>0)
                getCommentContent(listContent, level+1, commentNode.getChildren());

        }

        return listContent;
    }
}
