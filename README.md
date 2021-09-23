# NewsFeed Application (Telegram Longpolling bot) 
##[Under development]

### About
The NewsFeed Application implements an application that periodically parses the latest news from defined various websites which do not offer an API, and publishes them in a Telegram Channel displayed in a blogging platform Telegraph with an option of a callback button for comments, clicking on which provides up to time comments in Telegram Bot chat, also displayed as a Telegraph page.

### Requirements

In order to build and run the application you will need:

* Java 11
* Desktop or mobile version of Telegram

### How to use

 

### Code base

Dependencies:
* spring-boot-starter-web
* spring-boot-starter-data-jpa
* mysql
* lombok
* emoji-java

### Other useful Telegram APIs
```
https://api.telegram.org/bot1234567890:XXXXXXX
```
```
https://api.telegram.org/bot1234567890:XXXXXXX/getMe
```
```
https://api.telegram.org/bot1234567890:XXXXXXX/getWebhookInfo
```
Sending request message to a channel through postman:
```
https://api.telegram.org/bot1234567890:XXXXXXX/sendMessage
```
Sending a message to a channel as a request parameter:
```
https://api.telegram.org/bot1234567890:XXXXXXX/sendMessage?chat_id=XXXXX&text=Hello
```

