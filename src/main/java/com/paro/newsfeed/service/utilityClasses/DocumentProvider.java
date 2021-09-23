/**
 * This class provides the Document instance for the received URL
 *
 *
 * @author  Palvan Rozyyev
 * @version 1.0
 * @since   2021-09-17
 */

package com.paro.newsfeed.service.utilityClasses;

import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Log4j2
@Component
public class DocumentProvider {
    public Document getDocument(String webLink){
        Document doc = null;
        try {
            // TO-DO: https://stackoverflow.com/questions/7744075/how-to-connect-via-https-using-jsoup
            //System.out.println("WEBLINK "+webLink);
            //doc = Jsoup.connect(webLink).get();
            //WARNING: Disabling certificate check
            doc = SSLHelper.getConnection(webLink).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").referrer("http://www.google.com").get();
            if (doc==null)
                throw new NullPointerException();
        } catch (IOException e) {
            log.error("[DocumentProvider: getDocument]: Error occurred while retrieving from url: {}", webLink);
            e.printStackTrace();
        }
        log.info("[DocumentProvider: getDocument]: Document retrieved from url: {}", webLink);
        return doc;
    }
}
