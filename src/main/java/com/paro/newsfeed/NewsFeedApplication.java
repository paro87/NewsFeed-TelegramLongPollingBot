/**
 * The NewsFeed Application implements an application that
 * periodically parses the latest news from defined various websites
 * which do not offer an API, and publishes them in a Telegram Channel
 * displayed in a blogging platform Telegraph with an option of a callback
 * button for comments, clicking on which provides up to time comments
 * in Telegram Bot chat, also displayed as a Telegraph page.
 *
 * @author  Palvan Rozyyev
 * @version 1.0
 * @since   2021-09-17
 */
package com.paro.newsfeed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NewsFeedApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsFeedApplication.class, args);
    }

}
