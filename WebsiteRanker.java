package PdfFile;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class Website {
    String url;
    int socialLinkCount;

    public Website(String url, int socialLinkCount) {
        this.url = url;
        this.socialLinkCount = socialLinkCount;
    }
}

public class WebsiteRanker {

    private static final String[] SOCIAL_KEYWORDS = {
            "facebook.com", "twitter.com", "linkedin.com", "instagram.com"
    };

    public static void main(String[] args) {
        List<String> urls = List.of(
                "https://example1.com",
                "https://example2.com"
                // Add more URLs as needed
        );

        List<Website> websites = new ArrayList<>();

        for (String url : urls) {
            int count = countSocialLinks(url);
            websites.add(new Website(url, count));
        }

        // Sort websites by social link count in descending order
        websites.sort(Comparator.comparingInt((Website w) -> w.socialLinkCount).reversed());

        // Print ranked websites
        for (int i = 0; i < websites.size(); i++) {
            Website website = websites.get(i);
            System.out.println("Rank " + (i + 1) + ": " + website.url + " with " + website.socialLinkCount + " social links");
        }
    }

    private static int countSocialLinks(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");
            int socialLinks = 0;

            for (Element link : links) {
                String href = link.attr("href");
                for (String keyword : SOCIAL_KEYWORDS) {
                    if (href.contains(keyword)) {
                        socialLinks++;
                        break;
                    }
                }
            }

            return socialLinks;
        } catch (IOException e) {
            System.out.println("Error fetching " + url + ": " + e.getMessage());
            return 0;
        }
    }
}

