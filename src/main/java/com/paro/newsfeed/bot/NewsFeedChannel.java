/**
 * This class publishes the retrieved news from parsing action
 * in the Telegram channel after creating a Telegraph page and
 * equipping with a callback button for comments.
 *
 *
 * @author  Palvan Rozyyev
 * @version 1.0
 * @since   2021-09-17
 */
package com.paro.newsfeed.bot;

import com.paro.newsfeed.model.News;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
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
import java.util.Map;

@Component
@Log4j2
public class NewsFeedChannel {
    @Value("${telegramchannel.chatId}")
    private String chatId;
    @Value("${telegramchannel.accountShortname}")
    private String accountShortname;
    @Value("${telegramchannel.accountAuthorName}")
    private String accountAuthorName;
    @Value("${telegramchannel.accountAuthorUrl}")
    private String accountAuthorUrl;

    private final NewsFeedBot newsFeedBot;
    @Autowired
    public NewsFeedChannel(NewsFeedBot newsFeedBot){
        this.newsFeedBot = newsFeedBot;
    }

    public void startNewsFeedChannel(Map<String, List<News>> allNews){
        TelegraphContextInitializer.init();
        TelegraphContext.registerInstance(ExecutorOptions.class, new ExecutorOptions());
        log.info("[NewsFeedChannel: startNewsFeedChannel]: Creating Telegraph pages for the news");


        allNews.forEach((author, newsList)->{
            // Creating an account for the owner of the Telegram channel
            Account account = null;
            try {
                account = new CreateAccount(accountShortname)
                        .setAuthorName(accountAuthorName)
                        .setAuthorUrl(accountAuthorUrl)
                        .execute();
            } catch (TelegraphException e) {
                e.printStackTrace();
            }

            for (News news : newsList) {
                if (news.getTitle().isEmpty())
                    continue;

                // Constructing Telegraph display page
                Map<String, String> dom=new HashMap<>();
                dom.put("src", news.getMainPicture());
                Node mainPicture = new NodeElement("img", dom, null);
                List<Node> content = new ArrayList<>();

                for (String paragraphText : news.getParagraphs()) {
                    Node contentNode = new NodeText(paragraphText);
                    List<Node> paragraphs = new ArrayList<>();
                    paragraphs.add(contentNode);
                    Node paragraphNode = new NodeElement("p", new HashMap<>(), paragraphs);
                    content.add(paragraphNode);
                }
                if (content.size()>0)
                    ((NodeElement) content.get(0)).getChildren().add(0, mainPicture);
                List<String> pictures = news.getPictures();

                if (pictures!=null && pictures.size()>0){
                    for (String picture : pictures) {
                        Map<String, String> dom2=new HashMap<>();
                        dom2.put("src", picture);
                        Node mainPicture2 = new NodeElement("img", dom, null);
                        ((NodeElement) content.get(0)).getChildren().add(mainPicture2);
                    }
                }

                Page page =null;
                try {
                    page = new CreatePage(account.getAccessToken(), news.getTitle(), content)
                            .setAuthorName(author)               //Default author name used when creating new articles.
                            .setAuthorUrl(news.getLink())        //Default profile link, opened when users click on the author's name below the title. Can be any link, not necessarily to a Telegram profile or channel.
                            .execute();
                } catch (TelegraphException e) {
                    e.printStackTrace();
                }
                if (page==null)
                    continue;
                String pageUrl = page.getUrl();

                // Setting inline keyboard for comments for each of the news message in Telegraph display format
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                inlineKeyboardButton.setText("Комментарии из сайта");
                System.out.println(news.getSource()+" "+news.getLink());
                inlineKeyboardButton.setCallbackData(news.getSource()+" "+news.getLink());
                inlineKeyboardButton.setSwitchInlineQuery();

                List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
                keyboardButtonsRow.add(inlineKeyboardButton);

                List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
                rowList.add(keyboardButtonsRow);
                inlineKeyboardMarkup.setKeyboard(rowList);

                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText(pageUrl);
                message.setReplyMarkup(inlineKeyboardMarkup);

                log.info("[NewsFeedChannel: startNewsFeedChannel]: Sending message to the Telegram channel with chatId: {} and message: {}", chatId, pageUrl);
                try {
                    newsFeedBot.execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        });

    }


}
