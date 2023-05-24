package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HarbCareerDateTimeParser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class HabrCareerParse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private String retrieveDescription(String link) {
        String description = "";
        try {
            Connection cnDesc = Jsoup.connect(link);
            Document document = cnDesc.get();
            List<String> list = new LinkedList<>();
            Elements container = document.select(".basic-section--appearance-vacancy-description");
            Elements h2 = container.select("> h2");
            list.add(h2.text());
            Element div = container.select(".vacancy-description__text").first();
            Elements divEl = div.select("> *");
            for (Element el : divEl) {
                list.add(el.text());
            }
            description = String.join(System.lineSeparator(), list);
            return description;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        HarbCareerDateTimeParser dtParser = new HarbCareerDateTimeParser();
        for (int pageNumber = 1; pageNumber < 2; pageNumber++) {
            String pageLink = String.format("%s/vacancies/java_developer?page=%d", SOURCE_LINK, pageNumber);
            System.out.printf("Page №%d%n", pageNumber);
            try {
                Connection connection = Jsoup.connect(pageLink);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    HabrCareerParse habrParser = new HabrCareerParse();
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Element linkElement = titleElement.child(0);
                    String vacancyName = titleElement.text();
                    String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                    String description = habrParser.retrieveDescription(link);
                    String dateTime = row.select(".basic-date").first().attr("datetime");
                    System.out.printf("%s %s %s%n %s%n", vacancyName, dtParser.parse(dateTime), link, description);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
