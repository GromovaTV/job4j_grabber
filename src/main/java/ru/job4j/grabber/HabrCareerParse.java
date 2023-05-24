package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HarbCareerDateTimeParser;

public class HabrCareerParse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    public static void main(String[] args) {
        HarbCareerDateTimeParser dtParser = new HarbCareerDateTimeParser();
        for (int pageNumber = 1; pageNumber < 6; pageNumber++) {
            String pageLink = String.format("%s/vacancies/java_developer?page=%d", SOURCE_LINK, pageNumber);
            System.out.printf("Page №%d%n", pageNumber);
            try {
                Connection connection = Jsoup.connect(pageLink);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Element linkElement = titleElement.child(0);
                    String vacancyName = titleElement.text();
                    String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                    String dateTime = row.select(".basic-date").first().attr("datetime");
                    System.out.printf("%s %s %s%n", vacancyName, dtParser.parse(dateTime), link);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
